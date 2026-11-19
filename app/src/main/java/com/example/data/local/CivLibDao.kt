package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CivLibDao {
    @Query("SELECT * FROM books ORDER BY isOfflineAvailable DESC, subjectKey ASC, title ASC")
    fun observeAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isOfflineAvailable = 1 ORDER BY lastOpenedAt DESC, title ASC")
    fun observeOfflineBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE lastOpenedAt > 0 AND isOfflineAvailable = 1 ORDER BY lastOpenedAt DESC LIMIT 1")
    fun observeLastOpenedBook(): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    fun observeBookById(bookId: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    suspend fun getBookById(bookId: String): BookEntity?

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int

    @Query("SELECT id FROM books")
    suspend fun getAllBookIds(): List<String>

    @Query(
        """
        SELECT * FROM books
        WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(author) LIKE '%' || LOWER(:query) || '%'
           OR REPLACE(REPLACE(LOWER(isbn), '-', ''), ' ', '') LIKE '%' || LOWER(:normalizedQuery) || '%'
           OR LOWER(courseCode) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(subjectKey) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(description) LIKE '%' || LOWER(:query) || '%'
        ORDER BY isOfflineAvailable DESC, title ASC
        """
    )
    suspend fun searchLocalBooks(query: String, normalizedQuery: String): List<BookEntity>

    @Query("SELECT * FROM book_pages WHERE bookId = :bookId ORDER BY pageNumber ASC")
    fun observePagesForBook(bookId: String): Flow<List<BookPageEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBooksIgnore(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<BookPageEntity>)

    @Query("DELETE FROM book_pages WHERE bookId = :bookId")
    suspend fun deletePagesForBook(bookId: String)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookById(bookId: String)

    @Query("UPDATE books SET currentPage = :pageNumber, lastOpenedAt = :timestamp WHERE id = :bookId")
    suspend fun updateReadingProgress(bookId: String, pageNumber: Int, timestamp: Long)

    @Query(
        """
        UPDATE books
        SET isOfflineAvailable = 1,
            localPdfPath = :localPdfPath,
            localCoverPath = :localCoverPath,
            totalPages = :totalPages,
            fileSizeBytes = :fileSizeBytes,
            formatType = 'PDF'
        WHERE id = :bookId
        """
    )
    suspend fun markPdfDownloaded(
        bookId: String,
        localPdfPath: String,
        localCoverPath: String?,
        totalPages: Int,
        fileSizeBytes: Long
    )

    @Query(
        """
        UPDATE books
        SET isOfflineAvailable = 0,
            localPdfPath = NULL,
            fileSizeBytes = 0
        WHERE id = :bookId
        """
    )
    suspend fun clearDownloadedPdf(bookId: String)

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY pageNumber ASC")
    fun observeBookmarksForBook(bookId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId AND pageNumber = :pageNumber LIMIT 1")
    suspend fun getBookmarkForPage(bookId: String, pageNumber: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId AND pageNumber = :pageNumber")
    suspend fun deleteBookmarkForPage(bookId: String, pageNumber: Int)
}
