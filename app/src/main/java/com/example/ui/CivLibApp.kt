package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.firebase.CloudUiState
import com.example.data.firebase.UserCloudRepository
import com.example.data.firebase.authStateFlow
import com.example.data.firebase.safeFirebaseAuthOrNull
import com.example.data.firebase.sanitizeFirestoreDocId
import com.example.data.firebase.signOutWithCleanup
import com.example.data.model.CivilSubject
import com.example.ui.components.CreamBevelBottomNavBar
import com.example.ui.components.ImportPdfMetadataDialog
import com.example.ui.components.NavBarIconSpec
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BookDetailScreen
import com.example.ui.screens.BookReaderScreen
import com.example.ui.screens.DiscoverHomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OfflineVaultScreen
import com.example.ui.screens.ProfileVaultScreen
import com.example.ui.screens.SearchDiscoverScreen
import com.example.ui.theme.EspressoBg
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.SageOlive
import com.example.ui.theme.SandCream
import com.example.ui.viewmodel.CivLibViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

const val ROUTE_PROFILE = "profile"
const val ROUTE_LIBRARY = "library"
const val ROUTE_BOOKMARKS = "bookmarks"
const val ROUTE_DISCOVER = "discover"
const val ROUTE_SEARCH = "search"
const val ROUTE_BOOK_DETAIL = "book_detail/{bookId}"
const val ROUTE_READER = "reader/{bookId}/{pageNumber}"

/**
 * 4 bottom navigation destinations matching the reference mockup and user tab order:
 * 1. Profile (Google account profile, study track, logout, personal mosaic)
 * 2. Library (All 9 Civil Engineering categories & Year 1 -> Year 4 chronological roadmap)
 * 3. Bookmarks (Personal library of saved books, offline PDFs & synced study notes)
 * 4. Home / Discover (Showcase, search, curated featured banner & recommendations — landing screen)
 */
private val TopLevelNavItems = listOf(
    NavBarIconSpec(
        route = ROUTE_PROFILE,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_profile"
    ),
    NavBarIconSpec(
        route = ROUTE_LIBRARY,
        label = "Library",
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        testTag = "nav_library"
    ),
    NavBarIconSpec(
        route = ROUTE_BOOKMARKS,
        label = "Bookmarks",
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
        testTag = "nav_bookmarks"
    ),
    NavBarIconSpec(
        route = ROUTE_DISCOVER,
        label = "Home",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        testTag = "nav_discover"
    )
)

@Composable
fun CivLibApp(
    incomingPdfUri: Uri? = null,
    onConsumeIncomingPdf: () -> Unit = {},
    auth: FirebaseAuth? = remember { safeFirebaseAuthOrNull() }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cloudRepo = remember(context) { UserCloudRepository(context.applicationContext) }

    val currentUser by remember(auth) { auth.authStateFlow() }
        .collectAsStateWithLifecycle(initialValue = auth?.currentUser)
    var localSessionActive by remember {
        mutableStateOf(cloudRepo.isLocalOnboardingCompleted())
    }

    val user = currentUser
    if (user == null && !localSessionActive) {
        AuthScreen(
            onAuthSuccess = { localSessionActive = true },
            onSurveyCompleted = { survey ->
                scope.launch {
                    val uid = auth?.currentUser?.uid ?: "local_student"
                    cloudRepo.saveProfileOnboardingSurvey(
                        userId = uid,
                        displayName = survey.displayName,
                        email = survey.email,
                        curriculumYear = survey.curriculumYear,
                        primarySubjectKey = survey.primarySubjectKey,
                        studyGoal = survey.studyGoal,
                        studyMantra = survey.studyMantra,
                        selectedSubjectKeys = survey.selectedSubjectKeys,
                        selectedGoals = survey.selectedGoals
                    )
                    localSessionActive = true
                }
            }
        )
    } else {
        val sessionKey = user?.uid ?: "local_student"
        key(sessionKey) {
            AuthenticatedCivLibApp(
                currentUser = user,
                incomingPdfUri = incomingPdfUri,
                onConsumeIncomingPdf = onConsumeIncomingPdf
            )
        }
    }
}

@Composable
private fun AuthenticatedCivLibApp(
    currentUser: FirebaseUser?,
    incomingPdfUri: Uri?,
    onConsumeIncomingPdf: () -> Unit,
    onSignedOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val sessionKey = currentUser?.uid ?: "local_student"

    val viewModel: CivLibViewModel = viewModel(
        key = sessionKey,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = try {
                    FirebaseFirestore.getInstance(databaseId)
                } catch (_: Exception) {
                    null
                }
                CivLibViewModel(
                    appContext = app.applicationContext,
                    cloudRepository = UserCloudRepository(
                        db = db,
                        appContext = app.applicationContext
                    ),
                    currentUser = currentUser
                )
            }
        }
    )

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val libraryState by viewModel.libraryUiState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val offlineBooks by viewModel.offlineBooks.collectAsStateWithLifecycle()
    val allBookmarks by viewModel.allBookmarks.collectAsStateWithLifecycle()
    val readerPreferences by viewModel.readerPreferences.collectAsStateWithLifecycle()
    val downloadProgressMap by viewModel.downloadProgressMap.collectAsStateWithLifecycle()
    val isImportingPdf by viewModel.isImportingPdf.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val cloudStudyNotesState by viewModel.cloudStudyNotesState.collectAsStateWithLifecycle()
    val bookmarkedBookIds by viewModel.bookmarkedBookIds.collectAsStateWithLifecycle()
    val completedBookIds by viewModel.completedBookIds.collectAsStateWithLifecycle()
    val nextChronologicalBook by viewModel.nextChronologicalBook.collectAsStateWithLifecycle()

    val userProfile = (userProfileState as? CloudUiState.Success)?.data
    val cloudNotes = (cloudStudyNotesState as? CloudUiState.Success)?.data.orEmpty()

    val personalSavedBooks = remember(libraryState.books, bookmarkedBookIds) {
        libraryState.books.filter { book ->
            val docId = sanitizeFirestoreDocId(book.id)
            docId in bookmarkedBookIds || book.id in bookmarkedBookIds || book.isOfflineAvailable
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Zero-permission Storage Access Framework PDF picker
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImportTitle by remember { mutableStateOf("") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportTitle = viewModel.resolvePdfDisplayName(uri)
            pendingImportUri = uri
        }
    }

    val launchPdfImport: () -> Unit = {
        pdfPickerLauncher.launch(arrayOf("application/pdf"))
    }

    val openBookDetail: (String) -> Unit = { bookId ->
        navController.navigate("book_detail/${Uri.encode(bookId)}")
    }

    val openBookReader: (String, Int) -> Unit = { bookId, pageNumber ->
        navController.navigate("reader/${Uri.encode(bookId)}/${pageNumber.coerceAtLeast(1)}")
    }

    val handleSignOut: () -> Unit = {
        viewModel.clearLocalProfileSession()
        signOutWithCleanup(
            credentialManager = credentialManager,
            scope = scope,
            onSignOutComplete = onSignedOut
        )
    }

    // Handle external "Open PDF with CivLib" intents by importing & launching directly as a book
    LaunchedEffect(incomingPdfUri) {
        val uri = incomingPdfUri ?: return@LaunchedEffect
        val inferredTitle = viewModel.resolvePdfDisplayName(uri)
        val inferredSubject = CivilSubject.inferFromText(inferredTitle)
        viewModel.importLocalPdf(
            uri = uri,
            title = inferredTitle,
            author = "",
            isbn = "",
            subject = inferredSubject,
            onImported = { importedId ->
                openBookReader(importedId, 1)
            }
        )
        onConsumeIncomingPdf()
    }

    // Auto-sync open-access engineering catalog every time the app is opened/resumed
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, readerPreferences.autoSyncOnLaunch) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && readerPreferences.autoSyncOnLaunch) {
                viewModel.refreshOpenAccessCatalog(silent = true)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearSnackbar()
    }

    val isTopLevel = currentRoute == ROUTE_PROFILE ||
        currentRoute == ROUTE_LIBRARY ||
        currentRoute == ROUTE_BOOKMARKS ||
        currentRoute == ROUTE_DISCOVER

    val navigateToTopLevel: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(EspressoBg)
    ) {
        val useNavRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavRail && isTopLevel) {
                NavigationRail(
                    containerColor = SandCream,
                    contentColor = EspressoInk
                ) {
                    TopLevelNavItems.forEach { dest ->
                        val selected = currentRoute == dest.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateToTopLevel(dest.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.label
                                )
                            },
                            label = { Text(dest.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = EspressoInk,
                                selectedTextColor = EspressoInk,
                                indicatorColor = SageOlive.copy(alpha = 0.45f),
                                unselectedIconColor = EspressoInk.copy(alpha = 0.72f),
                                unselectedTextColor = EspressoInk.copy(alpha = 0.72f)
                            ),
                            modifier = Modifier.testTag(dest.testTag)
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = ROUTE_DISCOVER
                ) {
                    composable(ROUTE_DISCOVER) {
                        DiscoverHomeScreen(
                            libraryState = libraryState,
                            searchState = searchState,
                            downloadProgressMap = downloadProgressMap,
                            userProfile = userProfile,
                            onQueryChanged = viewModel::onSearchQueryChanged,
                            onExecuteSearch = viewModel::executeSearch,
                            onSelectSourceFilter = viewModel::setSearchSourceFilter,
                            onOpenSubjectsTab = {
                                viewModel.selectSubject(null)
                                navigateToTopLevel(ROUTE_LIBRARY)
                            },
                            onImportPdfClick = launchPdfImport,
                            onRefreshCatalog = { viewModel.refreshOpenAccessCatalog(silent = false) },
                            onBookClick = openBookDetail,
                            onQuickReadBook = openBookReader,
                            onDownloadRemoteBook = { remote ->
                                viewModel.downloadRemoteBook(remote)
                            },
                            onCancelDownload = viewModel::cancelDownload
                        )
                    }

                    composable(ROUTE_LIBRARY) {
                        LibraryScreen(
                            uiState = libraryState,
                            downloadProgressMap = downloadProgressMap,
                            bookmarkedBookIds = bookmarkedBookIds,
                            completedBookIds = completedBookIds,
                            nextChronologicalBookId = nextChronologicalBook?.id,
                            onSelectSubject = viewModel::selectSubject,
                            onSelectSortMode = viewModel::setSortMode,
                            onToggleViewMode = viewModel::setViewMode,
                            onToggleBookmarkBook = viewModel::toggleBookInPersonalLibrary,
                            onToggleCompletedBook = viewModel::toggleBookCompleted,
                            onOpenSearch = { navigateToTopLevel(ROUTE_DISCOVER) },
                            onImportPdfClick = launchPdfImport,
                            onRefreshCatalog = { viewModel.refreshOpenAccessCatalog(silent = false) },
                            onBookClick = openBookDetail,
                            onQuickReadBook = openBookReader,
                            onBackClick = { navigateToTopLevel(ROUTE_DISCOVER) }
                        )
                    }

                    composable(ROUTE_PROFILE) {
                        ProfileVaultScreen(
                            currentUser = currentUser,
                            userProfile = userProfile,
                            allBooks = libraryState.books,
                            personalSavedBooks = personalSavedBooks,
                            completedCount = completedBookIds.size,
                            cloudNotes = cloudNotes,
                            readerPreferences = readerPreferences,
                            onImportPdfClick = launchPdfImport,
                            onRefreshCatalog = { viewModel.refreshOpenAccessCatalog(silent = false) },
                            onUpdateProfilePreferences = { mantra, yr ->
                                viewModel.updateProfilePreferences(mantra, yr)
                            },
                            onSaveProfileSurvey = { res ->
                                viewModel.saveProfileOnboardingSurvey(
                                    displayName = res.displayName,
                                    email = res.email,
                                    curriculumYear = res.curriculumYear,
                                    primarySubjectKey = res.primarySubjectKey,
                                    studyGoal = res.studyGoal,
                                    studyMantra = res.studyMantra,
                                    selectedSubjectKeys = res.selectedSubjectKeys,
                                    selectedGoals = res.selectedGoals
                                )
                            },
                            onSelectPaperTheme = viewModel::setPaperTheme,
                            onSelectFontSize = viewModel::setFontSize,
                            onToggleCropPdfMargins = viewModel::setCropPdfMargins,
                            onToggleAutoSyncOnLaunch = viewModel::setAutoSyncOnLaunch,
                            onToggleResetToPortraitTrimmed = viewModel::setResetToPortraitTrimmedOnOpen,
                            onSaveCustomGeminiApiKey = viewModel::setCustomGeminiApiKey,
                            onSignOutClick = handleSignOut,
                            onSwitchGoogleAccount = handleSignOut,
                            onBookClick = openBookDetail,
                            onOpenBookAtPage = openBookReader,
                            onDeleteCloudNote = viewModel::deleteCloudStudyNote
                        )
                    }

                    composable(ROUTE_BOOKMARKS) {
                        OfflineVaultScreen(
                            personalSavedBooks = personalSavedBooks,
                            offlineBooks = offlineBooks,
                            completedBookIds = completedBookIds,
                            bookmarks = allBookmarks,
                            cloudNotes = cloudNotes,
                            isOnline = isOnline,
                            onImportPdfClick = launchPdfImport,
                            onOpenBookDetail = openBookDetail,
                            onOpenBookAtPage = openBookReader,
                            onToggleBookmarkBook = viewModel::toggleBookInPersonalLibrary,
                            onToggleCompletedBook = viewModel::toggleBookCompleted,
                            onRemoveOfflineBook = viewModel::removeOfflineBook,
                            onDeleteBookmark = viewModel::deleteBookmark,
                            onDeleteCloudNote = viewModel::deleteCloudStudyNote
                        )
                    }

                    composable(ROUTE_SEARCH) {
                        SearchDiscoverScreen(
                            searchState = searchState,
                            downloadProgressMap = downloadProgressMap,
                            isOnline = isOnline,
                            onQueryChanged = viewModel::onSearchQueryChanged,
                            onExecuteSearch = viewModel::executeSearch,
                            onSelectSourceFilter = viewModel::setSearchSourceFilter,
                            onOpenLocalBook = openBookDetail,
                            onDownloadRemoteBook = { remote ->
                                viewModel.downloadRemoteBook(remote)
                            },
                            onCancelDownload = viewModel::cancelDownload
                        )
                    }

                    composable(
                        route = ROUTE_BOOK_DETAIL,
                        arguments = listOf(navArgument("bookId") { type = NavType.StringType })
                    ) { entry ->
                        val bookId = Uri.decode(entry.arguments?.getString("bookId").orEmpty())
                        val book by viewModel.observeBook(bookId)
                            .collectAsStateWithLifecycle(initialValue = null)
                        val pages by viewModel.observePages(bookId)
                            .collectAsStateWithLifecycle(initialValue = emptyList())
                        val bookBookmarks by viewModel.observeBookmarksForBook(bookId)
                            .collectAsStateWithLifecycle(initialValue = emptyList())
                        val safeDocId = sanitizeFirestoreDocId(bookId)

                        BookDetailScreen(
                            book = book,
                            pages = pages,
                            bookmarks = bookBookmarks,
                            downloadProgress = downloadProgressMap[bookId],
                            isBookmarked = safeDocId in bookmarkedBookIds || bookId in bookmarkedBookIds,
                            isCompleted = safeDocId in completedBookIds || bookId in completedBookIds,
                            customGeminiApiKey = readerPreferences.customGeminiApiKey,
                            onBack = { navController.popBackStack() },
                            onOpenReader = { pageNum -> openBookReader(bookId, pageNum) },
                            onDownloadBook = { target ->
                                viewModel.downloadCatalogBook(target) { saved ->
                                    openBookReader(saved.id, saved.currentPage)
                                }
                            },
                            onCancelDownload = viewModel::cancelDownload,
                            onRemoveOffline = viewModel::removeOfflineBook,
                            onToggleBookmark = viewModel::toggleBookInPersonalLibrary,
                            onToggleCompleted = viewModel::toggleBookCompleted,
                            onSaveReviewNote = { pNum, chTitle, noteText ->
                                book?.let { b ->
                                    viewModel.saveReviewNote(b.id, b.title, pNum, chTitle, noteText)
                                }
                            }
                        )
                    }

                    composable(
                        route = ROUTE_READER,
                        arguments = listOf(
                            navArgument("bookId") { type = NavType.StringType },
                            navArgument("pageNumber") { type = NavType.IntType }
                        )
                    ) { entry ->
                        val bookId = Uri.decode(entry.arguments?.getString("bookId").orEmpty())
                        val pageNumber = entry.arguments?.getInt("pageNumber") ?: 1
                        val book by viewModel.observeBook(bookId)
                            .collectAsStateWithLifecycle(initialValue = null)
                        val pages by viewModel.observePages(bookId)
                            .collectAsStateWithLifecycle(initialValue = emptyList())
                        val bookBookmarks by viewModel.observeBookmarksForBook(bookId)
                            .collectAsStateWithLifecycle(initialValue = emptyList())

                        BookReaderScreen(
                            book = book,
                            pages = pages,
                            bookmarks = bookBookmarks,
                            initialPageNumber = pageNumber,
                            preferences = readerPreferences,
                            onBack = { navController.popBackStack() },
                            onPageVisited = { visitedPage ->
                                viewModel.recordReadingProgress(bookId, visitedPage)
                            },
                            onToggleBookmark = { pNum, chTitle ->
                                book?.let { b ->
                                    viewModel.toggleBookmark(b.id, b.title, pNum, chTitle)
                                }
                            },
                            onSaveReviewNote = { pNum, chTitle, note ->
                                book?.let { b ->
                                    viewModel.saveReviewNote(b.id, b.title, pNum, chTitle, note)
                                }
                            },
                            onSelectPaperTheme = viewModel::setPaperTheme,
                            onSelectReadingLayout = viewModel::setReadingLayout,
                            onToggleCropPdfMargins = viewModel::setCropPdfMargins,
                            onAdjustFontSize = viewModel::adjustFontSize
                        )
                    }
                }

                // Floating rounded-top Sand-Cream beveled bottom navigation bar
                if (!useNavRail && isTopLevel) {
                    CreamBevelBottomNavBar(
                        items = TopLevelNavItems,
                        selectedRoute = currentRoute,
                        onSelectRoute = navigateToTopLevel,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }

                // Snackbar positioned cleanly above the floating cream nav bar
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(
                            bottom = if (!useNavRail && isTopLevel) 78.dp else 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                )
            }
        }
    }

    // Metadata tagging dialog after picking a local PDF textbook
    pendingImportUri?.let { uri ->
        ImportPdfMetadataDialog(
            initialTitle = pendingImportTitle,
            isImporting = isImportingPdf,
            onDismiss = { pendingImportUri = null },
            onConfirmImport = { title, author, isbn, subject ->
                viewModel.importLocalPdf(
                    uri = uri,
                    title = title,
                    author = author,
                    isbn = isbn,
                    subject = subject,
                    onImported = { importedId ->
                        pendingImportUri = null
                        openBookReader(importedId, 1)
                    }
                )
            }
        )
    }
}
