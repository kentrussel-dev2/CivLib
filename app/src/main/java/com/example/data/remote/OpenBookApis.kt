package com.example.data.remote

import com.example.data.model.BookFormat
import com.example.data.model.BookSourceType
import com.example.data.model.CivilSubject
import com.example.data.model.DiscoverBookResult
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenLibrarySearchResponse(
    @Json(name = "numFound") val numFound: Int = 0,
    @Json(name = "docs") val docs: List<OpenLibraryDoc> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OpenLibraryDoc(
    @Json(name = "key") val key: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "author_name") val authorName: List<String>? = null,
    @Json(name = "isbn") val isbn: List<String>? = null,
    @Json(name = "first_publish_year") val firstPublishYear: Int? = null,
    @Json(name = "publisher") val publisher: List<String>? = null,
    @Json(name = "subject") val subject: List<String>? = null,
    @Json(name = "cover_i") val coverId: Int? = null,
    @Json(name = "ia") val internetArchiveIds: List<String>? = null,
    @Json(name = "first_sentence") val firstSentence: List<String>? = null,
    @Json(name = "number_of_pages_median") val medianPages: Int? = null
)

data class OpenLibraryWorkSummary(
    val description: String?,
    val subjects: List<String>
)

@JsonClass(generateAdapter = true)
data class GutendexResponse(
    @Json(name = "count") val count: Int = 0,
    @Json(name = "results") val results: List<GutendexBook> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GutendexBook(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "authors") val authors: List<GutendexAuthor> = emptyList(),
    @Json(name = "subjects") val subjects: List<String> = emptyList(),
    @Json(name = "bookshelves") val bookshelves: List<String> = emptyList(),
    @Json(name = "formats") val formats: Map<String, String> = emptyMap(),
    @Json(name = "download_count") val downloadCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class GutendexAuthor(
    @Json(name = "name") val name: String
)

data class ResolvedPdfResource(
    val downloadUrl: String,
    val fileSizeBytes: Long = 0L,
    val coverUrl: String? = null
)

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun searchByQuery(
        @Query("q") query: String,
        @Query("limit") limit: Int = 14,
        @Query("fields") fields: String = "key,title,author_name,isbn,first_publish_year,publisher,subject,cover_i,ia,first_sentence,number_of_pages_median"
    ): OpenLibrarySearchResponse

    @GET("search.json")
    suspend fun searchByIsbn(
        @Query("isbn") isbn: String,
        @Query("limit") limit: Int = 10,
        @Query("fields") fields: String = "key,title,author_name,isbn,first_publish_year,publisher,subject,cover_i,ia,first_sentence,number_of_pages_median"
    ): OpenLibrarySearchResponse

    @GET("works/{workId}.json")
    suspend fun getWorkDetailRaw(@Path("workId") workId: String): ResponseBody
}

interface GutendexApi {
    @GET("books/")
    suspend fun searchBooks(
        @Query("search") search: String,
        @Query("languages") languages: String = "en"
    ): GutendexResponse
}

interface InternetArchiveApi {
    @GET("advancedsearch.php")
    suspend fun advancedSearchRaw(
        @Query("q") query: String,
        @Query("fl[]") fields: List<String> = listOf(
            "identifier", "title", "creator", "year", "subject",
            "description", "isbn", "publisher", "downloads", "imagecount"
        ),
        @Query("sort[]") sort: List<String> = listOf("downloads desc"),
        @Query("rows") rows: Int = 16,
        @Query("page") page: Int = 1,
        @Query("output") output: String = "json"
    ): ResponseBody

    @GET("metadata/{identifier}")
    suspend fun getItemMetadataRaw(@Path("identifier") identifier: String): ResponseBody
}

interface DSpaceOpenBookApi {
    @GET("rest/search")
    suspend fun searchRaw(
        @Query("query") query: String,
        @Query("expand") expand: String = "metadata,bitstreams",
        @Query("limit") limit: Int = 12
    ): ResponseBody

    @GET("rest/handle/{prefix}/{suffix}")
    suspend fun getHandleRaw(
        @Path("prefix") prefix: String,
        @Path("suffix") suffix: String,
        @Query("expand") expand: String = "metadata,bitstreams"
    ): ResponseBody
}

object FreeBookNetworkClient {
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(18, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "CivLib-Android/2.0 (Open-Access Civil Engineering Library)")
                .build()
            chain.proceed(request)
        }
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val openLibraryApi: OpenLibraryApi = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(OpenLibraryApi::class.java)

    val gutendexApi: GutendexApi = Retrofit.Builder()
        .baseUrl("https://gutendex.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(GutendexApi::class.java)

    val internetArchiveApi: InternetArchiveApi = Retrofit.Builder()
        .baseUrl("https://archive.org/")
        .client(okHttpClient)
        .build()
        .create(InternetArchiveApi::class.java)

    val oapenApi: DSpaceOpenBookApi = Retrofit.Builder()
        .baseUrl("https://library.oapen.org/")
        .client(okHttpClient)
        .build()
        .create(DSpaceOpenBookApi::class.java)

    val doabApi: DSpaceOpenBookApi = Retrofit.Builder()
        .baseUrl("https://directory.doabooks.org/")
        .client(okHttpClient)
        .build()
        .create(DSpaceOpenBookApi::class.java)

    fun downloadPlainText(url: String, maxChars: Int = 420_000): String? {
        return runCatching {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                if (body.length > maxChars) body.substring(0, maxChars) else body
            }
        }.getOrNull()
    }

    /**
     * Streams a real PDF file to disk with live progress updates and %PDF- signature validation.
     */
    fun downloadPdfToFile(
        url: String,
        destinationFile: File,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<File> {
        return runCatching {
            destinationFile.parentFile?.mkdirs()
            val tempFile = File(destinationFile.parentFile, "${destinationFile.name}.part")
            if (tempFile.exists()) tempFile.delete()

            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/pdf,application/octet-stream,*/*")
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Server returned HTTP ${response.code} for PDF download")
                }
                val body = response.body ?: throw IllegalStateException("Empty PDF response body")
                val totalBytes = body.contentLength()
                var downloaded = 0L
                var headerChecked = false

                body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        val buffer = ByteArray(16 * 1024)
                        var lastReportAt = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            if (!headerChecked && read >= 4) {
                                val magic = String(buffer, 0, minOf(read, 16), Charsets.US_ASCII)
                                if (!magic.contains("%PDF")) {
                                    throw IllegalStateException("Downloaded file is not an unrestricted PDF (may require borrow/login on host)")
                                }
                                headerChecked = true
                            }
                            output.write(buffer, 0, read)
                            downloaded += read
                            val now = System.currentTimeMillis()
                            if (now - lastReportAt >= 120L) {
                                onProgress(downloaded, totalBytes)
                                lastReportAt = now
                            }
                        }
                        output.flush()
                    }
                }
                onProgress(downloaded, totalBytes.takeIf { it > 0 } ?: downloaded)

                if (downloaded < 1024L || !headerChecked) {
                    tempFile.delete()
                    throw IllegalStateException("PDF download was incomplete or unavailable")
                }

                if (destinationFile.exists()) destinationFile.delete()
                if (!tempFile.renameTo(destinationFile)) {
                    tempFile.copyTo(destinationFile, overwrite = true)
                    tempFile.delete()
                }
                destinationFile
            }
        }
    }
}

fun parseOpenLibraryWorkSummary(rawJson: String): OpenLibraryWorkSummary {
    return runCatching {
        val obj = JSONObject(rawJson)
        val descValue = obj.opt("description")
        val description = when (descValue) {
            is String -> descValue
            is JSONObject -> descValue.optString("value").takeIf { it.isNotBlank() }
            else -> null
        }
        val subjectsArr = obj.optJSONArray("subjects")
        val subjects = buildList {
            if (subjectsArr != null) {
                for (i in 0 until subjectsArr.length()) {
                    val s = subjectsArr.optString(i)
                    if (s.isNotBlank()) add(s)
                }
            }
        }
        OpenLibraryWorkSummary(description = description, subjects = subjects)
    }.getOrDefault(OpenLibraryWorkSummary(null, emptyList()))
}

fun parseInternetArchiveSearchResults(
    rawJson: String,
    existingOfflineIds: Set<String>,
    existingLibraryIds: Set<String>
): List<DiscoverBookResult> {
    return runCatching {
        val root = JSONObject(rawJson)
        val docs = root.optJSONObject("response")?.optJSONArray("docs") ?: return emptyList()
        buildList {
            for (i in 0 until docs.length()) {
                val doc = docs.optJSONObject(i) ?: continue
                val identifier = doc.optString("identifier").trim()
                val title = firstStringField(doc, "title")?.trim().orEmpty()
                if (identifier.isBlank() || title.isBlank()) continue

                val author = firstStringField(doc, "creator")?.trim().orEmpty().ifBlank { "Engineering Author" }
                val year = firstStringField(doc, "year")?.take(4)?.toIntOrNull()
                val isbn = firstStringField(doc, "isbn")?.trim().orEmpty()
                val publisher = firstStringField(doc, "publisher")?.trim().orEmpty()
                    .ifBlank { "Internet Archive Open Texts" }
                val description = firstStringField(doc, "description")?.trim().orEmpty()
                    .replace(Regex("<[^>]*>"), " ")
                    .replace(Regex("\\s+"), " ")
                    .take(420)
                val subjects = joinStringField(doc, "subject")
                val pages = doc.optInt("imagecount", 0).takeIf { it > 0 }
                val id = "ia_${identifier.replace(Regex("[^a-zA-Z0-9_]"), "_")}"
                val subject = CivilSubject.inferFromText("$title $subjects $description")

                add(
                    DiscoverBookResult(
                        id = id,
                        title = title,
                        author = author,
                        isbn = isbn,
                        subject = subject,
                        publishYear = year,
                        publisher = publisher,
                        description = description.ifBlank {
                            "Full-length open-access civil engineering PDF available on Internet Archive."
                        },
                        coverUrl = "https://archive.org/services/img/$identifier",
                        sourceType = BookSourceType.INTERNET_ARCHIVE,
                        format = BookFormat.PDF,
                        archiveIdentifier = identifier,
                        estimatedPages = pages,
                        isAlreadyInLibrary = id in existingLibraryIds,
                        isOfflineDownloaded = id in existingOfflineIds
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

fun resolveInternetArchivePdfFromMetadata(identifier: String, rawJson: String): ResolvedPdfResource? {
    return runCatching {
        val root = JSONObject(rawJson)
        val files = root.optJSONArray("files") ?: return null
        val server = root.optString("d1").takeIf { it.isNotBlank() }
            ?: root.optString("d2").takeIf { it.isNotBlank() }
        val dir = root.optString("dir").takeIf { it.isNotBlank() }

        var bestName: String? = null
        var bestSize = 0L
        var bestScore = -1

        for (i in 0 until files.length()) {
            val f = files.optJSONObject(i) ?: continue
            val name = f.optString("name")
            if (!name.endsWith(".pdf", ignoreCase = true)) continue
            val isPrivate = f.optString("private").equals("true", ignoreCase = true)
            if (isPrivate) continue
            val format = f.optString("format").lowercase()
            if (format.contains("encrypted") || format.contains("lcp")) continue

            val size = f.optString("size").toLongOrNull() ?: f.optLong("size", 0L)
            val score = when {
                format == "text pdf" -> 100
                format == "additional text pdf" -> 80
                format.contains("pdf") && !name.contains("_bw", ignoreCase = true) -> 70
                else -> 40
            }
            if (score > bestScore || (score == bestScore && size > bestSize)) {
                bestScore = score
                bestName = name
                bestSize = size
            }
        }

        val chosenFile = bestName ?: return null
        val encodedFile = URLEncoder.encode(chosenFile, "UTF-8").replace("+", "%20").replace("%2F", "/")
        val url = if (server != null && dir != null) {
            "https://$server$dir/$encodedFile"
        } else {
            "https://archive.org/download/$identifier/$encodedFile"
        }
        ResolvedPdfResource(
            downloadUrl = url,
            fileSizeBytes = bestSize,
            coverUrl = "https://archive.org/services/img/$identifier"
        )
    }.getOrNull()
}

fun parseDSpaceSearchResults(
    rawJson: String,
    baseUrl: String,
    sourceType: BookSourceType,
    existingOfflineIds: Set<String>,
    existingLibraryIds: Set<String>
): List<DiscoverBookResult> {
    return runCatching {
        val arr = JSONArray(rawJson)
        buildList {
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val parsed = parseSingleDSpaceItem(
                    item = item,
                    baseUrl = baseUrl,
                    sourceType = sourceType,
                    existingOfflineIds = existingOfflineIds,
                    existingLibraryIds = existingLibraryIds
                )
                if (parsed != null) add(parsed)
            }
        }
    }.getOrDefault(emptyList())
}

fun resolveDSpacePdfFromHandleJson(baseUrl: String, rawJson: String): ResolvedPdfResource? {
    return runCatching {
        val obj = JSONObject(rawJson)
        extractBestPdfBitstream(obj, baseUrl)
    }.getOrNull()
}

private fun parseSingleDSpaceItem(
    item: JSONObject,
    baseUrl: String,
    sourceType: BookSourceType,
    existingOfflineIds: Set<String>,
    existingLibraryIds: Set<String>
): DiscoverBookResult? {
    val handle = item.optString("handle").trim()
    val uuid = item.optString("uuid").ifBlank { item.optString("id") }.trim()
    val metadataArr = item.optJSONArray("metadata")

    val metaMap = mutableMapOf<String, MutableList<String>>()
    if (metadataArr != null) {
        for (i in 0 until metadataArr.length()) {
            val entry = metadataArr.optJSONObject(i) ?: continue
            val key = entry.optString("key")
            val value = entry.optString("value").trim()
            if (key.isNotBlank() && value.isNotBlank()) {
                metaMap.getOrPut(key) { mutableListOf() }.add(value)
            }
        }
    }

    val title = metaMap["dc.title"]?.firstOrNull()
        ?: item.optString("name").takeIf { it.isNotBlank() }
        ?: return null
    val author = metaMap["dc.contributor.author"]?.take(2)?.joinToString(", ")
        ?: metaMap["dc.contributor.editor"]?.take(2)?.joinToString(", ")
        ?: "Open Access Author"
    val isbn = metaMap["dc.identifier.isbn"]?.firstOrNull().orEmpty()
    val publisher = metaMap["dc.publisher"]?.firstOrNull()
        ?: metaMap["oapen.imprint"]?.firstOrNull()
        ?: sourceType.displayLabel
    val year = metaMap["dc.date.issued"]?.firstOrNull()?.take(4)?.toIntOrNull()
    val abstractText = metaMap["dc.description.abstract"]?.firstOrNull()
        ?: metaMap["dc.description"]?.firstOrNull()
        ?: "Peer-reviewed open-access textbook available as a full downloadable PDF."
    val subjectsText = (metaMap["dc.subject"].orEmpty() + title).joinToString(" ")
    val pages = metaMap["dc.format.extent"]?.firstOrNull()
        ?.let { Regex("\\d+").find(it)?.value?.toIntOrNull() }

    val pdfResource = extractBestPdfBitstream(item, baseUrl)
    // Require either a direct PDF bitstream or a valid handle that resolves to one
    if (pdfResource == null && handle.isBlank()) return null

    val prefix = if (sourceType == BookSourceType.OAPEN) "oapen" else "doab"
    val rawKey = handle.ifBlank { uuid }.replace(Regex("[^a-zA-Z0-9_]"), "_")
    val id = "${prefix}_$rawKey"
    val subject = CivilSubject.inferFromText("$title $subjectsText $abstractText")

    return DiscoverBookResult(
        id = id,
        title = title,
        author = author,
        isbn = isbn,
        subject = subject,
        publishYear = year,
        publisher = publisher,
        description = abstractText.take(450),
        coverUrl = pdfResource?.coverUrl,
        sourceType = sourceType,
        format = BookFormat.PDF,
        externalKey = handle.ifBlank { null },
        pdfDownloadUrl = pdfResource?.downloadUrl,
        estimatedPages = pages,
        fileSizeBytes = pdfResource?.fileSizeBytes ?: 0L,
        isAlreadyInLibrary = id in existingLibraryIds,
        isOfflineDownloaded = id in existingOfflineIds
    )
}

private fun extractBestPdfBitstream(item: JSONObject, baseUrl: String): ResolvedPdfResource? {
    val bitstreams = item.optJSONArray("bitstreams") ?: return null
    val cleanBase = baseUrl.trimEnd('/')
    var bestPdfUrl: String? = null
    var bestPdfSize = 0L
    var coverUrl: String? = null

    for (i in 0 until bitstreams.length()) {
        val bs = bitstreams.optJSONObject(i) ?: continue
        val mime = bs.optString("mimeType").lowercase()
        val format = bs.optString("format").lowercase()
        val name = bs.optString("name").lowercase()
        val bundle = bs.optString("bundleName").uppercase()
        val retrieveLink = bs.optString("retrieveLink").trim()
        val sizeBytes = bs.optLong("sizeBytes", 0L)

        if (retrieveLink.isBlank()) continue
        val fullUrl = if (retrieveLink.startsWith("http")) retrieveLink else "$cleanBase$retrieveLink"

        if (bundle == "THUMBNAIL" || mime.startsWith("image/") || name.endsWith(".jpg") || name.endsWith(".png")) {
            if (coverUrl == null) coverUrl = fullUrl
        }

        val isPdf = mime.contains("pdf") || format.contains("pdf") || name.endsWith(".pdf")
        if (isPdf && (bestPdfUrl == null || sizeBytes > bestPdfSize)) {
            bestPdfUrl = fullUrl
            bestPdfSize = sizeBytes
        }
    }

    return bestPdfUrl?.let { ResolvedPdfResource(it, bestPdfSize, coverUrl) }
}

private fun firstStringField(obj: JSONObject, key: String): String? {
    val value = obj.opt(key) ?: return null
    return when (value) {
        is String -> value
        is Number -> value.toString()
        is JSONArray -> (0 until value.length()).firstNotNullOfOrNull { idx ->
            value.optString(idx).takeIf { it.isNotBlank() }
        }
        else -> null
    }
}

private fun joinStringField(obj: JSONObject, key: String): String {
    val value = obj.opt(key) ?: return ""
    return when (value) {
        is String -> value
        is JSONArray -> buildList {
            for (i in 0 until value.length()) {
                val s = value.optString(i)
                if (s.isNotBlank()) add(s)
            }
        }.joinToString(" ")
        else -> ""
    }
}
