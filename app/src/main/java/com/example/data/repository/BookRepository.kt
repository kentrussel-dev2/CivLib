package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.BookEntity
import com.example.data.local.BookPageEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.CivLibDao
import com.example.data.local.OpenAccessCivilCatalog
import com.example.data.model.BookFormat
import com.example.data.model.BookSourceType
import com.example.data.model.CivilSubject
import com.example.data.model.DiscoverBookResult
import com.example.data.model.DownloadProgress
import com.example.data.model.IsbnValidator
import com.example.data.pdf.PdfStorageHelper
import com.example.data.remote.FreeBookNetworkClient
import com.example.data.remote.GutendexBook
import com.example.data.remote.OpenLibraryDoc
import com.example.data.remote.parseDSpaceSearchResults
import com.example.data.remote.parseInternetArchiveSearchResults
import com.example.data.remote.parseOpenLibraryWorkSummary
import com.example.data.remote.resolveDSpacePdfFromHandleJson
import com.example.data.remote.resolveInternetArchivePdfFromMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class CombinedSearchResults(
    val localMatches: List<BookEntity>,
    val remoteResults: List<DiscoverBookResult>,
    val usedNetwork: Boolean,
    val errorMessage: String? = null
)

class BookRepository(
    private val context: Context,
    private val dao: CivLibDao
) {
    val allBooks: Flow<List<BookEntity>> = dao.observeAllBooks()
    val offlineBooks: Flow<List<BookEntity>> = dao.observeOfflineBooks()
    val lastOpenedBook: Flow<BookEntity?> = dao.observeLastOpenedBook()
    val allBookmarks: Flow<List<BookmarkEntity>> = dao.observeAllBookmarks()

    suspend fun ensureStarterCatalog() = withContext(Dispatchers.IO) {
        dao.insertBooksIgnore(OpenAccessCivilCatalog.starterVolumes())
    }

    /**
     * Hydrates the Civil Engineering Library shelves with live unrestricted full-length PDFs
     * from Internet Archive and peer-reviewed Open Access books from OAPEN.
     */
    suspend fun syncOpenAccessCatalog(isOnline: Boolean): Int = withContext(Dispatchers.IO) {
        if (!isOnline) return@withContext 0
        val existingIds = dao.getAllBookIds().toSet()
        val fetchedEntities = mutableListOf<BookEntity>()

        coroutineScope {
            val iaSubjects = listOf(
                "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND (\"engineering mechanics\" OR \"statics\" OR \"theoretical mechanics\")",
                "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND (\"surveying\" OR \"geodesy\" OR \"topographic surveying\")",
                "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND (\"strength of materials\" OR \"mechanics of materials\" OR \"concrete technology\")",
                "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND (\"structural analysis\" OR \"reinforced concrete\" OR \"steel structures\" OR \"bridge engineering\")",
                "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND (\"soil mechanics\" OR \"foundation engineering\" OR \"geotechnical\")",
                "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND (\"open channel hydraulics\" OR \"fluid mechanics\" OR \"engineering hydrology\")",
                "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND (\"highway engineering\" OR \"transportation engineering\" OR \"water supply\" OR \"construction management\")",
                "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND (\"engineering mathematics\" OR \"differential and integral calculus\" OR \"engineering physics\")",
                "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND (\"finite element\" OR \"earthquake engineering\" OR \"structural dynamics\" OR \"plates and shells\")",
                "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND (\"engineering thermodynamics\" OR \"electrical engineering\" OR \"mechanical engineering\")"
            )

            val iaDeferreds = iaSubjects.map { query ->
                async {
                    runCatching {
                        val raw = FreeBookNetworkClient.internetArchiveApi
                            .advancedSearchRaw(query = query, rows = 8)
                            .string()
                        parseInternetArchiveSearchResults(raw, emptySet(), existingIds)
                    }.getOrDefault(emptyList())
                }
            }

            val oapenDeferred = async {
                runCatching {
                    val raw = FreeBookNetworkClient.oapenApi
                        .searchRaw(query = "civil engineering OR structural engineering OR geotechnical OR hydraulics OR transportation OR environmental engineering OR finite element OR engineering mathematics OR physics", limit = 22)
                        .string()
                    parseDSpaceSearchResults(
                        rawJson = raw,
                        baseUrl = "https://library.oapen.org",
                        sourceType = BookSourceType.OAPEN,
                        existingOfflineIds = emptySet(),
                        existingLibraryIds = existingIds
                    )
                }.getOrDefault(emptyList())
            }

            val doabDeferred = async {
                runCatching {
                    val raw = FreeBookNetworkClient.doabApi
                        .searchRaw(query = "civil engineering OR hydraulics OR concrete OR geotechnical OR construction OR mechanical engineering OR electrical engineering OR calculus", limit = 16)
                        .string()
                    parseDSpaceSearchResults(
                        rawJson = raw,
                        baseUrl = "https://directory.doabooks.org",
                        sourceType = BookSourceType.DOAB,
                        existingOfflineIds = emptySet(),
                        existingLibraryIds = existingIds
                    )
                }.getOrDefault(emptyList())
            }

            val allDiscovered = (iaDeferreds.flatMap { it.await() } +
                oapenDeferred.await() +
                doabDeferred.await())
                .distinctBy { it.id }
                .filter { it.id !in existingIds }

            allDiscovered.forEach { item ->
                fetchedEntities.add(item.toCatalogBookEntity())
            }
        }

        if (fetchedEntities.isNotEmpty()) {
            dao.insertBooksIgnore(fetchedEntities)
        }
        fetchedEntities.size
    }

    fun observeBookById(bookId: String): Flow<BookEntity?> = dao.observeBookById(bookId)

    fun observePagesForBook(bookId: String): Flow<List<BookPageEntity>> = dao.observePagesForBook(bookId)

    fun observeBookmarksForBook(bookId: String): Flow<List<BookmarkEntity>> = dao.observeBookmarksForBook(bookId)

    suspend fun recordReadingProgress(bookId: String, pageNumber: Int) = withContext(Dispatchers.IO) {
        dao.updateReadingProgress(
            bookId = bookId,
            pageNumber = pageNumber.coerceAtLeast(1),
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun toggleBookmark(
        bookId: String,
        bookTitle: String,
        pageNumber: Int,
        chapterTitle: String
    ): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getBookmarkForPage(bookId, pageNumber)
        if (existing != null) {
            dao.deleteBookmarkForPage(bookId, pageNumber)
            false
        } else {
            dao.insertBookmark(
                BookmarkEntity(
                    bookId = bookId,
                    bookTitle = bookTitle,
                    pageNumber = pageNumber,
                    chapterTitle = chapterTitle
                )
            )
            true
        }
    }

    suspend fun saveReviewNote(
        bookId: String,
        bookTitle: String,
        pageNumber: Int,
        chapterTitle: String,
        noteText: String
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getBookmarkForPage(bookId, pageNumber)
        val trimmed = noteText.trim()
        if (existing != null) {
            dao.insertBookmark(
                existing.copy(
                    noteText = trimmed,
                    chapterTitle = chapterTitle,
                    createdAt = System.currentTimeMillis()
                )
            )
        } else {
            dao.insertBookmark(
                BookmarkEntity(
                    bookId = bookId,
                    bookTitle = bookTitle,
                    pageNumber = pageNumber,
                    chapterTitle = chapterTitle,
                    noteText = trimmed
                )
            )
        }
    }

    suspend fun deleteBookmark(bookmarkId: Long) = withContext(Dispatchers.IO) {
        dao.deleteBookmarkById(bookmarkId)
    }

    suspend fun removeOfflineDownload(book: BookEntity) = withContext(Dispatchers.IO) {
        book.localPdfPath?.let { path -> runCatching { File(path).delete() } }
        if (book.sourceType == BookSourceType.LOCAL_IMPORT.name) {
            book.localCoverPath?.let { path -> runCatching { File(path).delete() } }
            dao.deleteBookById(book.id)
        } else if (book.isCuratedStarter) {
            dao.clearDownloadedPdf(book.id)
        } else {
            book.localCoverPath?.let { path -> runCatching { File(path).delete() } }
            dao.deleteBookById(book.id)
        }
    }

    /**
     * Zero-permission Storage Access Framework import of a local PDF textbook from the user's device.
     */
    suspend fun importLocalPdfTextbook(
        uri: Uri,
        title: String,
        author: String,
        isbn: String,
        subject: CivilSubject
    ): Result<BookEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val bookId = "local_${UUID.randomUUID().toString().replace("-", "").take(12)}"
            val destPdf = PdfStorageHelper.pdfFileForBook(context, bookId)
            val destCover = PdfStorageHelper.coverFileForBook(context, bookId)

            PdfStorageHelper.copyUriToPrivatePdf(context, uri, destPdf).getOrThrow()
            val inspection = PdfStorageHelper.inspectPdfAndExtractCover(destPdf, destCover).getOrThrow()

            val cleanTitle = title.trim().ifBlank { PdfStorageHelper.resolveDisplayName(context, uri) }
            val cleanAuthor = author.trim().ifBlank { "Personal Reference" }
            val cleanIsbn = isbn.trim().ifBlank { "LOCAL-${bookId.takeLast(6).uppercase()}" }

            val entity = BookEntity(
                id = bookId,
                title = cleanTitle,
                author = cleanAuthor,
                isbn = cleanIsbn,
                subjectKey = subject.name,
                courseCode = subject.courseCode,
                publisher = "Device Imported PDF",
                publishYear = 2026,
                edition = "Full Local PDF",
                description = "Imported full-length PDF textbook (${inspection.pageCount} pages) saved in CivLib's offline vault for ${subject.courseCode} — ${subject.displayName}.",
                sourceType = BookSourceType.LOCAL_IMPORT.name,
                formatType = BookFormat.PDF.name,
                localCoverPath = inspection.coverPath,
                coverArtKey = subject.defaultCoverArtKey,
                isOfflineAvailable = true,
                isCuratedStarter = false,
                totalPages = inspection.pageCount,
                currentPage = 1,
                lastOpenedAt = System.currentTimeMillis(),
                fileSizeBytes = inspection.fileSizeBytes,
                localPdfPath = destPdf.absolutePath
            )
            dao.insertBook(entity)
            entity
        }
    }

    suspend fun searchAcrossRepositories(
        rawQuery: String,
        isOnline: Boolean
    ): CombinedSearchResults = withContext(Dispatchers.IO) {
        val query = rawQuery.trim()
        if (query.isEmpty()) {
            return@withContext CombinedSearchResults(emptyList(), emptyList(), usedNetwork = false)
        }

        val normalized = IsbnValidator.normalize(query)
        val localMatches = dao.searchLocalBooks(query, normalized)
        val allLocalIds = dao.getAllBookIds().toSet()
        val offlineIds = localMatches.filter { it.isOfflineAvailable }.map { it.id }.toSet()
        val localIsbns = localMatches.map { IsbnValidator.normalize(it.isbn) }.toSet()

        if (!isOnline) {
            return@withContext CombinedSearchResults(
                localMatches = localMatches,
                remoteResults = emptyList(),
                usedNetwork = false,
                errorMessage = "You are offline. Showing instant matches from your local Civil Engineering repository."
            )
        }

        val isIsbn = IsbnValidator.isIsbnQuery(query)
        val remoteList = mutableListOf<DiscoverBookResult>()
        var anySucceeded = false

        coroutineScope {
            val iaDeferred = async {
                runCatching {
                    val iaQuery = if (isIsbn) {
                        "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND isbn:$normalized"
                    } else {
                        "mediatype:texts AND format:\"Text PDF\" AND -access-restricted-item:true AND ($query)"
                    }
                    val raw = FreeBookNetworkClient.internetArchiveApi
                        .advancedSearchRaw(query = iaQuery, rows = 14)
                        .string()
                    parseInternetArchiveSearchResults(raw, offlineIds, allLocalIds)
                }
            }

            val oapenDeferred = async {
                runCatching {
                    val q = if (isIsbn) "dc.identifier.isbn:$normalized" else query
                    val raw = FreeBookNetworkClient.oapenApi.searchRaw(query = q, limit = 10).string()
                    parseDSpaceSearchResults(
                        rawJson = raw,
                        baseUrl = "https://library.oapen.org",
                        sourceType = BookSourceType.OAPEN,
                        existingOfflineIds = offlineIds,
                        existingLibraryIds = allLocalIds
                    )
                }
            }

            val doabDeferred = async {
                runCatching {
                    val q = if (isIsbn) "dc.identifier.isbn:$normalized" else query
                    val raw = FreeBookNetworkClient.doabApi.searchRaw(query = q, limit = 10).string()
                    parseDSpaceSearchResults(
                        rawJson = raw,
                        baseUrl = "https://directory.doabooks.org",
                        sourceType = BookSourceType.DOAB,
                        existingOfflineIds = offlineIds,
                        existingLibraryIds = allLocalIds
                    )
                }
            }

            val openLibDeferred = async {
                runCatching {
                    val response = if (isIsbn) {
                        FreeBookNetworkClient.openLibraryApi.searchByIsbn(normalized)
                    } else {
                        FreeBookNetworkClient.openLibraryApi.searchByQuery(query)
                    }
                    response.docs.mapNotNull { doc -> doc.toDiscoverResult(allLocalIds, offlineIds, localIsbns) }
                }
            }

            val gutenbergDeferred = async {
                if (isIsbn) {
                    Result.success(emptyList())
                } else {
                    runCatching {
                        val response = FreeBookNetworkClient.gutendexApi.searchBooks(query)
                        response.results.take(8).map { it.toDiscoverResult(allLocalIds, offlineIds) }
                    }
                }
            }

            listOf(
                iaDeferred.await(),
                oapenDeferred.await(),
                doabDeferred.await(),
                openLibDeferred.await(),
                gutenbergDeferred.await()
            ).forEach { res ->
                res.onSuccess { items ->
                    anySucceeded = true
                    remoteList.addAll(items)
                }
            }
        }

        CombinedSearchResults(
            localMatches = localMatches,
            remoteResults = remoteList.distinctBy { it.id },
            usedNetwork = true,
            errorMessage = if (!anySucceeded) {
                "Could not reach online repositories right now. Showing local library results."
            } else null
        )
    }

    /**
     * Downloads a real full-length PDF (or full Gutenberg text) for an existing catalog BookEntity.
     */
    suspend fun downloadBookEntityOffline(
        book: BookEntity,
        onProgress: (DownloadProgress) -> Unit
    ): Result<BookEntity> = withContext(Dispatchers.IO) {
        val resultModel = DiscoverBookResult(
            id = book.id,
            title = book.title,
            author = book.author,
            isbn = book.isbn,
            subject = CivilSubject.fromKey(book.subjectKey),
            publishYear = book.publishYear,
            publisher = book.publisher,
            description = book.description,
            coverUrl = book.coverUrl,
            sourceType = BookSourceType.entries.firstOrNull { it.name == book.sourceType }
                ?: BookSourceType.INTERNET_ARCHIVE,
            format = BookFormat.entries.firstOrNull { it.name == book.formatType } ?: BookFormat.PDF,
            externalKey = book.externalKey,
            archiveIdentifier = book.archiveIdentifier,
            pdfDownloadUrl = book.pdfDownloadUrl,
            fullTextUrl = book.fullTextUrl,
            estimatedPages = book.totalPages.takeIf { it > 0 },
            fileSizeBytes = book.fileSizeBytes,
            isAlreadyInLibrary = true,
            isOfflineDownloaded = book.isOfflineAvailable
        )
        downloadRemoteResultOffline(resultModel, preserveStarterFlag = book.isCuratedStarter, onProgress = onProgress)
    }

    /**
     * Resolves and streams a real full-length PDF from Internet Archive / OAPEN / DOAB / Open Library,
     * or downloads full Project Gutenberg text, saving it offline into Room + app-private storage.
     */
    suspend fun downloadRemoteResultOffline(
        result: DiscoverBookResult,
        preserveStarterFlag: Boolean = false,
        onProgress: (DownloadProgress) -> Unit
    ): Result<BookEntity> = withContext(Dispatchers.IO) {
        runCatching {
            onProgress(DownloadProgress(result.id, statusText = "Resolving full book link…"))

            if (result.sourceType == BookSourceType.GUTENBERG) {
                return@runCatching downloadGutenbergFullText(result, onProgress)
            }

            // Step 1: Resolve direct unrestricted PDF URL
            var resolvedUrl = result.pdfDownloadUrl
            var resolvedCover = result.coverUrl

            if (resolvedUrl.isNullOrBlank() && !result.archiveIdentifier.isNullOrBlank()) {
                val rawMeta = FreeBookNetworkClient.internetArchiveApi
                    .getItemMetadataRaw(result.archiveIdentifier)
                    .string()
                val resolved = resolveInternetArchivePdfFromMetadata(result.archiveIdentifier, rawMeta)
                if (resolved != null) {
                    resolvedUrl = resolved.downloadUrl
                    if (resolvedCover.isNullOrBlank()) resolvedCover = resolved.coverUrl
                }
            }

            if (resolvedUrl.isNullOrBlank() &&
                (result.sourceType == BookSourceType.OAPEN || result.sourceType == BookSourceType.DOAB) &&
                !result.externalKey.isNullOrBlank()
            ) {
                val parts = result.externalKey.split("/")
                if (parts.size == 2) {
                    val api = if (result.sourceType == BookSourceType.OAPEN) {
                        FreeBookNetworkClient.oapenApi
                    } else {
                        FreeBookNetworkClient.doabApi
                    }
                    val baseUrl = if (result.sourceType == BookSourceType.OAPEN) {
                        "https://library.oapen.org"
                    } else {
                        "https://directory.doabooks.org"
                    }
                    val rawHandle = api.getHandleRaw(parts[0], parts[1]).string()
                    val resolved = resolveDSpacePdfFromHandleJson(baseUrl, rawHandle)
                    if (resolved != null) {
                        resolvedUrl = resolved.downloadUrl
                        if (resolvedCover.isNullOrBlank()) resolvedCover = resolved.coverUrl
                    }
                }
            }

            // Step 2: Stream real full-length PDF to app-private offline storage
            if (!resolvedUrl.isNullOrBlank()) {
                val pdfFile = PdfStorageHelper.pdfFileForBook(context, result.id)
                val coverFile = PdfStorageHelper.coverFileForBook(context, result.id)

                val downloadAttempt = FreeBookNetworkClient.downloadPdfToFile(
                    url = resolvedUrl,
                    destinationFile = pdfFile,
                    onProgress = { dl, total ->
                        onProgress(
                            DownloadProgress(
                                bookId = result.id,
                                downloadedBytes = dl,
                                totalBytes = total,
                                statusText = "Downloading PDF…"
                            )
                        )
                    }
                )

                if (downloadAttempt.isSuccess) {
                    onProgress(DownloadProgress(result.id, statusText = "Indexing PDF pages…"))
                    val inspection = PdfStorageHelper.inspectPdfAndExtractCover(pdfFile, coverFile).getOrThrow()

                    val entity = BookEntity(
                        id = result.id,
                        title = result.title,
                        author = result.author,
                        isbn = result.isbn.ifBlank { "OPEN-${result.id.takeLast(6).uppercase()}" },
                        subjectKey = result.subject.name,
                        courseCode = result.subject.courseCode,
                        publisher = result.publisher.ifBlank { result.sourceType.displayLabel },
                        publishYear = result.publishYear ?: 2024,
                        edition = "Full Open-Access PDF",
                        description = result.description,
                        sourceType = result.sourceType.name,
                        formatType = BookFormat.PDF.name,
                        coverUrl = resolvedCover,
                        localCoverPath = inspection.coverPath,
                        coverArtKey = result.subject.defaultCoverArtKey,
                        isOfflineAvailable = true,
                        isCuratedStarter = preserveStarterFlag,
                        totalPages = inspection.pageCount,
                        currentPage = 1,
                        lastOpenedAt = System.currentTimeMillis(),
                        fileSizeBytes = inspection.fileSizeBytes,
                        localPdfPath = pdfFile.absolutePath,
                        pdfDownloadUrl = resolvedUrl,
                        archiveIdentifier = result.archiveIdentifier,
                        externalKey = result.externalKey
                    )
                    dao.insertBook(entity)
                    return@runCatching entity
                } else if (result.sourceType != BookSourceType.OPEN_LIBRARY) {
                    throw downloadAttempt.exceptionOrNull()
                        ?: IllegalStateException("PDF download failed")
                }
            }

            // Fallback strictly for Open Library catalog-only records without unrestricted PDFs
            if (result.sourceType == BookSourceType.OPEN_LIBRARY) {
                return@runCatching saveOpenLibraryStudyReference(result)
            }

            throw IllegalStateException("No unrestricted full PDF bitstream found for this title")
        }
    }

    private suspend fun downloadGutenbergFullText(
        result: DiscoverBookResult,
        onProgress: (DownloadProgress) -> Unit
    ): BookEntity {
        onProgress(DownloadProgress(result.id, statusText = "Downloading full public-domain text…"))
        val rawText = result.fullTextUrl?.let { FreeBookNetworkClient.downloadPlainText(it) }
            ?: throw IllegalStateException("Plain-text book URL not available")

        val cleaned = rawText
            .substringAfter("*** START OF", rawText)
            .substringAfter("\n\n", rawText)
            .substringBefore("*** END OF", rawText)
            .replace("\r\n", "\n")
            .replace(Regex("\n{3,}"), "\n\n")
            .trim()

        val paragraphs = cleaned.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
        val pages = mutableListOf<BookPageEntity>()
        val buffer = StringBuilder()
        var pageNum = 1

        for (para in paragraphs) {
            if (buffer.length + para.length > 1350 && buffer.isNotEmpty()) {
                pages.add(
                    BookPageEntity(
                        bookId = result.id,
                        pageNumber = pageNum,
                        chapterTitle = "Page $pageNum · ${result.title.take(36)}",
                        sectionLabel = "PROJECT GUTENBERG FULL TEXT",
                        contentBody = buffer.toString().trim()
                    )
                )
                buffer.clear()
                pageNum++
                if (pageNum > 350) break
            }
            if (buffer.isNotEmpty()) buffer.append("\n\n")
            buffer.append(para)
        }
        if (buffer.isNotEmpty() && pageNum <= 350) {
            pages.add(
                BookPageEntity(
                    bookId = result.id,
                    pageNumber = pageNum,
                    chapterTitle = "Page $pageNum · ${result.title.take(36)}",
                    sectionLabel = "PROJECT GUTENBERG FULL TEXT",
                    contentBody = buffer.toString().trim()
                )
            )
        }

        val entity = BookEntity(
            id = result.id,
            title = result.title,
            author = result.author,
            isbn = result.isbn,
            subjectKey = result.subject.name,
            courseCode = result.subject.courseCode,
            publisher = "Project Gutenberg",
            publishYear = result.publishYear ?: 1920,
            edition = "Public Domain Full Text",
            description = result.description,
            sourceType = BookSourceType.GUTENBERG.name,
            formatType = BookFormat.TEXT.name,
            coverUrl = result.coverUrl,
            coverArtKey = result.subject.defaultCoverArtKey,
            isOfflineAvailable = true,
            isCuratedStarter = false,
            totalPages = pages.size,
            currentPage = 1,
            lastOpenedAt = System.currentTimeMillis(),
            fileSizeBytes = cleaned.length.toLong(),
            fullTextUrl = result.fullTextUrl
        )
        dao.insertBook(entity)
        dao.deletePagesForBook(entity.id)
        dao.insertPages(pages)
        return entity
    }

    private suspend fun saveOpenLibraryStudyReference(result: DiscoverBookResult): BookEntity {
        val workId = result.externalKey?.removePrefix("/works/")?.trim()
        val summary = if (!workId.isNullOrBlank()) {
            runCatching {
                val raw = FreeBookNetworkClient.openLibraryApi.getWorkDetailRaw(workId).string()
                parseOpenLibraryWorkSummary(raw)
            }.getOrNull()
        } else null

        val desc = summary?.description?.takeIf { it.isNotBlank() } ?: result.description
        val topics = summary?.subjects?.take(14).orEmpty()

        val pages = buildList {
            add(
                BookPageEntity(
                    bookId = result.id,
                    pageNumber = 1,
                    chapterTitle = "Open Library Catalog Record",
                    sectionLabel = "ISBN REFERENCE DOSSIER",
                    contentBody = buildString {
                        append(desc.ifBlank { "${result.title} by ${result.author}." })
                        append("\n\nNote: This specific ISBN does not host an unrestricted open-access PDF on Internet Archive. You can import your own PDF copy via '+ Import PDF' on the Library screen or search Internet Archive / OAPEN for full open-access PDFs.")
                    },
                    formulaBlock = "ISBN: ${result.isbn.ifBlank { "N/A" }}\nSubject: ${result.subject.courseCode} · ${result.subject.displayName}\nPublisher: ${result.publisher}"
                )
            )
            if (topics.isNotEmpty()) {
                add(
                    BookPageEntity(
                        bookId = result.id,
                        pageNumber = 2,
                        chapterTitle = "Indexed Engineering Subjects",
                        sectionLabel = "CURRICULUM TAGS",
                        contentBody = topics.joinToString("\n") { "• $it" }
                    )
                )
            }
        }

        val entity = BookEntity(
            id = result.id,
            title = result.title,
            author = result.author,
            isbn = result.isbn.ifBlank { "OL-${result.id.takeLast(6).uppercase()}" },
            subjectKey = result.subject.name,
            courseCode = result.subject.courseCode,
            publisher = result.publisher,
            publishYear = result.publishYear ?: 2024,
            edition = "Open Library Record",
            description = desc,
            sourceType = BookSourceType.OPEN_LIBRARY.name,
            formatType = BookFormat.TEXT.name,
            coverUrl = result.coverUrl,
            coverArtKey = result.subject.defaultCoverArtKey,
            isOfflineAvailable = true,
            totalPages = pages.size,
            currentPage = 1,
            lastOpenedAt = System.currentTimeMillis(),
            externalKey = result.externalKey,
            archiveIdentifier = result.archiveIdentifier
        )
        dao.insertBook(entity)
        dao.deletePagesForBook(entity.id)
        dao.insertPages(pages)
        return entity
    }

    private fun DiscoverBookResult.toCatalogBookEntity(): BookEntity {
        return BookEntity(
            id = id,
            title = title,
            author = author,
            isbn = isbn.ifBlank { "OPEN-${id.takeLast(6).uppercase()}" },
            subjectKey = subject.name,
            courseCode = subject.courseCode,
            publisher = publisher.ifBlank { sourceType.displayLabel },
            publishYear = publishYear ?: 2024,
            edition = if (format == BookFormat.PDF) "Full Open PDF" else "Public Domain Text",
            description = description,
            sourceType = sourceType.name,
            formatType = format.name,
            coverUrl = coverUrl,
            coverArtKey = subject.defaultCoverArtKey,
            isOfflineAvailable = false,
            isCuratedStarter = false,
            totalPages = estimatedPages ?: 0,
            fileSizeBytes = fileSizeBytes,
            pdfDownloadUrl = pdfDownloadUrl,
            archiveIdentifier = archiveIdentifier,
            externalKey = externalKey,
            fullTextUrl = fullTextUrl
        )
    }

    private fun OpenLibraryDoc.toDiscoverResult(
        allLocalIds: Set<String>,
        offlineIds: Set<String>,
        localIsbns: Set<String>
    ): DiscoverBookResult? {
        val safeTitle = title?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val safeAuthor = authorName?.firstOrNull()?.trim().orEmpty().ifBlank { "Unknown Author" }
        val chosenIsbn = isbn?.firstOrNull { it.length == 13 }
            ?: isbn?.firstOrNull { it.length == 10 }
            ?: isbn?.firstOrNull().orEmpty()
        val workKey = key ?: "/works/${safeTitle.hashCode()}"
        val id = "ol_${workKey.replace("/", "_")}"
        val subjectText = (subject.orEmpty().joinToString(" ") + " " + safeTitle)
        val inferredSubject = CivilSubject.inferFromText(subjectText)
        val coverUrl = coverId?.let { "https://covers.openlibrary.org/b/id/$it-L.jpg" }
        val iaId = internetArchiveIds?.firstOrNull()
        val alreadySaved = id in allLocalIds ||
            (chosenIsbn.isNotBlank() && IsbnValidator.normalize(chosenIsbn) in localIsbns)

        return DiscoverBookResult(
            id = id,
            title = safeTitle,
            author = safeAuthor,
            isbn = chosenIsbn,
            subject = inferredSubject,
            publishYear = firstPublishYear,
            publisher = publisher?.firstOrNull().orEmpty().ifBlank { "Open Library" },
            description = firstSentence?.firstOrNull()
                ?: "Open Library ISBN catalog entry in ${inferredSubject.displayName}.",
            coverUrl = coverUrl,
            sourceType = BookSourceType.OPEN_LIBRARY,
            format = if (iaId != null) BookFormat.PDF else BookFormat.TEXT,
            externalKey = workKey,
            archiveIdentifier = iaId,
            estimatedPages = medianPages,
            isAlreadyInLibrary = alreadySaved,
            isOfflineDownloaded = id in offlineIds
        )
    }

    private fun GutendexBook.toDiscoverResult(
        allLocalIds: Set<String>,
        offlineIds: Set<String>
    ): DiscoverBookResult {
        val id = "pg_$id"
        val authorName = authors.firstOrNull()?.name ?: "Public Domain Author"
        val subjectText = (subjects + bookshelves + title).joinToString(" ")
        val inferredSubject = CivilSubject.inferFromText(subjectText)
        val plainTextUrl = formats.entries.firstOrNull { (mime, url) ->
            mime.startsWith("text/plain") && !url.endsWith(".zip")
        }?.value
        val coverUrl = formats["image/jpeg"]

        return DiscoverBookResult(
            id = id,
            title = title,
            author = authorName,
            isbn = "PG-$id",
            subject = inferredSubject,
            publishYear = null,
            publisher = "Project Gutenberg",
            description = subjects.take(3).joinToString(" · ").ifBlank {
                "Free public-domain full-length book via Project Gutenberg."
            },
            coverUrl = coverUrl,
            sourceType = BookSourceType.GUTENBERG,
            format = BookFormat.TEXT,
            externalKey = "/gutenberg/$id",
            fullTextUrl = plainTextUrl,
            isAlreadyInLibrary = id in allLocalIds,
            isOfflineDownloaded = id in offlineIds
        )
    }
}
