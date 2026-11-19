package com.example.data.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class PdfInspectionResult(
    val pageCount: Int,
    val coverPath: String?,
    val fileSizeBytes: Long
)

object PdfStorageHelper {
    fun getPdfDirectory(context: Context): File {
        return File(context.filesDir, "pdfs").apply { mkdirs() }
    }

    fun getCoverDirectory(context: Context): File {
        return File(context.filesDir, "covers").apply { mkdirs() }
    }

    fun pdfFileForBook(context: Context, bookId: String): File {
        val safeName = bookId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(getPdfDirectory(context), "$safeName.pdf")
    }

    fun coverFileForBook(context: Context, bookId: String): File {
        val safeName = bookId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(getCoverDirectory(context), "$safeName.jpg")
    }

    fun resolveDisplayName(context: Context, uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) {
                        val name = cursor.getString(idx)
                        if (!name.isNullOrBlank()) {
                            return name.removeSuffix(".pdf").removeSuffix(".PDF")
                                .replace('_', ' ')
                                .replace('-', ' ')
                                .trim()
                        }
                    }
                }
            }
        uri.lastPathSegment?.substringAfterLast('/')?.let { fallback ->
            val cleaned = fallback.removeSuffix(".pdf").removeSuffix(".PDF")
                .replace('_', ' ')
                .replace('-', ' ')
                .trim()
            if (cleaned.isNotBlank()) return cleaned
        }
        return "Imported Civil Engineering Textbook"
    }

    suspend fun copyUriToPrivatePdf(
        context: Context,
        uri: Uri,
        destinationFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            destinationFile.parentFile?.mkdirs()
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Could not open selected PDF file")
            var totalBytes = 0L
            var headerValid = false
            inputStream.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        if (!headerValid && read >= 4) {
                            val header = String(buffer, 0, minOf(read, 16), Charsets.US_ASCII)
                            if (!header.contains("%PDF")) {
                                throw IllegalArgumentException("Selected file is not a valid PDF document")
                            }
                            headerValid = true
                        }
                        output.write(buffer, 0, read)
                        totalBytes += read
                    }
                    output.flush()
                }
            }
            if (!headerValid || totalBytes < 512L) {
                destinationFile.delete()
                throw IllegalArgumentException("Selected PDF appears empty or corrupted")
            }
            destinationFile
        }
    }

    /**
     * Inspects a local PDF via Android's native PdfRenderer to get its real page count
     * and extracts Page 1 as a high-contrast local cover thumbnail.
     */
    suspend fun inspectPdfAndExtractCover(
        pdfFile: File,
        coverOutputFile: File
    ): Result<PdfInspectionResult> = withContext(Dispatchers.IO) {
        runCatching {
            if (!pdfFile.exists() || pdfFile.length() == 0L) {
                throw IllegalStateException("PDF file does not exist")
            }
            ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val count = renderer.pageCount
                    if (count <= 0) {
                        throw IllegalStateException("PDF contains no readable pages")
                    }
                    var savedCover: String? = null
                    runCatching {
                        renderer.openPage(0).use { firstPage ->
                            val targetWidth = 420
                            val aspect = firstPage.height.toFloat() / firstPage.width.coerceAtLeast(1).toFloat()
                            val targetHeight = (targetWidth * aspect).toInt().coerceIn(320, 720)
                            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bitmap)
                            canvas.drawColor(Color.WHITE)
                            firstPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            coverOutputFile.parentFile?.mkdirs()
                            FileOutputStream(coverOutputFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 86, out)
                            }
                            bitmap.recycle()
                            savedCover = coverOutputFile.absolutePath
                        }
                    }
                    PdfInspectionResult(
                        pageCount = count,
                        coverPath = savedCover,
                        fileSizeBytes = pdfFile.length()
                    )
                }
            }
        }
    }
}

/**
 * Thread-safe native Android PdfRenderer session with:
 * - Lazy idempotent auto-open (eliminates first-page render race conditions)
 * - Dedicated high-res page LRU cache + low-res thumbnail LRU cache
 * - Optional smart white-margin cropping for scanned textbooks
 */
class PdfBookSession(private val pdfFile: File) : AutoCloseable {
    private val renderMutex = Mutex()
    private var descriptor: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null
    @Volatile
    private var isClosed = false

    private val pageCache = object : LruCache<String, Bitmap>(16) {}
    private val thumbnailCache = object : LruCache<Int, Bitmap>(32) {}

    @Volatile
    var pageCount: Int = 0
        private set

    fun quantizeTargetWidth(targetWidthPx: Int): Int {
        val clampedWidth = targetWidthPx.coerceIn(640, 2200)
        return ((clampedWidth + 63) / 64) * 64
    }

    fun getCachedPage(
        pageIndex: Int,
        targetWidthPx: Int,
        cropMargins: Boolean = false
    ): Bitmap? {
        if (isClosed || pageIndex < 0) return null
        val quantizedWidth = quantizeTargetWidth(targetWidthPx)
        val cacheKey = "${pageIndex}_${quantizedWidth}_$cropMargins"
        return synchronized(pageCache) {
            pageCache.get(cacheKey)?.takeIf { !it.isRecycled }
        }
    }

    private fun ensureOpenLocked(): PdfRenderer {
        renderer?.let { return it }
        if (isClosed) {
            throw IllegalStateException("PDF session has already been closed")
        }
        if (!pdfFile.exists() || pdfFile.length() == 0L) {
            throw IllegalStateException("Offline PDF file is missing on device")
        }
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val pdfRenderer = PdfRenderer(pfd)
        descriptor = pfd
        renderer = pdfRenderer
        pageCount = pdfRenderer.pageCount
        return pdfRenderer
    }

    suspend fun open(): Result<Int> = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            runCatching {
                ensureOpenLocked().pageCount
            }
        }
    }

    suspend fun renderPage(
        pageIndex: Int,
        targetWidthPx: Int,
        cropMargins: Boolean = false
    ): Bitmap? = withContext(Dispatchers.IO) {
        if (isClosed || pageIndex < 0) return@withContext null
        val quantizedWidth = quantizeTargetWidth(targetWidthPx)
        val cacheKey = "${pageIndex}_${quantizedWidth}_$cropMargins"

        synchronized(pageCache) {
            pageCache.get(cacheKey)?.takeIf { !it.isRecycled }
        }?.let { return@withContext it }

        renderMutex.withLock {
            if (isClosed) return@withContext null
            val activeRenderer = runCatching { ensureOpenLocked() }.getOrNull() ?: return@withContext null
            if (pageIndex !in 0 until activeRenderer.pageCount) return@withContext null

            synchronized(pageCache) {
                pageCache.get(cacheKey)?.takeIf { !it.isRecycled }
            }?.let { return@withContext it }

            runCatching {
                activeRenderer.openPage(pageIndex).use { page ->
                    val ratio = page.height.toFloat() / page.width.coerceAtLeast(1).toFloat()
                    val width = quantizedWidth
                    val height = (width * ratio).toInt().coerceIn(480, 3400)
                    val rendered = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(rendered)
                    canvas.drawColor(Color.WHITE)
                    page.render(rendered, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val finalBitmap = if (cropMargins) {
                        val trimmed = cropWhiteMargins(rendered)
                        if (trimmed !== rendered) {
                            rendered.recycle()
                        }
                        trimmed
                    } else {
                        rendered
                    }

                    synchronized(pageCache) {
                        pageCache.put(cacheKey, finalBitmap)
                    }
                    finalBitmap
                }
            }.getOrNull()
        }
    }

    suspend fun renderThumbnail(pageIndex: Int, targetWidthPx: Int = 220): Bitmap? = withContext(Dispatchers.IO) {
        if (isClosed || pageIndex < 0) return@withContext null

        synchronized(thumbnailCache) {
            thumbnailCache.get(pageIndex)?.takeIf { !it.isRecycled }
        }?.let { return@withContext it }

        renderMutex.withLock {
            if (isClosed) return@withContext null
            val activeRenderer = runCatching { ensureOpenLocked() }.getOrNull() ?: return@withContext null
            if (pageIndex !in 0 until activeRenderer.pageCount) return@withContext null

            synchronized(thumbnailCache) {
                thumbnailCache.get(pageIndex)?.takeIf { !it.isRecycled }
            }?.let { return@withContext it }

            runCatching {
                activeRenderer.openPage(pageIndex).use { page ->
                    val width = targetWidthPx.coerceIn(120, 320)
                    val ratio = page.height.toFloat() / page.width.coerceAtLeast(1).toFloat()
                    val height = (width * ratio).toInt().coerceIn(140, 480)
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    synchronized(thumbnailCache) {
                        thumbnailCache.put(pageIndex, bitmap)
                    }
                    bitmap
                }
            }.getOrNull()
        }
    }

    suspend fun prefetchPages(indices: List<Int>, targetWidthPx: Int, cropMargins: Boolean = false) {
        for (idx in indices) {
            if (isClosed) break
            if (idx in 0 until pageCount) {
                renderPage(idx, targetWidthPx, cropMargins)
            }
        }
    }

    /**
     * Detects excessive empty white margins on scanned textbook PDFs and trims them cleanly
     * while preserving a comfortable 3.5% book-margin breathing zone around content.
     */
    private fun cropWhiteMargins(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        if (w < 120 || h < 120) return source

        val step = 6
        val rowPixels = IntArray(w)
        var minX = w
        var maxX = 0
        var minY = h
        var maxY = 0
        var foundInk = false

        var y = 0
        while (y < h) {
            source.getPixels(rowPixels, 0, w, 0, y, w, 1)
            var x = 0
            while (x < w) {
                val p = rowPixels[x]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                // Consider pixel as "ink" if noticeably darker than white paper
                if (r < 236 || g < 236 || b < 236) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                    foundInk = true
                }
                x += step
            }
            y += step
        }

        if (!foundInk) return source

        val padX = (w * 0.032f).toInt()
        val padY = (h * 0.028f).toInt()
        val left = (minX - padX).coerceAtLeast(0)
        val top = (minY - padY).coerceAtLeast(0)
        val right = (maxX + padX).coerceAtMost(w)
        val bottom = (maxY + padY).coerceAtMost(h)

        val cropW = right - left
        val cropH = bottom - top

        // Only crop if we meaningfully reclaim margins and preserve readable content
        if (cropW < w * 0.35f || cropH < h * 0.35f) return source
        if (cropW > w * 0.94f && cropH > h * 0.94f) return source

        return runCatching {
            Bitmap.createBitmap(source, left, top, cropW, cropH)
        }.getOrDefault(source)
    }

    override fun close() {
        isClosed = true
        synchronized(pageCache) { pageCache.evictAll() }
        synchronized(thumbnailCache) { thumbnailCache.evictAll() }
        runCatching { renderer?.close() }
        runCatching { descriptor?.close() }
        renderer = null
        descriptor = null
    }
}
