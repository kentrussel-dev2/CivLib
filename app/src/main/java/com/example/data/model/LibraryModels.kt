package com.example.data.model

enum class LibrarySortMode(val label: String) {
    BY_SUBJECT("Subject"),
    BY_TITLE("Title"),
    BY_AUTHOR("Author")
}

enum class LibraryViewMode(val label: String) {
    BOOKSHELF("Shelf"),
    CATALOG("Catalog")
}

enum class SearchSourceFilter(val label: String) {
    ALL("All Sources"),
    LOCAL("Offline Vault"),
    INTERNET_ARCHIVE("Internet Archive"),
    OAPEN_DOAB("OAPEN & DOAB"),
    OPEN_LIBRARY("Open Library"),
    GUTENBERG("Project Gutenberg")
}

enum class PaperTheme(val label: String) {
    PARCHMENT("Book Paper"),
    CRISP("Original"),
    BLUEPRINT_DARK("Night")
}

enum class BookReadingLayout(val label: String) {
    AUTO("Auto"),
    SINGLE_PAGE("Single"),
    TWO_PAGE_SPREAD("Spread")
}

enum class BookFormat {
    PDF,
    TEXT
}

enum class BookSourceType(val displayLabel: String) {
    INTERNET_ARCHIVE("Internet Archive PDF"),
    OAPEN("OAPEN Open Access"),
    DOAB("DOAB Peer-Reviewed"),
    OPEN_LIBRARY("Open Library"),
    GUTENBERG("Project Gutenberg"),
    LOCAL_IMPORT("Imported PDF")
}

data class DownloadProgress(
    val bookId: String,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = -1L,
    val statusText: String = "Connecting…"
) {
    val fraction: Float?
        get() = if (totalBytes > 0L) {
            (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        } else {
            null
        }

    val formattedSize: String
        get() {
            val dlMb = downloadedBytes / (1024.0 * 1024.0)
            return if (totalBytes > 0L) {
                val totMb = totalBytes / (1024.0 * 1024.0)
                String.format("%.1f / %.1f MB", dlMb, totMb)
            } else if (downloadedBytes > 0L) {
                String.format("%.1f MB", dlMb)
            } else {
                statusText
            }
        }
}

data class DiscoverBookResult(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val subject: CivilSubject,
    val publishYear: Int?,
    val publisher: String,
    val description: String,
    val coverUrl: String?,
    val sourceType: BookSourceType,
    val format: BookFormat = BookFormat.PDF,
    val externalKey: String? = null,
    val archiveIdentifier: String? = null,
    val pdfDownloadUrl: String? = null,
    val fullTextUrl: String? = null,
    val estimatedPages: Int? = null,
    val fileSizeBytes: Long = 0L,
    val isAlreadyInLibrary: Boolean = false,
    val isOfflineDownloaded: Boolean = false
)

object IsbnValidator {
    fun normalize(input: String): String {
        return input.replace("-", "").replace(" ", "").trim().uppercase()
    }

    fun isIsbnQuery(input: String): Boolean {
        val clean = normalize(input)
        return isValidIsbn10(clean) || isValidIsbn13(clean)
    }

    fun detectFormatLabel(input: String): String? {
        val clean = normalize(input)
        return when {
            isValidIsbn13(clean) -> "ISBN-13 Detected"
            isValidIsbn10(clean) -> "ISBN-10 Detected"
            clean.length in 7..13 && clean.all { it.isDigit() || it == 'X' } -> "ISBN Lookup"
            else -> null
        }
    }

    private fun isValidIsbn10(clean: String): Boolean {
        return clean.length == 10 && clean.substring(0, 9).all { it.isDigit() } &&
            (clean[9].isDigit() || clean[9] == 'X')
    }

    private fun isValidIsbn13(clean: String): Boolean {
        return clean.length == 13 && clean.all { it.isDigit() } &&
            (clean.startsWith("978") || clean.startsWith("979"))
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return ""
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) String.format("%.1f MB", mb) else String.format("%.0f KB", kb)
}
