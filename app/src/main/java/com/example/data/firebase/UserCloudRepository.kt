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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

private val VALID_DOC_ID_REGEX = Regex("[^a-zA-Z0-9_\\-.]")

fun sanitizeFirestoreDocId(raw: String): String {
    val cleaned = raw.replace(VALID_DOC_ID_REGEX, "_").take(120)
    return cleaned.ifBlank { "item_${raw.hashCode().toUInt()}" }
}

class UserCloudRepository(
    private val db: FirebaseFirestore?,
    private val auth: FirebaseAuth? = safeFirebaseAuthOrNull(),
    private val appContext: Context? = null
) {
    private val prefs = appContext?.applicationContext
        ?.getSharedPreferences("civlib_student_profile_prefs", Context.MODE_PRIVATE)

    private val localProfileFlow = MutableStateFlow<UserProfile?>(loadPersistedLocalProfile())
    private val localPersonalBooksFlow = MutableStateFlow<List<PersonalBook>>(emptyList())
    private val localStudyNotesFlow = MutableStateFlow<List<CloudStudyNote>>(emptyList())

    constructor(context: Context) : this(
        db = try {
            FirebaseFirestore.getInstance(
                context.applicationContext.getString(R.string.firestore_database_id)
            )
        } catch (_: Exception) {
            null
        },
        auth = safeFirebaseAuthOrNull(),
        appContext = context.applicationContext
    )

    private fun loadPersistedLocalProfile(): UserProfile? {
        val sp = prefs ?: return null
        if (!sp.getBoolean("onboarding_completed", false)) return null
        val uid = sp.getString("user_id", "local_student") ?: "local_student"
        val name = sp.getString("display_name", "Civil Engineer") ?: "Civil Engineer"
        val email = sp.getString("email", null)
        val mantra = sp.getString("study_mantra", "BS Civil Engineering · Chronological Track")
            ?: "BS Civil Engineering · Chronological Track"
        val year = sp.getLong("curriculum_year", 1L).coerceIn(1L, 4L)
        val subjectKey = sp.getString("primary_subject_key", "MECHANICS") ?: "MECHANICS"
        val goal = sp.getString("study_goal", "Current Semester Courses") ?: "Current Semester Courses"
        return UserProfile(
            id = uid,
            userId = uid,
            displayName = name,
            email = email,
            studyMantra = mantra,
            curriculumYear = year,
            primarySubjectKey = subjectKey,
            studyGoal = goal,
            onboardingCompleted = true
        )
    }

    private fun persistLocalProfile(profile: UserProfile?) {
        val sp = prefs ?: return
        if (profile == null) {
            sp.edit().putBoolean("onboarding_completed", false).apply()
            return
        }
        sp.edit()
            .putBoolean("onboarding_completed", profile.onboardingCompleted)
            .putString("user_id", profile.userId)
            .putString("display_name", profile.displayName)
            .putString("email", profile.email)
            .putString("study_mantra", profile.studyMantra)
            .putLong("curriculum_year", profile.curriculumYear)
            .putString("primary_subject_key", profile.primarySubjectKey)
            .putString("study_goal", profile.studyGoal)
            .apply()
    }

    fun isLocalOnboardingCompleted(): Boolean {
        return prefs?.getBoolean("onboarding_completed", false) == true || localProfileFlow.value != null
    }

    fun clearLocalSession() {
        localProfileFlow.value = null
        prefs?.edit()?.putBoolean("onboarding_completed", false)?.apply()
    }

    private fun requireVerifiedUserId(expectedUid: String): String {
        if (auth == null) return expectedUid
        val currentUid = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        check(currentUid == expectedUid) { "Signed-in account changed; refusing cross-user write." }
        return currentUid
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val firestore = db
        if (firestore == null) {
            emitAll(localProfileFlow)
            return@flow
        }
        val docRef = firestore.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) localProfileFlow.value
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
                    if (exceptionToLog != null && auth?.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    fun observePersonalBooks(userId: String): Flow<List<PersonalBook>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val firestore = db
        if (firestore == null) {
            emitAll(localPersonalBooksFlow)
            return@flow
        }
        val colRef = firestore.collection("users").document(userId).collection("personal_books")
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
                    if (exceptionToLog != null && auth?.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeStudyNotes(userId: String): Flow<List<CloudStudyNote>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank" }
        val firestore = db
        if (firestore == null) {
            emitAll(localStudyNotesFlow)
            return@flow
        }
        val colRef = firestore.collection("users").document(userId).collection("study_notes")
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
                    if (exceptionToLog != null && auth?.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(user: FirebaseUser?): Result<Unit> {
        val uid = user?.uid ?: "local_student"
        val firestore = db
        if (firestore == null || user == null) {
            val existing = localProfileFlow.value ?: loadPersistedLocalProfile()
            if (existing != null) {
                localProfileFlow.value = existing
            }
            return Result.success(Unit)
        }
        val docRef = firestore.collection("users").document(uid)
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

    suspend fun saveProfileOnboardingSurvey(
        userId: String,
        displayName: String,
        email: String?,
        curriculumYear: Int,
        primarySubjectKey: String,
        studyGoal: String,
        studyMantra: String
    ): Result<UserProfile> {
        val cleanName = displayName.trim().ifBlank { "Civil Engineer" }.take(80)
        val cleanEmail = email?.trim()?.takeIf { it.isNotBlank() }?.take(120)
        val clampedYear = curriculumYear.coerceIn(1, 5).toLong()
        val cleanSubject = primarySubjectKey.trim().ifBlank { "MECHANICS" }.take(40)
        val cleanGoal = studyGoal.trim().ifBlank { "Current Semester Courses" }.take(80)
        val cleanMantra = studyMantra.trim().ifBlank { "BS Civil Engineering · $cleanGoal" }.take(140)

        val updatedProfile = UserProfile(
            id = userId,
            userId = userId,
            displayName = cleanName,
            email = cleanEmail,
            studyMantra = cleanMantra,
            curriculumYear = clampedYear,
            primarySubjectKey = cleanSubject,
            studyGoal = cleanGoal,
            onboardingCompleted = true
        )
        localProfileFlow.value = updatedProfile
        persistLocalProfile(updatedProfile)

        val firestore = db
        if (firestore != null && auth?.currentUser != null) {
            val docRef = firestore.collection("users").document(userId)
            try {
                requireVerifiedUserId(userId)
                val payload = buildMap<String, Any> {
                    put("userId", userId)
                    put("displayName", cleanName)
                    if (cleanEmail != null) put("email", cleanEmail)
                    put("studyMantra", cleanMantra)
                    put("curriculumYear", clampedYear)
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.WRITE, docRef.path)
            }
        }
        return Result.success(updatedProfile)
    }

    suspend fun updateProfilePreferences(
        userId: String,
        studyMantra: String,
        curriculumYear: Int,
        displayName: String? = null,
        primarySubjectKey: String? = null,
        studyGoal: String? = null
    ): Result<Unit> {
        val cleanMantra = studyMantra.trim().take(140)
        val clampedYear = curriculumYear.coerceIn(1, 5).toLong()
        val current = localProfileFlow.value ?: loadPersistedLocalProfile() ?: UserProfile(
            id = userId,
            userId = userId,
            displayName = displayName?.takeIf { it.isNotBlank() } ?: "Civil Engineer"
        )
        val nextLocal = current.copy(
            displayName = displayName?.trim()?.takeIf { it.isNotBlank() } ?: current.displayName,
            studyMantra = cleanMantra,
            curriculumYear = clampedYear,
            primarySubjectKey = primarySubjectKey?.takeIf { it.isNotBlank() } ?: current.primarySubjectKey,
            studyGoal = studyGoal?.takeIf { it.isNotBlank() } ?: current.studyGoal,
            onboardingCompleted = true
        )
        localProfileFlow.value = nextLocal
        persistLocalProfile(nextLocal)

        val firestore = db
        if (firestore == null || auth?.currentUser == null) {
            return Result.success(Unit)
        }
        val docRef = firestore.collection("users").document(userId)
        return try {
            requireVerifiedUserId(userId)
            docRef.update(
                mapOf(
                    "displayName" to nextLocal.displayName,
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
        val nextBookmarked = !(existingRecord?.bookmarked ?: false)
        val firestore = db
        if (firestore == null) {
            localPersonalBooksFlow.update { list ->
                val filtered = list.filterNot { it.bookId == docId }
                filtered + PersonalBook(
                    id = docId,
                    userId = userId,
                    bookId = docId,
                    title = book.title,
                    author = book.author,
                    subjectKey = book.subjectKey,
                    courseCode = book.courseCode,
                    coverUrl = book.coverUrl,
                    coverArtKey = book.coverArtKey,
                    totalPages = book.totalPages.toLong(),
                    currentPage = book.currentPage.toLong(),
                    bookmarked = nextBookmarked,
                    completed = existingRecord?.completed ?: false,
                    lastOpenedAt = book.lastOpenedAt
                )
            }
            return Result.success(nextBookmarked)
        }
        val docRef = firestore.collection("users").document(userId)
            .collection("personal_books").document(docId)
        return try {
            requireVerifiedUserId(userId)
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
        val nextCompleted = !(existingRecord?.completed ?: false)
        val firestore = db
        if (firestore == null) {
            localPersonalBooksFlow.update { list ->
                val filtered = list.filterNot { it.bookId == docId }
                filtered + PersonalBook(
                    id = docId,
                    userId = userId,
                    bookId = docId,
                    title = book.title,
                    author = book.author,
                    subjectKey = book.subjectKey,
                    courseCode = book.courseCode,
                    coverUrl = book.coverUrl,
                    coverArtKey = book.coverArtKey,
                    totalPages = book.totalPages.toLong(),
                    currentPage = book.currentPage.toLong(),
                    bookmarked = existingRecord?.bookmarked ?: true,
                    completed = nextCompleted,
                    lastOpenedAt = book.lastOpenedAt
                )
            }
            return Result.success(nextCompleted)
        }
        val docRef = firestore.collection("users").document(userId)
            .collection("personal_books").document(docId)
        return try {
            requireVerifiedUserId(userId)
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
        val clampedPage = pageNumber.coerceIn(1, 50000).toLong()
        val clampedTotal = book.totalPages.coerceIn(0, 50000).toLong()
        val nowMs = System.currentTimeMillis()
        val firestore = db
        if (firestore == null) {
            localPersonalBooksFlow.update { list ->
                val filtered = list.filterNot { it.bookId == docId }
                filtered + PersonalBook(
                    id = docId,
                    userId = userId,
                    bookId = docId,
                    title = book.title,
                    author = book.author,
                    subjectKey = book.subjectKey,
                    courseCode = book.courseCode,
                    coverUrl = book.coverUrl,
                    coverArtKey = book.coverArtKey,
                    totalPages = clampedTotal,
                    currentPage = clampedPage,
                    bookmarked = existingRecord?.bookmarked ?: false,
                    completed = existingRecord?.completed ?: false,
                    lastOpenedAt = nowMs
                )
            }
            return Result.success(Unit)
        }
        val docRef = firestore.collection("users").document(userId)
            .collection("personal_books").document(docId)
        return try {
            requireVerifiedUserId(userId)
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
        val cleanTitle = bookTitle.trim().ifBlank { "Civil Engineering Volume" }.take(240)
        val cleanChapter = chapterTitle.trim().take(180)
        val cleanNote = noteText.trim().take(2000)
        val clampedPage = pageNumber.coerceIn(1, 50000).toLong()
        val firestore = db
        if (firestore == null) {
            localStudyNotesFlow.update { list ->
                val filtered = list.filterNot { it.id == docId }
                filtered + CloudStudyNote(
                    id = docId,
                    userId = userId,
                    bookId = sanitizeFirestoreDocId(bookId),
                    bookTitle = cleanTitle,
                    pageNumber = clampedPage,
                    chapterTitle = cleanChapter,
                    noteText = cleanNote
                )
            }
            return Result.success(Unit)
        }
        val docRef = firestore.collection("users").document(userId)
            .collection("study_notes").document(docId)
        return try {
            requireVerifiedUserId(userId)
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
        val firestore = db
        if (firestore == null) {
            localStudyNotesFlow.update { list -> list.filterNot { it.id == safeDocId } }
            return Result.success(Unit)
        }
        val docRef = firestore.collection("users").document(userId)
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
