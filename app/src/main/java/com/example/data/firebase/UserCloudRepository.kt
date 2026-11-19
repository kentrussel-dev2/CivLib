package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.local.BookEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private val VALID_DOC_ID_REGEX = Regex("[^a-zA-Z0-9_\\-.]")

fun sanitizeFirestoreDocId(raw: String): String {
    val cleaned = raw.replace(VALID_DOC_ID_REGEX, "_").take(120)
    return cleaned.ifBlank { "item_${raw.hashCode().toUInt()}" }
}

class UserCloudRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireVerifiedUserId(expectedUid: String): String {
        val currentUid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        check(currentUid == expectedUid) { "Signed-in account changed; refusing cross-user write." }
        return currentUid
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    fun observePersonalBooks(userId: String): Flow<List<PersonalBook>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val colRef = db.collection("users").document(userId).collection("personal_books")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        PersonalBook::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeStudyNotes(userId: String): Flow<List<CloudStudyNote>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val colRef = db.collection("users").document(userId).collection("study_notes")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CloudStudyNote::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(user: FirebaseUser): Result<Unit> {
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        return try {
            requireVerifiedUserId(uid)
            val snap = docRef.get().await()
            val safeName = (user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@")
                ?: "Civil Engineer").take(80)
            val safeEmail = user.email?.takeIf { it.isNotBlank() }?.take(120)
            val safePhoto = user.photoUrl?.toString()?.takeIf { it.isNotBlank() }?.take(500)

            if (!snap.exists()) {
                val createPayload = buildMap<String, Any> {
                    put("userId", uid)
                    put("displayName", safeName)
                    if (safeEmail != null) put("email", safeEmail)
                    if (safePhoto != null) put("photoUrl", safePhoto)
                    put("studyMantra", "BS Civil Engineering · Chronological Track")
                    put("curriculumYear", 1L)
                    put("createdAt", FieldValue.serverTimestamp())
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.set(createPayload).await()
            } else {
                val updatePayload = buildMap<String, Any> {
                    put("displayName", safeName)
                    if (safeEmail != null) put("email", safeEmail)
                    if (safePhoto != null) put("photoUrl", safePhoto)
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateProfilePreferences(
        userId: String,
        studyMantra: String,
        curriculumYear: Int
    ): Result<Unit> {
        val docRef = db.collection("users").document(userId)
        return try {
            requireVerifiedUserId(userId)
            val cleanMantra = studyMantra.trim().take(140)
            val clampedYear = curriculumYear.coerceIn(1, 5).toLong()
            docRef.update(
                mapOf(
                    "studyMantra" to cleanMantra,
                    "curriculumYear" to clampedYear,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun togglePersonalBookBookmark(
        userId: String,
        book: BookEntity,
        existingRecord: PersonalBook?
    ): Result<Boolean> {
        val docId = sanitizeFirestoreDocId(book.id)
        val docRef = db.collection("users").document(userId)
            .collection("personal_books").document(docId)
        return try {
            requireVerifiedUserId(userId)
            val nextBookmarked = !(existingRecord?.bookmarked ?: false)
            if (existingRecord == null) {
                val payload = buildPersonalBookCreateMap(
                    userId = userId,
                    docId = docId,
                    book = book,
                    bookmarked = nextBookmarked,
                    completed = false
                )
                docRef.set(payload).await()
            } else {
                docRef.update(
                    mapOf(
                        "bookmarked" to nextBookmarked,
                        "completed" to existingRecord.completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(nextBookmarked)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun togglePersonalBookCompleted(
        userId: String,
        book: BookEntity,
        existingRecord: PersonalBook?
    ): Result<Boolean> {
        val docId = sanitizeFirestoreDocId(book.id)
        val docRef = db.collection("users").document(userId)
            .collection("personal_books").document(docId)
        return try {
            requireVerifiedUserId(userId)
            val nextCompleted = !(existingRecord?.completed ?: false)
            if (existingRecord == null) {
                val payload = buildPersonalBookCreateMap(
                    userId = userId,
                    docId = docId,
                    book = book,
                    bookmarked = true,
                    completed = nextCompleted
                )
                docRef.set(payload).await()
            } else {
                docRef.update(
                    mapOf(
                        "bookmarked" to existingRecord.bookmarked,
                        "completed" to nextCompleted,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(nextCompleted)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun syncReadingProgress(
        userId: String,
        book: BookEntity,
        pageNumber: Int,
        existingRecord: PersonalBook?
    ): Result<Unit> {
        val docId = sanitizeFirestoreDocId(book.id)
        val docRef = db.collection("users").document(userId)
            .collection("personal_books").document(docId)
        return try {
            requireVerifiedUserId(userId)
            val clampedPage = pageNumber.coerceIn(1, 50000).toLong()
            val clampedTotal = book.totalPages.coerceIn(0, 50000).toLong()
            val nowMs = System.currentTimeMillis()
            if (existingRecord == null) {
                val payload = buildPersonalBookCreateMap(
                    userId = userId,
                    docId = docId,
                    book = book.copy(currentPage = clampedPage.toInt(), lastOpenedAt = nowMs),
                    bookmarked = false,
                    completed = false
                )
                docRef.set(payload).await()
            } else {
                docRef.update(
                    mapOf(
                        "currentPage" to clampedPage,
                        "totalPages" to clampedTotal,
                        "lastOpenedAt" to nowMs,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun upsertStudyNote(
        userId: String,
        bookId: String,
        bookTitle: String,
        pageNumber: Int,
        chapterTitle: String,
        noteText: String
    ): Result<Unit> {
        val docId = sanitizeFirestoreDocId("${bookId}_p${pageNumber}")
        val docRef = db.collection("users").document(userId)
            .collection("study_notes").document(docId)
        return try {
            requireVerifiedUserId(userId)
            val cleanTitle = bookTitle.trim().ifBlank { "Civil Engineering Volume" }.take(240)
            val cleanChapter = chapterTitle.trim().take(180)
            val cleanNote = noteText.trim().take(2000)
            val clampedPage = pageNumber.coerceIn(1, 50000).toLong()
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(
                    mapOf(
                        "userId" to userId,
                        "bookId" to sanitizeFirestoreDocId(bookId),
                        "bookTitle" to cleanTitle,
                        "pageNumber" to clampedPage,
                        "chapterTitle" to cleanChapter,
                        "noteText" to cleanNote,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "bookTitle" to cleanTitle,
                        "pageNumber" to clampedPage,
                        "chapterTitle" to cleanChapter,
                        "noteText" to cleanNote,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteStudyNote(userId: String, noteDocId: String): Result<Unit> {
        val safeDocId = sanitizeFirestoreDocId(noteDocId)
        val docRef = db.collection("users").document(userId)
            .collection("study_notes").document(safeDocId)
        return try {
            requireVerifiedUserId(userId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    private fun buildPersonalBookCreateMap(
        userId: String,
        docId: String,
        book: BookEntity,
        bookmarked: Boolean,
        completed: Boolean
    ): Map<String, Any> {
        val safeTitle = book.title.trim().ifBlank { "Untitled Volume" }.take(240)
        val safeAuthor = book.author.trim().take(180)
        val safeSubject = book.subjectKey.trim().ifBlank { "STRUCTURAL" }.take(40)
        val safeCourse = book.courseCode.trim().take(32)
        val safeCover = book.coverUrl?.trim()?.takeIf { it.isNotBlank() }?.take(500)
        val safeArtKey = book.coverArtKey.trim().ifBlank { "structural" }.take(40)
        return buildMap {
            put("userId", userId)
            put("bookId", docId)
            put("title", safeTitle)
            put("author", safeAuthor)
            put("subjectKey", safeSubject)
            put("courseCode", safeCourse)
            if (safeCover != null) put("coverUrl", safeCover)
            put("coverArtKey", safeArtKey)
            put("totalPages", book.totalPages.coerceIn(0, 50000).toLong())
            put("currentPage", book.currentPage.coerceIn(1, 50000).toLong())
            put("bookmarked", bookmarked)
            put("completed", completed)
            put("lastOpenedAt", book.lastOpenedAt.coerceAtLeast(0L))
            put("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }
    }
}
