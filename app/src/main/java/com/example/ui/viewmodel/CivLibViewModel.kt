package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.CloudStudyNote
import com.example.data.firebase.CloudUiState
import com.example.data.firebase.PersonalBook
import com.example.data.firebase.UserCloudRepository
import com.example.data.firebase.UserProfile
import com.example.data.firebase.sanitizeFirestoreDocId
import com.example.data.local.BookEntity
import com.example.data.local.BookPageEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.CivLibDatabase
import com.example.data.local.ReaderPreferences
import com.example.data.local.ReaderPreferencesStore
import com.example.data.model.BookReadingLayout
import com.example.data.model.CivilSubject
import com.example.data.model.DiscoverBookResult
import com.example.data.model.DownloadProgress
import com.example.data.model.IsbnValidator
import com.example.data.model.LibrarySortMode
import com.example.data.model.LibraryViewMode
import com.example.data.model.PaperTheme
import com.example.data.model.SearchSourceFilter
import com.example.data.pdf.PdfStorageHelper
import com.example.data.repository.BookRepository
import com.example.data.util.NetworkMonitor
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryUiState(
    val books: List<BookEntity> = emptyList(),
    val filteredBooks: List<BookEntity> = emptyList(),
    val lastOpenedBook: BookEntity? = null,
    val selectedSubject: CivilSubject? = null,
    val sortMode: LibrarySortMode = LibrarySortMode.BY_SUBJECT,
    val viewMode: LibraryViewMode = LibraryViewMode.BOOKSHELF,
    val isOnline: Boolean = true,
    val isSyncingCatalog: Boolean = false,
    val offlineBookCount: Int = 0,
    val totalCatalogCount: Int = 0,
    val totalOfflineBytes: Long = 0L
)

data class SearchUiState(
    val query: String = "",
    val isbnBadgeLabel: String? = null,
    val sourceFilter: SearchSourceFilter = SearchSourceFilter.ALL,
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val localResults: List<BookEntity> = emptyList(),
    val remoteResults: List<DiscoverBookResult> = emptyList(),
    val statusMessage: String? = null
)

class CivLibViewModel(
    appContext: Context,
    private val cloudRepository: UserCloudRepository,
    private val currentUser: FirebaseUser
) : ViewModel() {

    private val currentUserId: String = currentUser.uid
    private val context = appContext.applicationContext
    private val database = CivLibDatabase.getInstance(context)
    val repository = BookRepository(context, database.civLibDao())
    val preferencesStore = ReaderPreferencesStore(context)
    private val networkMonitor = NetworkMonitor(context)

    private val _selectedSubject = MutableStateFlow<CivilSubject?>(null)
    private val _isSyncingCatalog = MutableStateFlow(false)
    private val _isImportingPdf = MutableStateFlow(false)
    val isImportingPdf: StateFlow<Boolean> = _isImportingPdf.asStateFlow()

    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    private val _downloadProgressMap = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloadProgressMap: StateFlow<Map<String, DownloadProgress>> = _downloadProgressMap.asStateFlow()

    private val activeDownloadJobs = mutableMapOf<String, Job>()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val readerPreferences: StateFlow<ReaderPreferences> = preferencesStore.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReaderPreferences()
    )

    val offlineBooks: StateFlow<List<BookEntity>> = repository.offlineBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- CLOUD-SYNCED USER PROFILE, PERSONAL LIBRARY & STUDY NOTES (FIRESTORE) ---
    val userProfileState: StateFlow<CloudUiState<UserProfile?>> = cloudRepository
        .observeUserProfile(currentUserId)
        .map<UserProfile?, CloudUiState<UserProfile?>> { CloudUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing user profile", rootError)
            emit(CloudUiState.Error(rootError.message ?: "Failed to load account profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = CloudUiState.Loading
        )

    val personalBooksState: StateFlow<CloudUiState<List<PersonalBook>>> = cloudRepository
        .observePersonalBooks(currentUserId)
        .map<List<PersonalBook>, CloudUiState<List<PersonalBook>>> { CloudUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing personal books", rootError)
            emit(CloudUiState.Error(rootError.message ?: "Failed to load personal library"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = CloudUiState.Loading
        )

    val cloudStudyNotesState: StateFlow<CloudUiState<List<CloudStudyNote>>> = cloudRepository
        .observeStudyNotes(currentUserId)
        .map<List<CloudStudyNote>, CloudUiState<List<CloudStudyNote>>> { notes ->
            CloudUiState.Success(notes.sortedByDescending { it.updatedAt?.seconds ?: 0L })
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing study notes", rootError)
            emit(CloudUiState.Error(rootError.message ?: "Failed to load study notes"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = CloudUiState.Loading
        )

    val personalBookMap: StateFlow<Map<String, PersonalBook>> = personalBooksState
        .map { state ->
            when (state) {
                is CloudUiState.Success -> state.data.associateBy { it.bookId }
                else -> emptyMap()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyMap()
        )

    val bookmarkedBookIds: StateFlow<Set<String>> = combine(
        personalBookMap,
        allBookmarks
    ) { cloudMap, localMarks ->
        val cloudIds = cloudMap.values.filter { it.bookmarked }.map { it.bookId }.toSet()
        val localIds = localMarks.map { it.bookId }.toSet()
        cloudIds + localIds
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptySet()
    )

    val completedBookIds: StateFlow<Set<String>> = personalBookMap
        .map { map -> map.values.filter { it.completed }.map { it.bookId }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptySet()
        )

    // Strict chronological college ordering: Year 1 (CE 101) -> Year 4 (CE 408)
    val libraryUiState: StateFlow<LibraryUiState> = combine(
        repository.allBooks,
        repository.lastOpenedBook,
        _selectedSubject,
        readerPreferences,
        combine(isOnline, _isSyncingCatalog) { online, syncing -> online to syncing }
    ) { allBooks, lastOpened, selectedSubject, prefs, (online, syncing) ->
        val chronologicalAll = allBooks.sortedWith(
            compareBy<BookEntity> { CivilSubject.fromKey(it.subjectKey).sequenceOrder }
                .thenBy { extractCourseNumber(it.courseCode) }
                .thenByDescending { it.isCuratedStarter }
                .thenBy { it.title.lowercase() }
        )
        val filtered = chronologicalAll.filter { book ->
            selectedSubject == null || book.subjectKey.equals(selectedSubject.name, ignoreCase = true)
        }
        val sorted = when (prefs.sortMode) {
            LibrarySortMode.BY_SUBJECT -> filtered
            LibrarySortMode.BY_TITLE -> filtered.sortedBy { it.title.lowercase() }
            LibrarySortMode.BY_AUTHOR -> filtered.sortedBy { it.author.lowercase() }
        }
        val offlineList = chronologicalAll.filter { it.isOfflineAvailable }
        LibraryUiState(
            books = chronologicalAll,
            filteredBooks = sorted,
            lastOpenedBook = lastOpened,
            selectedSubject = selectedSubject,
            sortMode = prefs.sortMode,
            viewMode = prefs.viewMode,
            isOnline = online,
            isSyncingCatalog = syncing,
            offlineBookCount = offlineList.size,
            totalCatalogCount = chronologicalAll.size,
            totalOfflineBytes = offlineList.sumOf { it.fileSizeBytes }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    // Next chronological textbook she hasn't completed yet
    val nextChronologicalBook: StateFlow<BookEntity?> = combine(
        libraryUiState,
        completedBookIds
    ) { libState, completed ->
        libState.books.firstOrNull { book ->
            sanitizeFirestoreDocId(book.id) !in completed && book.id !in completed
        } ?: libState.books.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )

    init {
        viewModelScope.launch {
            cloudRepository.ensureUserProfile(currentUser)
            repository.ensureStarterCatalog()
            refreshOpenAccessCatalog(silent = true)
        }
    }

    fun refreshOpenAccessCatalog(silent: Boolean = false) {
        if (_isSyncingCatalog.value) return
        viewModelScope.launch {
            _isSyncingCatalog.value = true
            val onlineNow = networkMonitor.isCurrentlyOnline() || isOnline.value
            val added = repository.syncOpenAccessCatalog(isOnline = onlineNow)
            _isSyncingCatalog.value = false
            if (!silent) {
                _snackbarMessage.value = when {
                    !onlineNow -> "Offline — showing cached catalog and downloaded PDFs"
                    added > 0 -> "Added $added open-access Civil Engineering textbooks"
                    else -> "Your Civil Engineering library catalog is up to date"
                }
            }
        }
    }

    fun selectSubject(subject: CivilSubject?) {
        _selectedSubject.value = subject
    }

    fun setSortMode(mode: LibrarySortMode) {
        viewModelScope.launch { preferencesStore.updateSortMode(mode) }
    }

    fun setViewMode(mode: LibraryViewMode) {
        viewModelScope.launch { preferencesStore.updateViewMode(mode) }
    }

    fun setPaperTheme(theme: PaperTheme) {
        viewModelScope.launch { preferencesStore.updatePaperTheme(theme) }
    }

    fun setReadingLayout(layout: BookReadingLayout) {
        viewModelScope.launch { preferencesStore.updateReadingLayout(layout) }
    }

    fun setCropPdfMargins(crop: Boolean) {
        viewModelScope.launch { preferencesStore.updateCropPdfMargins(crop) }
    }

    fun adjustFontSize(delta: Float) {
        viewModelScope.launch {
            val next = (readerPreferences.value.fontSizeSp + delta).coerceIn(14f, 24f)
            preferencesStore.updateFontSize(next)
        }
    }

    fun updateProfilePreferences(studyMantra: String, curriculumYear: Int) {
        viewModelScope.launch {
            cloudRepository.updateProfilePreferences(currentUserId, studyMantra, curriculumYear)
                .onSuccess {
                    _snackbarMessage.value = "Profile updated for Year ${curriculumYear.coerceIn(1, 5)}"
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Could not save profile changes"
                }
        }
    }

    fun toggleBookInPersonalLibrary(book: BookEntity) {
        viewModelScope.launch {
            val docId = sanitizeFirestoreDocId(book.id)
            val existing = personalBookMap.value[docId]
            cloudRepository.togglePersonalBookBookmark(currentUserId, book, existing)
                .onSuccess { nowBookmarked ->
                    _snackbarMessage.value = if (nowBookmarked) {
                        "Saved \"${book.title}\" to your Bookmarks"
                    } else {
                        "Removed \"${book.title}\" from Bookmarks"
                    }
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Could not update bookmark"
                }
        }
    }

    fun toggleBookCompleted(book: BookEntity) {
        viewModelScope.launch {
            val docId = sanitizeFirestoreDocId(book.id)
            val existing = personalBookMap.value[docId]
            cloudRepository.togglePersonalBookCompleted(currentUserId, book, existing)
                .onSuccess { nowCompleted ->
                    _snackbarMessage.value = if (nowCompleted) {
                        "Marked \"${book.title}\" completed"
                    } else {
                        "Marked \"${book.title}\" in progress"
                    }
                }
                .onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Could not update progress"
                }
        }
    }

    fun resolvePdfDisplayName(uri: Uri): String {
        return PdfStorageHelper.resolveDisplayName(context, uri)
    }

    fun importLocalPdf(
        uri: Uri,
        title: String,
        author: String,
        isbn: String,
        subject: CivilSubject,
        onImported: (String) -> Unit = {}
    ) {
        if (_isImportingPdf.value) return
        viewModelScope.launch {
            _isImportingPdf.value = true
            val result = repository.importLocalPdfTextbook(uri, title, author, isbn, subject)
            _isImportingPdf.value = false
            result.onSuccess { book ->
                val docId = sanitizeFirestoreDocId(book.id)
                cloudRepository.togglePersonalBookBookmark(
                    userId = currentUserId,
                    book = book,
                    existingRecord = personalBookMap.value[docId]
                )
                _snackbarMessage.value = "Imported \"${book.title}\" (${book.totalPages} pages) to your library"
                onImported(book.id)
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "Could not import selected PDF file"
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchState.update {
            it.copy(
                query = newQuery,
                isbnBadgeLabel = IsbnValidator.detectFormatLabel(newQuery),
                statusMessage = null
            )
        }
    }

    fun setSearchSourceFilter(filter: SearchSourceFilter) {
        _searchState.update { it.copy(sourceFilter = filter) }
    }

    fun executeSearch(explicitQuery: String? = null) {
        val targetQuery = (explicitQuery ?: _searchState.value.query).trim()
        if (targetQuery.isEmpty()) return

        _searchState.update {
            it.copy(
                query = targetQuery,
                isbnBadgeLabel = IsbnValidator.detectFormatLabel(targetQuery),
                isSearching = true,
                statusMessage = null
            )
        }

        viewModelScope.launch {
            val combined = repository.searchAcrossRepositories(
                rawQuery = targetQuery,
                isOnline = networkMonitor.isCurrentlyOnline() || isOnline.value
            )
            _searchState.update {
                it.copy(
                    isSearching = false,
                    hasSearched = true,
                    localResults = combined.localMatches,
                    remoteResults = combined.remoteResults,
                    statusMessage = combined.errorMessage
                )
            }
        }
    }

    fun downloadCatalogBook(book: BookEntity, onComplete: (BookEntity) -> Unit = {}) {
        if (book.id in _downloadProgressMap.value) return
        val job = viewModelScope.launch {
            _downloadProgressMap.update {
                it + (book.id to DownloadProgress(book.id, statusText = "Preparing PDF…"))
            }
            try {
                val outcome = repository.downloadBookEntityOffline(book) { progress ->
                    _downloadProgressMap.update { it + (book.id to progress) }
                }
                outcome.onSuccess { saved ->
                    val docId = sanitizeFirestoreDocId(saved.id)
                    if (personalBookMap.value[docId]?.bookmarked != true) {
                        cloudRepository.togglePersonalBookBookmark(
                            userId = currentUserId,
                            book = saved,
                            existingRecord = personalBookMap.value[docId]
                        )
                    }
                    _snackbarMessage.value = "Downloaded \"${saved.title}\" (${saved.totalPages} pages) offline"
                    onComplete(saved)
                }.onFailure { err ->
                    _snackbarMessage.value = err.message
                        ?: "Could not download full PDF for this title. Try another open-access edition."
                }
            } catch (_: CancellationException) {
                _snackbarMessage.value = "Download cancelled"
            } finally {
                _downloadProgressMap.update { it - book.id }
                activeDownloadJobs.remove(book.id)
            }
        }
        activeDownloadJobs[book.id] = job
    }

    fun downloadRemoteBook(result: DiscoverBookResult, onSaved: (String) -> Unit = {}) {
        if (result.id in _downloadProgressMap.value) return
        val job = viewModelScope.launch {
            _downloadProgressMap.update {
                it + (result.id to DownloadProgress(result.id, statusText = "Preparing download…"))
            }
            try {
                val outcome = repository.downloadRemoteResultOffline(result) { progress ->
                    _downloadProgressMap.update { it + (result.id to progress) }
                }
                outcome.onSuccess { saved ->
                    val docId = sanitizeFirestoreDocId(saved.id)
                    if (personalBookMap.value[docId]?.bookmarked != true) {
                        cloudRepository.togglePersonalBookBookmark(
                            userId = currentUserId,
                            book = saved,
                            existingRecord = personalBookMap.value[docId]
                        )
                    }
                    _searchState.update { state ->
                        state.copy(
                            remoteResults = state.remoteResults.map { item ->
                                if (item.id == result.id) {
                                    item.copy(isAlreadyInLibrary = true, isOfflineDownloaded = true)
                                } else item
                            }
                        )
                    }
                    _snackbarMessage.value = "Saved \"${saved.title}\" (${saved.totalPages} pages) to Bookmarks"
                    onSaved(saved.id)
                }.onFailure { err ->
                    _snackbarMessage.value = err.message
                        ?: "Unable to download full PDF right now. Try another open-access result."
                }
            } catch (_: CancellationException) {
                _snackbarMessage.value = "Download cancelled"
            } finally {
                _downloadProgressMap.update { it - result.id }
                activeDownloadJobs.remove(result.id)
            }
        }
        activeDownloadJobs[result.id] = job
    }

    fun cancelDownload(bookId: String) {
        activeDownloadJobs[bookId]?.cancel()
        activeDownloadJobs.remove(bookId)
        _downloadProgressMap.update { it - bookId }
    }

    fun observeBook(bookId: String): Flow<BookEntity?> = repository.observeBookById(bookId)

    fun observePages(bookId: String): Flow<List<BookPageEntity>> = repository.observePagesForBook(bookId)

    fun observeBookmarksForBook(bookId: String): Flow<List<BookmarkEntity>> =
        repository.observeBookmarksForBook(bookId)

    fun recordReadingProgress(bookId: String, pageNumber: Int) {
        viewModelScope.launch {
            repository.recordReadingProgress(bookId, pageNumber)
            val book = repository.observeBookById(bookId).firstOrNull()
            if (book != null) {
                val docId = sanitizeFirestoreDocId(book.id)
                cloudRepository.syncReadingProgress(
                    userId = currentUserId,
                    book = book,
                    pageNumber = pageNumber,
                    existingRecord = personalBookMap.value[docId]
                )
            }
        }
    }

    fun toggleBookmark(bookId: String, bookTitle: String, pageNumber: Int, chapterTitle: String) {
        viewModelScope.launch {
            val added = repository.toggleBookmark(bookId, bookTitle, pageNumber, chapterTitle)
            val noteDocId = sanitizeFirestoreDocId("${bookId}_p${pageNumber}")
            if (added) {
                cloudRepository.upsertStudyNote(
                    userId = currentUserId,
                    bookId = bookId,
                    bookTitle = bookTitle,
                    pageNumber = pageNumber,
                    chapterTitle = chapterTitle,
                    noteText = ""
                )
            } else {
                cloudRepository.deleteStudyNote(currentUserId, noteDocId)
            }
            _snackbarMessage.value = if (added) "Page $pageNumber bookmarked" else "Bookmark removed"
        }
    }

    fun saveReviewNote(
        bookId: String,
        bookTitle: String,
        pageNumber: Int,
        chapterTitle: String,
        noteText: String
    ) {
        viewModelScope.launch {
            repository.saveReviewNote(bookId, bookTitle, pageNumber, chapterTitle, noteText)
            cloudRepository.upsertStudyNote(
                userId = currentUserId,
                bookId = bookId,
                bookTitle = bookTitle,
                pageNumber = pageNumber,
                chapterTitle = chapterTitle,
                noteText = noteText
            )
            _snackbarMessage.value = "Review note saved for Page $pageNumber"
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmarkId)
            _snackbarMessage.value = "Note deleted"
        }
    }

    fun deleteCloudStudyNote(noteDocId: String) {
        viewModelScope.launch {
            cloudRepository.deleteStudyNote(currentUserId, noteDocId)
                .onSuccess { _snackbarMessage.value = "Study note deleted" }
                .onFailure { _snackbarMessage.value = "Could not delete study note" }
        }
    }

    fun removeOfflineBook(book: BookEntity) {
        viewModelScope.launch {
            repository.removeOfflineDownload(book)
            _snackbarMessage.value = "Removed offline PDF for \"${book.title}\""
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    private fun extractCourseNumber(courseCode: String): Int {
        val digits = courseCode.filter { it.isDigit() }
        return digits.toIntOrNull() ?: 999
    }

    private companion object {
        const val TAG = "CivLibVM"
    }
}
