package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DriveFolderUpload
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.sanitizeFirestoreDocId
import com.example.data.local.BookEntity
import com.example.data.model.CivilSubject
import com.example.data.model.DownloadProgress
import com.example.data.model.LibrarySortMode
import com.example.data.model.LibraryViewMode
import com.example.ui.components.CivilSubjectPosterCard
import com.example.ui.components.HardcoverBookThumbnail
import com.example.ui.theme.EspressoBg
import com.example.ui.theme.EspressoCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SageOlive
import com.example.ui.theme.SandCream
import com.example.ui.theme.SandCreamLight
import com.example.ui.theme.SandCreamMuted
import com.example.ui.viewmodel.LibraryUiState

/**
 * 1-to-1 Layout Twin of "My Library" with Full Civil Engineering Categories & Chronological Roadmap:
 * - Warm Espresso background (#4D3C36)
 * - Header: Left "Back", Center "My Library", Right "Filter"
 * - 4 oval-pill icon buttons (active Sand-Cream, inactive Olive-Sage):
 *   1. All 9 Subject Categories (Year 1 -> Year 4)
 *   2. Chronological Reading Roadmap (Year 1 CE 101 -> Year 4 CE 408 with Up Next tracking)
 *   3. Offline Downloaded PDFs
 *   4. Import Local PDF
 * - 3-column vibrant illustrated Subject Poster Grid drilling into chronological course books
 */
@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    downloadProgressMap: Map<String, DownloadProgress>,
    bookmarkedBookIds: Set<String> = emptySet(),
    completedBookIds: Set<String> = emptySet(),
    nextChronologicalBookId: String? = null,
    onSelectSubject: (CivilSubject?) -> Unit,
    onSelectSortMode: (LibrarySortMode) -> Unit,
    onToggleViewMode: (LibraryViewMode) -> Unit,
    onToggleBookmarkBook: (BookEntity) -> Unit = {},
    onToggleCompletedBook: (BookEntity) -> Unit = {},
    onOpenSearch: () -> Unit,
    onImportPdfClick: () -> Unit,
    onRefreshCatalog: () -> Unit,
    onBookClick: (String) -> Unit,
    onQuickReadBook: (String, Int) -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 0 = 9 Subject Categories Grid, 1 = Chronological Curriculum Order (Year 1->4), 2 = Offline PDFs
    var activePillIndex by rememberSaveable { mutableIntStateOf(0) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val showingSubjectsGrid = activePillIndex == 0 && uiState.selectedSubject == null
    val showingChronologicalList = activePillIndex == 1 && uiState.selectedSubject == null

    val displayedBooks = remember(uiState.filteredBooks, activePillIndex) {
        when (activePillIndex) {
            2 -> uiState.filteredBooks.filter { it.isOfflineAvailable }
            else -> uiState.filteredBooks
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EspressoBg)
            .statusBarsPadding()
    ) {
        // 1. Top Header Row: Back · My Library · Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Back",
                fontFamily = PlusJakartaFontFamily,
                fontSize = 14.sp,
                color = SandCreamLight.copy(alpha = 0.88f),
                modifier = Modifier
                    .clickable {
                        if (uiState.selectedSubject != null) {
                            onSelectSubject(null)
                        } else if (activePillIndex != 0) {
                            activePillIndex = 0
                        } else {
                            onBackClick()
                        }
                    }
                    .padding(vertical = 6.dp, horizontal = 2.dp)
                    .testTag("library_back_action")
            )

            Text(
                text = when {
                    uiState.selectedSubject != null -> uiState.selectedSubject.shortName
                    activePillIndex == 1 -> "Roadmap"
                    activePillIndex == 2 -> "Offline"
                    else -> "My Library"
                },
                fontFamily = PlayfairDisplayFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = SandCreamLight
            )

            Box {
                Text(
                    text = "Filter",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 14.sp,
                    color = SandCreamLight.copy(alpha = 0.88f),
                    modifier = Modifier
                        .clickable { showFilterMenu = true }
                        .padding(vertical = 6.dp, horizontal = 2.dp)
                        .testTag("library_filter_action")
                )
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false },
                    containerColor = EspressoCard
                ) {
                    LibrarySortMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Sort by ${mode.label}" + if (mode == uiState.sortMode) " ✓" else "",
                                    color = SandCreamLight
                                )
                            },
                            onClick = {
                                onSelectSortMode(mode)
                                showFilterMenu = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Sync Open-Access Catalog",
                                color = SandCream
                            )
                        },
                        onClick = {
                            onRefreshCatalog()
                            showFilterMenu = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Four Oval-Pill Icon Buttons Row (Categories, Chronological Order, Offline, Import)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Top
        ) {
            LibraryOvalPillButton(
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                label = "Subjects",
                contentDescription = "All 9 Civil Engineering categories",
                selected = activePillIndex == 0 && uiState.selectedSubject == null,
                onClick = {
                    activePillIndex = 0
                    onSelectSubject(null)
                },
                modifier = Modifier.weight(1f).testTag("library_pill_subjects")
            )
            LibraryOvalPillButton(
                icon = Icons.Outlined.FormatListNumbered,
                label = "Roadmap",
                contentDescription = "Chronological order Year 1 to Year 4",
                selected = activePillIndex == 1 && uiState.selectedSubject == null,
                onClick = {
                    activePillIndex = 1
                    onSelectSubject(null)
                },
                modifier = Modifier.weight(1f).testTag("library_pill_chronological")
            )
            LibraryOvalPillButton(
                icon = Icons.Outlined.CheckBox,
                label = "Offline",
                contentDescription = "Offline downloaded PDFs",
                selected = activePillIndex == 2 && uiState.selectedSubject == null,
                onClick = {
                    activePillIndex = 2
                    onSelectSubject(null)
                },
                modifier = Modifier.weight(1f).testTag("library_pill_offline")
            )
            LibraryOvalPillButton(
                icon = Icons.Outlined.DriveFolderUpload,
                label = "Import",
                contentDescription = "Import local PDF textbook",
                selected = false,
                onClick = onImportPdfClick,
                modifier = Modifier.weight(1f).testTag("hero_import_pdf_button")
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Content Area: 9-Category Poster Grid OR Chronological Year 1-4 Roadmap OR Subject Books Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 108.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (showingSubjectsGrid) {
                item(span = { GridItemSpan(3) }, key = "cat_subtitle") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Year 1 → Year 4 Categories (${uiState.totalCatalogCount} books)",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.5.sp,
                            color = SandCreamMuted
                        )
                        Text(
                            text = "Roadmap →",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp,
                            color = SandCream,
                            modifier = Modifier
                                .clickable { activePillIndex = 1 }
                                .padding(vertical = 2.dp)
                        )
                    }
                }

                items(CivilSubject.entries, key = { it.name }) { subject ->
                    val count = uiState.books.count {
                        it.subjectKey.equals(subject.name, ignoreCase = true)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onSelectSubject(subject) }
                            .testTag("subject_chip_${subject.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.66f)
                                .shadow(5.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            CivilSubjectPosterCard(subject = subject)
                            // Compact chronological Year pill in top-left corner
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EspressoBg.copy(alpha = 0.84f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Y${subject.curriculumYear} · #${subject.sequenceOrder}",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp,
                                    color = SandCreamLight
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(7.dp))
                        Text(
                            text = subject.shortName,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = SandCreamLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${subject.courseCode} · $count PDFs",
                            fontFamily = PlusJakartaFontFamily,
                            fontSize = 10.5.sp,
                            color = SandCreamMuted,
                            maxLines = 1
                        )
                    }
                }
            } else if (showingChronologicalList) {
                // Full Chronological Curriculum Order (Year 1 -> Year 4) so she never loses track!
                item(span = { GridItemSpan(3) }, key = "roadmap_header") {
                    Surface(
                        color = EspressoCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "CHRONOLOGICAL STUDY ORDER · YEAR 1 → 4",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SandCream
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Follow each course textbook in sequence. Check off completed volumes or bookmark books to your personal library.",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                color = SandCreamMuted
                            )
                        }
                    }
                }

                itemsIndexed(
                    items = uiState.books,
                    key = { _, book -> "chrono_${book.id}" },
                    span = { _, _ -> GridItemSpan(3) }
                ) { index, book ->
                    val docId = sanitizeFirestoreDocId(book.id)
                    val isBookmarked = docId in bookmarkedBookIds || book.id in bookmarkedBookIds
                    val isCompleted = docId in completedBookIds || book.id in completedBookIds
                    val isUpNext = book.id == nextChronologicalBookId

                    ChronologicalBookRowCard(
                        stepNumber = index + 1,
                        book = book,
                        isBookmarked = isBookmarked,
                        isCompleted = isCompleted,
                        isUpNext = isUpNext,
                        onOpen = {
                            if (book.isOfflineAvailable) onQuickReadBook(book.id, book.currentPage)
                            else onBookClick(book.id)
                        },
                        onToggleBookmark = { onToggleBookmarkBook(book) },
                        onToggleCompleted = { onToggleCompletedBook(book) }
                    )
                }
            } else {
                // Subject detail bar
                if (uiState.selectedSubject != null) {
                    item(span = { GridItemSpan(3) }, key = "subject_sub_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${uiState.selectedSubject.semesterLabel} · ${uiState.selectedSubject.courseCode}",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = SandCream
                                )
                                Text(
                                    text = uiState.selectedSubject.displayName,
                                    fontFamily = PlusJakartaFontFamily,
                                    fontSize = 12.5.sp,
                                    color = SandCreamMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "All 9 subjects",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = SandCream,
                                modifier = Modifier
                                    .clickable { onSelectSubject(null) }
                                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                            )
                        }
                    }
                }

                if (displayedBooks.isEmpty()) {
                    item(span = { GridItemSpan(3) }, key = "empty_grid") {
                        LibraryEmptyCard(
                            title = if (activePillIndex == 2) "No offline PDFs yet"
                            else "No books in this category yet",
                            subtitle = if (activePillIndex == 2) {
                                "Download any Civil Engineering textbook or import a PDF from your device."
                            } else {
                                "Tap Filter → Sync Open-Access Catalog to fetch more free full-length PDFs."
                            },
                            actionLabel = if (activePillIndex == 2) "Import Local PDF" else "Back to Categories",
                            onAction = {
                                if (activePillIndex == 2) onImportPdfClick()
                                else {
                                    activePillIndex = 0
                                    onSelectSubject(null)
                                }
                            }
                        )
                    }
                } else {
                    items(displayedBooks, key = { it.id }) { book ->
                        val docId = sanitizeFirestoreDocId(book.id)
                        val isBookmarked = docId in bookmarkedBookIds || book.id in bookmarkedBookIds
                        val isCompleted = docId in completedBookIds || book.id in completedBookIds
                        LibraryBookGridTile(
                            book = book,
                            progress = downloadProgressMap[book.id],
                            isBookmarked = isBookmarked,
                            isCompleted = isCompleted,
                            onClick = {
                                if (book.isOfflineAvailable) onQuickReadBook(book.id, book.currentPage)
                                else onBookClick(book.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChronologicalBookRowCard(
    stepNumber: Int,
    book: BookEntity,
    isBookmarked: Boolean,
    isCompleted: Boolean,
    isUpNext: Boolean,
    onOpen: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleCompleted: () -> Unit
) {
    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }

    Surface(
        color = if (isUpNext) EspressoCard else EspressoCard.copy(alpha = 0.72f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isUpNext) Modifier.border(1.25.dp, SandCream, RoundedCornerShape(14.dp))
                else Modifier
            )
            .clickable(onClick = onOpen)
            .testTag("chrono_book_${book.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step number circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> SageOlive
                            isUpNext -> SandCream
                            else -> EspressoBg
                        }
                    )
            ) {
                Text(
                    text = stepNumber.toString().padStart(2, '0'),
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = if (isCompleted || isUpNext) EspressoInk else SandCreamLight
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

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
                showTextOverlay = false,
                elevation = 2.dp,
                modifier = Modifier.width(46.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Y${subject.curriculumYear} · ${book.courseCode}",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        color = SandCream
                    )
                    if (isUpNext) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SandCream)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "UP NEXT",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = EspressoInk
                            )
                        }
                    } else if (isCompleted) {
                        Text(
                            text = "DONE",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            color = SageOlive
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = book.title,
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    lineHeight = 17.sp,
                    color = SandCreamLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${book.author} · ${book.totalPages}p",
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 11.sp,
                    color = SandCreamMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove from bookmarks" else "Save to bookmarks",
                        tint = if (isBookmarked) SandCream else SandCreamMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onToggleCompleted,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (isCompleted) "Mark in progress" else "Mark completed",
                        tint = if (isCompleted) SageOlive else SandCreamMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryOvalPillButton(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(50),
            color = if (selected) SandCream else SageOlive,
            shadowElevation = if (selected) 4.dp else 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = EspressoInk,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = label,
            fontFamily = PlusJakartaFontFamily,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = if (selected) SandCreamLight else SandCreamMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LibraryBookGridTile(
    book: BookEntity,
    progress: DownloadProgress?,
    isBookmarked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("book_card_${book.id}")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
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
                modifier = Modifier.fillMaxWidth()
            )
            if (isCompleted || isBookmarked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(EspressoBg.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = if (isCompleted) SageOlive else SandCream,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = book.title,
            fontFamily = PlusJakartaFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            color = SandCreamLight,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${book.courseCode} · ${if (book.totalPages > 0) "${book.totalPages}p" else "PDF"}",
            fontFamily = PlusJakartaFontFamily,
            fontSize = 10.sp,
            color = SandCreamMuted,
            maxLines = 1
        )
        if (progress != null) {
            Spacer(modifier = Modifier.height(4.dp))
            val frac = progress.fraction
            if (frac != null) {
                LinearProgressIndicator(
                    progress = { frac },
                    color = SandCream,
                    trackColor = EspressoCard,
                    modifier = Modifier.fillMaxWidth().height(3.dp)
                )
            } else {
                LinearProgressIndicator(
                    color = SandCream,
                    trackColor = EspressoCard,
                    modifier = Modifier.fillMaxWidth().height(3.dp)
                )
            }
        }
    }
}

@Composable
private fun LibraryEmptyCard(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        color = EspressoCard,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontFamily = PlusJakartaFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = SandCreamLight
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontFamily = PlusJakartaFontFamily,
                fontSize = 13.sp,
                color = SandCreamMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                onClick = onAction,
                shape = RoundedCornerShape(24.dp),
                color = SandCream
            ) {
                Text(
                    text = actionLabel,
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = EspressoInk,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}
