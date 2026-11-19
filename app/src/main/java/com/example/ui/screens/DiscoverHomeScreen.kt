package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookEntity
import com.example.data.model.BookSourceType
import com.example.data.model.CivilSubject
import com.example.data.model.DiscoverBookResult
import com.example.data.model.DownloadProgress
import com.example.data.model.SearchSourceFilter
import com.example.data.model.formatFileSize
import com.example.ui.components.HardcoverBookThumbnail
import com.example.ui.theme.DmSansFontFamily
import com.example.ui.theme.EspressoBg
import com.example.ui.theme.EspressoCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.EspressoInkMuted
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.SageOlive
import com.example.ui.theme.SandCream
import com.example.ui.theme.SandCreamLight
import com.example.ui.theme.SandCreamMuted
import com.example.ui.viewmodel.LibraryUiState
import com.example.ui.viewmodel.SearchUiState

/**
 * Editorial Home Showcase Screen:
 * - Warm Espresso background (#4D3C36)
 * - Full-width Sand-Cream pill Search bar
 * - 3 mathematically centered Circular Olive-Sage medallions (PDF Books, Subjects, Import PDF)
 * - Clean full-width Sand-Cream Featured Highlight Banner (no decorative curve lines) with crisp book/text alignment
 * - Horizontal "Top Textbooks" shelf with aligned 0.72f book covers & refined Playfair/Lora typography
 */
@Composable
fun DiscoverHomeScreen(
    libraryState: LibraryUiState,
    searchState: SearchUiState,
    downloadProgressMap: Map<String, DownloadProgress>,
    userProfile: com.example.data.firebase.UserProfile? = null,
    onQueryChanged: (String) -> Unit,
    onExecuteSearch: (String?) -> Unit,
    onSelectSourceFilter: (SearchSourceFilter) -> Unit,
    onOpenSubjectsTab: () -> Unit,
    onImportPdfClick: () -> Unit,
    onRefreshCatalog: () -> Unit,
    onBookClick: (String) -> Unit,
    onQuickReadBook: (String, Int) -> Unit,
    onDownloadRemoteBook: (DiscoverBookResult) -> Unit,
    onCancelDownload: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val preferredYear = (userProfile?.curriculumYear ?: 1L).toInt().coerceIn(1, 4)
    val preferredSubjectKey = userProfile?.primarySubjectKey.orEmpty()

    val personalizedBooks = remember(libraryState.books, preferredYear, preferredSubjectKey) {
        if (userProfile == null) {
            libraryState.books
        } else {
            libraryState.books.sortedWith(
                compareBy<BookEntity>(
                    { if (preferredSubjectKey.isNotBlank() && it.subjectKey.equals(preferredSubjectKey, ignoreCase = true)) 0 else 1 },
                    { if (CivilSubject.fromKey(it.subjectKey).curriculumYear == preferredYear) 0 else 1 },
                    { CivilSubject.fromKey(it.subjectKey).sequenceOrder },
                    { it.courseCode }
                )
            )
        }
    }

    val featuredBooks = remember(personalizedBooks) {
        personalizedBooks.take(7)
    }
    val topShelfBooks = remember(personalizedBooks) {
        if (personalizedBooks.size > 3) personalizedBooks.drop(1) + personalizedBooks.first()
        else personalizedBooks
    }
    val showSearchResults = searchState.query.isNotBlank() && (searchState.hasSearched || searchState.isSearching)

    val filteredRemote = remember(searchState.remoteResults, searchState.sourceFilter) {
        when (searchState.sourceFilter) {
            SearchSourceFilter.ALL -> searchState.remoteResults
            SearchSourceFilter.LOCAL -> emptyList()
            SearchSourceFilter.INTERNET_ARCHIVE -> searchState.remoteResults.filter {
                it.sourceType == BookSourceType.INTERNET_ARCHIVE
            }
            SearchSourceFilter.OAPEN_DOAB -> searchState.remoteResults.filter {
                it.sourceType == BookSourceType.OAPEN || it.sourceType == BookSourceType.DOAB
            }
            SearchSourceFilter.OPEN_LIBRARY -> searchState.remoteResults.filter {
                it.sourceType == BookSourceType.OPEN_LIBRARY
            }
            SearchSourceFilter.GUTENBERG -> searchState.remoteResults.filter {
                it.sourceType == BookSourceType.GUTENBERG
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EspressoBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(top = 18.dp, bottom = 114.dp)
    ) {
        // 1. Cream Pill Search Bar
        item(key = "pill_search_bar") {
            CreamPillSearchBar(
                query = searchState.query,
                onQueryChanged = onQueryChanged,
                onSearchSubmit = {
                    focusManager.clearFocus()
                    onExecuteSearch(null)
                },
                onClear = { onQueryChanged("") },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(22.dp))
        }

        // 2. Three Mathematically Centered Olive-Sage Medallions
        item(key = "medallion_row") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.Top
            ) {
                SageCategoryMedallion(
                    label = "PDF Books",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    iconTint = Color(0xFFFAF6EB),
                    badgeBg = Color(0xFF6B4F43),
                    onClick = onRefreshCatalog,
                    testTag = "medallion_pdf_books",
                    modifier = Modifier.weight(1f)
                )
                SageCategoryMedallion(
                    label = "Subjects",
                    icon = Icons.AutoMirrored.Filled.LibraryBooks,
                    iconTint = Color(0xFFFAF6EB),
                    badgeBg = Color(0xFF7B412D),
                    onClick = onOpenSubjectsTab,
                    testTag = "medallion_subjects",
                    modifier = Modifier.weight(1f)
                )
                SageCategoryMedallion(
                    label = "Import PDF",
                    icon = Icons.Filled.UploadFile,
                    iconTint = Color(0xFFFAF6EB),
                    badgeBg = Color(0xFF4F3830),
                    onClick = onImportPdfClick,
                    testTag = "medallion_import_pdf",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showSearchResults) {
            // Inline Search Filter Pills
            item(key = "inline_search_filters") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SearchSourceFilter.entries) { filter ->
                        val selected = searchState.sourceFilter == filter
                        Surface(
                            onClick = { onSelectSourceFilter(filter) },
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) SandCream else SageOlive.copy(alpha = 0.72f)
                        ) {
                            Text(
                                text = filter.label,
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = EspressoInk,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (searchState.isSearching) {
                item(key = "searching_spinner") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = SandCream, strokeWidth = 2.5.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Searching Internet Archive, OAPEN, DOAB & Open Library…",
                                style = MaterialTheme.typography.bodySmall,
                                color = SandCreamMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                if (searchState.localResults.isNotEmpty() &&
                    (searchState.sourceFilter == SearchSourceFilter.ALL || searchState.sourceFilter == SearchSourceFilter.LOCAL)
                ) {
                    item(key = "local_hits_header") {
                        Text(
                            text = "In Your Library (${searchState.localResults.size})",
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            color = SandCreamLight,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                    items(searchState.localResults, key = { "home_local_${it.id}" }) { localBook ->
                        HomeLocalSearchRow(
                            book = localBook,
                            onClick = {
                                if (localBook.isOfflineAvailable) onQuickReadBook(localBook.id, localBook.currentPage)
                                else onBookClick(localBook.id)
                            },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                        )
                    }
                }

                if (filteredRemote.isNotEmpty()) {
                    item(key = "remote_hits_header") {
                        Text(
                            text = "Open-Access Repositories (${filteredRemote.size})",
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            color = SandCreamLight,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 6.dp)
                        )
                    }
                    items(filteredRemote, key = { "home_remote_${it.id}" }) { remote ->
                        HomeRemoteSearchRow(
                            book = remote,
                            downloadProgress = downloadProgressMap[remote.id],
                            onDownload = { onDownloadRemoteBook(remote) },
                            onCancel = { onCancelDownload(remote.id) },
                            onOpenExisting = { onBookClick(remote.id) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                        )
                    }
                }

                if (searchState.localResults.isEmpty() && filteredRemote.isEmpty()) {
                    item(key = "no_hits") {
                        Text(
                            text = "No matching Civil Engineering books found. Try an ISBN or keywords like 'structural', 'soil mechanics', or 'hydraulics'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SandCreamMuted,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
                        )
                    }
                }
            }
        } else {
            // 3. Clean Full-Width Sand-Cream Featured Highlight Banner (Balanced Cover & Editorial Copy)
            if (featuredBooks.isNotEmpty()) {
                item(key = "featured_cream_banner") {
                    FeaturedCreamBookPager(
                        books = featuredBooks,
                        onBookClick = { book ->
                            if (book.isOfflineAvailable) onQuickReadBook(book.id, book.currentPage)
                            else onBookClick(book.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(26.dp))
                }
            }

            // 4. "Top Textbooks" Horizontal Shelf
            item(key = "top_textbooks_section") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Top Textbooks",
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 21.sp,
                            color = SandCreamLight
                        )
                        Text(
                            text = "See all",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = SandCreamMuted,
                            modifier = Modifier
                                .clickable(onClick = onOpenSubjectsTab)
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .testTag("home_see_all_library")
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(topShelfBooks, key = { "top_${it.id}" }) { book ->
                            TopShowBookCard(
                                book = book,
                                progress = downloadProgressMap[book.id],
                                onClick = {
                                    if (book.isOfflineAvailable) onQuickReadBook(book.id, book.currentPage)
                                    else onBookClick(book.id)
                                }
                            )
                        }
                    }
                }
            }

            // 5. Continue Reading Card (when an offline book has active progress)
            libraryState.lastOpenedBook?.let { activeBook ->
                item(key = "home_continue_reading") {
                    Spacer(modifier = Modifier.height(26.dp))
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Continue Reading",
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 21.sp,
                            color = SandCreamLight
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            onClick = { onQuickReadBook(activeBook.id, activeBook.currentPage) },
                            shape = RoundedCornerShape(16.dp),
                            color = EspressoCard,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("continue_reading_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val subj = remember(activeBook.subjectKey) { CivilSubject.fromKey(activeBook.subjectKey) }
                                HardcoverBookThumbnail(
                                    title = activeBook.title,
                                    author = activeBook.author,
                                    courseCode = activeBook.courseCode,
                                    subject = subj,
                                    coverUrl = activeBook.coverUrl,
                                    localCoverPath = activeBook.localCoverPath,
                                    coverArtKey = activeBook.coverArtKey,
                                    isOfflineAvailable = true,
                                    showTextOverlay = false,
                                    elevation = 3.dp,
                                    aspectRatio = 0.72f,
                                    cornerRadius = 5.dp,
                                    modifier = Modifier.width(50.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = activeBook.courseCode,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SandCream
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = activeBook.title,
                                        fontFamily = LoraFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.5.sp,
                                        color = SandCreamLight,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Page ${activeBook.currentPage} of ${activeBook.totalPages}",
                                        fontFamily = DmSansFontFamily,
                                        fontSize = 12.sp,
                                        color = SandCreamMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = SandCream
                                ) {
                                    Text(
                                        text = "Read",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = EspressoInk,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreamPillSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = SandCream,
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search books, authors, or ISBN…",
                        fontFamily = DmSansFontFamily,
                        fontSize = 14.5.sp,
                        color = EspressoInkMuted
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    singleLine = true,
                    cursorBrush = SolidColor(EspressoInk),
                    textStyle = TextStyle(
                        fontFamily = DmSansFontFamily,
                        fontSize = 14.5.sp,
                        color = EspressoInk
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_isbn_keyword_field")
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear search",
                        tint = EspressoInkMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            IconButton(
                onClick = onSearchSubmit,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("execute_search_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search books by ISBN or keyword",
                    tint = EspressoInk,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

@Composable
private fun SageCategoryMedallion(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    badgeBg: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(SageOlive)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeBg)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.5.sp,
            color = SandCreamLight,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FeaturedCreamBookPager(
    books: List<BookEntity>,
    onBookClick: (BookEntity) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { books.size })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(216.dp)
            .background(SandCream)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val book = books[page]
                val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onBookClick(book) }
                        .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HardcoverBookThumbnail(
                        title = book.title,
                        author = book.author,
                        courseCode = book.courseCode,
                        subject = subject,
                        coverUrl = book.coverUrl,
                        localCoverPath = book.localCoverPath,
                        coverArtKey = book.coverArtKey,
                        totalPages = book.totalPages,
                        isOfflineAvailable = book.isOfflineAvailable,
                        elevation = 6.dp,
                        aspectRatio = 0.72f,
                        cornerRadius = 6.dp,
                        modifier = Modifier.width(102.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "YEAR ${subject.curriculumYear} · ${book.courseCode}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.6.sp,
                            color = EspressoInkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = book.title,
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.5.sp,
                            lineHeight = 20.sp,
                            color = EspressoInk,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = book.author,
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp,
                            color = EspressoInk.copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = book.description,
                            fontFamily = LoraFontFamily,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            color = EspressoInkMuted,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Clean Centered Pager Indicator Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(books.size.coerceAtMost(7)) { idx ->
                    val active = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.5.dp)
                            .width(if (active) 16.dp else 6.dp)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) EspressoInk else EspressoInk.copy(alpha = 0.28f)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun TopShowBookCard(
    book: BookEntity,
    progress: DownloadProgress?,
    onClick: () -> Unit
) {
    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }
    Column(
        modifier = Modifier
            .width(116.dp)
            .clickable(onClick = onClick)
            .testTag("book_shelf_card_${book.id}")
    ) {
        HardcoverBookThumbnail(
            title = book.title,
            author = book.author,
            courseCode = book.courseCode,
            subject = subject,
            coverUrl = book.coverUrl,
            localCoverPath = book.localCoverPath,
            coverArtKey = book.coverArtKey,
            totalPages = book.totalPages,
            isOfflineAvailable = book.isOfflineAvailable,
            elevation = 5.dp,
            aspectRatio = 0.72f,
            cornerRadius = 8.dp,
            modifier = Modifier.fillMaxWidth()
        )
        if (progress != null) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                color = SandCream,
                trackColor = EspressoCard,
                modifier = Modifier.fillMaxWidth().height(2.5.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = book.title,
            fontFamily = LoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            lineHeight = 16.5.sp,
            color = SandCreamLight,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Y${subject.curriculumYear} · ${book.courseCode}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 9.5.sp,
            color = SandCreamMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeLocalSearchRow(
    book: BookEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = EspressoCard,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HardcoverBookThumbnail(
                title = book.title,
                author = book.author,
                courseCode = book.courseCode,
                subject = subject,
                coverUrl = book.coverUrl,
                localCoverPath = book.localCoverPath,
                coverArtKey = book.coverArtKey,
                isOfflineAvailable = book.isOfflineAvailable,
                showTextOverlay = false,
                aspectRatio = 0.72f,
                cornerRadius = 4.dp,
                modifier = Modifier.width(48.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    fontFamily = LoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = SandCreamLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${book.author} · ${book.courseCode}",
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = SandCreamMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = SandCream
            ) {
                Text(
                    text = if (book.isOfflineAvailable) "Read" else "View",
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp,
                    color = EspressoInk,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeRemoteSearchRow(
    book: DiscoverBookResult,
    downloadProgress: DownloadProgress?,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onOpenExisting: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = EspressoCard,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                HardcoverBookThumbnail(
                    title = book.title,
                    author = book.author,
                    courseCode = book.subject.courseCode,
                    subject = book.subject,
                    coverUrl = book.coverUrl,
                    coverArtKey = book.subject.defaultCoverArtKey,
                    isOfflineAvailable = book.isOfflineDownloaded,
                    showTextOverlay = false,
                    aspectRatio = 0.72f,
                    cornerRadius = 4.dp,
                    modifier = Modifier.width(50.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = book.sourceType.displayLabel.uppercase(),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.5.sp,
                        color = SandCream
                    )
                    Text(
                        text = book.title,
                        fontFamily = LoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = SandCreamLight,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append(book.author)
                            if (book.estimatedPages != null && book.estimatedPages > 0) append(" · ~${book.estimatedPages}p")
                            if (book.fileSizeBytes > 0L) append(" · ${formatFileSize(book.fileSizeBytes)}")
                        },
                        fontFamily = DmSansFontFamily,
                        fontSize = 12.sp,
                        color = SandCreamMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (downloadProgress != null) {
                            Text(
                                text = downloadProgress.formattedSize,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = SandCream
                            )
                            IconButton(onClick = onCancel, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.Filled.Cancel,
                                    contentDescription = "Cancel",
                                    tint = SandCream,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else if (book.isOfflineDownloaded) {
                            Button(
                                onClick = onOpenExisting,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SageOlive, contentColor = EspressoInk),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) { Text("Open Book", fontSize = 12.sp) }
                        } else {
                            Button(
                                onClick = onDownload,
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SandCream, contentColor = EspressoInk),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Get PDF", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            if (downloadProgress != null) {
                Spacer(modifier = Modifier.height(6.dp))
                val frac = downloadProgress.fraction
                if (frac != null) {
                    LinearProgressIndicator(
                        progress = { frac },
                        color = SandCream,
                        trackColor = EspressoBg,
                        modifier = Modifier.fillMaxWidth().height(3.dp)
                    )
                } else {
                    LinearProgressIndicator(
                        color = SandCream,
                        trackColor = EspressoBg,
                        modifier = Modifier.fillMaxWidth().height(3.dp)
                    )
                }
            }
        }
    }
}
