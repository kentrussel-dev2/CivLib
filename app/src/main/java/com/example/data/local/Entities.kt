package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.BookFormat

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val subjectKey: String,
    val courseCode: String,
    val publisher: String,
    val publishYear: Int,
    val edition: String,
    val description: String,
    val sourceType: String,
    val formatType: String = BookFormat.PDF.name,
    val coverUrl: String? = null,
    val localCoverPath: String? = null,
    val coverArtKey: String = "structural",
    val isOfflineAvailable: Boolean = false,
    val isCuratedStarter: Boolean = false,
    val totalPages: Int = 0,
    val currentPage: Int = 1,
    val lastOpenedAt: Long = 0L,
    val fileSizeBytes: Long = 0L,
    val localPdfPath: String? = null,
    val pdfDownloadUrl: String? = null,
    val archiveIdentifier: String? = null,
    val externalKey: String? = null,
    val fullTextUrl: String? = null
)

@Entity(
    tableName = "book_pages",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class BookPageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val bookId: String,
    val pageNumber: Int,
    val chapterTitle: String,
    val sectionLabel: String,
    val contentBody: String,
    val formulaBlock: String? = null,
    val workedExample: String? = null,
    val keyTakeaway: String? = null
)

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId")]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val bookId: String,
    val bookTitle: String,
    val pageNumber: Int,
    val chapterTitle: String,
    val noteText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
