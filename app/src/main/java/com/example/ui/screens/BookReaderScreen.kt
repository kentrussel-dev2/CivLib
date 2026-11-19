package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookEntity
import com.example.data.local.BookPageEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.ReaderPreferences
import com.example.data.model.BookFormat
import com.example.data.model.BookReadingLayout
import com.example.data.model.CivilSubject
import com.example.data.model.PaperTheme
import com.example.data.pdf.PdfBookSession
import com.example.ui.components.BookOpeningOverlay
import com.example.ui.components.InteractiveBookPageTurner
import com.example.ui.components.InteractiveOpenBookSpreadTurner
import com.example.ui.components.JumpToPageDialog
import com.example.ui.components.PdfPageThumbnailsSheet
import com.example.ui.components.ReaderBookmarksAndContentsSheet
import com.example.ui.components.ReaderBottomBarOverlay
import com.example.ui.components.ReaderReviewNoteSheet
import com.example.ui.components.ReaderTopBarOverlay
import com.example.ui.components.ReaderVisualPalette
import com.example.ui.components.RealPdfBookPageView
import com.example.ui.theme.BlueprintNightBg
import com.example.ui.theme.BlueprintNightInk
import com.example.ui.theme.BlueprintNightSubtle
import com.example.ui.theme.BlueprintNightSurface
import com.example.ui.theme.CrispPaperBg
import com.example.ui.theme.CrispPaperInk
import com.example.ui.theme.CrispPaperSubtle
import com.example.ui.theme.CrispPaperSurface
import com.example.ui.theme.DraftingBrass
import com.example.ui.theme.DraftingBrassLight
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NewsreaderFontFamily
import com.example.ui.theme.ParchmentBg
import com.example.ui.theme.ParchmentInk
import com.example.ui.theme.ParchmentSubtle
import com.example.ui.theme.ParchmentSurface
import java.io.File

@Composable
fun BookReaderScreen(
    book: BookEntity?,
    pages: List<BookPageEntity>,
    bookmarks: List<BookmarkEntity>,
    initialPageNumber: Int,
    preferences: ReaderPreferences,
    onBack: () -> Unit,
    onPageVisited: (Int) -> Unit,
    onToggleBookmark: (pageNumber: Int, chapterTitle: String) -> Unit,
    onSaveReviewNote: (pageNumber: Int, chapterTitle: String, noteText: String) -> Unit,
    onSelectPaperTheme: (PaperTheme) -> Unit,
    onSelectReadingLayout: (BookReadingLayout) -> Unit,
    onToggleCropPdfMargins: (Boolean) -> Unit,
    onAdjustFontSize: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (book == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Keep screen awake while studying technical pages/diagrams
    val context = LocalContext.current
    DisposableEffect(context) {
        val window = context.findActivity()?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val isPdfMode = book.formatType == BookFormat.PDF.name && !book.localPdfPath.isNullOrBlank()
    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }

    // Open native Android PdfRenderer session cleanly without race conditions
    var pdfSession by remember(book.localPdfPath) { mutableStateOf<PdfBookSession?>(null) }
    var sessionReadyVersion by remember(book.localPdfPath) { mutableIntStateOf(0) }
    var pdfPageCount by remember(book.localPdfPath) { mutableIntStateOf(book.totalPages) }
    var pdfLoadError by remember(book.localPdfPath) { mutableStateOf<String?>(null) }

    DisposableEffect(book.localPdfPath, isPdfMode) {
        var activeSession: PdfBookSession? = null
        if (isPdfMode && book.localPdfPath != null) {
            activeSession = PdfBookSession(File(book.localPdfPath))
            pdfSession = activeSession
        }
        onDispose {
            activeSession?.close()
            if (pdfSession === activeSession) pdfSession = null
        }
    }

    LaunchedEffect(pdfSession) {
        val session = pdfSession ?: return@LaunchedEffect
        session.open().onSuccess { count ->
            pdfPageCount = count
            pdfLoadError = null
            sessionReadyVersion += 1
        }.onFailure { err ->
            pdfLoadError = err.message ?: "Unable to open PDF document"
        }
    }

    val effectivePageCount = if (isPdfMode) pdfPageCount.coerceAtLeast(1) else pages.size.coerceAtLeast(1)
    val safeInitialIndex = (initialPageNumber - 1).coerceIn(0, (effectivePageCount - 1).coerceAtLeast(0))
    var currentIndex by rememberSaveable(book.id) { mutableIntStateOf(safeInitialIndex) }
    var showOpeningAnimation by remember(book.id) { mutableStateOf(true) }
    var showChrome by remember { mutableStateOf(true) }
    var showBookmarksDrawer by remember { mutableStateOf(false) }
    var showPageGridSheet by remember { mutableStateOf(false) }
    var showNoteSheet by remember { mutableStateOf(false) }
    var showGoToPageDialog by remember { mutableStateOf(false) }

    // Zoom & pan state for inspecting technical PDF diagrams & formulas
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(currentIndex) {
        zoomScale = 1f
        panOffset = Offset.Zero
        onPageVisited(currentIndex + 1)
    }

    val currentPageNumber = currentIndex + 1
    val currentChapterLabel = if (isPdfMode) {
        "Page $currentPageNumber of $effectivePageCount"
    } else {
        pages.getOrNull(currentIndex)?.chapterTitle ?: "Page $currentPageNumber"
    }
    val activeBookmark = remember(bookmarks, currentPageNumber) {
        bookmarks.firstOrNull { it.pageNumber == currentPageNumber }
    }
    val bookmarkedPagesSet = remember(bookmarks) { bookmarks.map { it.pageNumber }.toSet() }

    val palette = remember(preferences.paperTheme, subject) {
        when (preferences.paperTheme) {
            PaperTheme.PARCHMENT -> ReaderVisualPalette(
                deskBg = Color(0xFF231D17),
                hardcoverBoard = Color(0xFF3E2D20),
                bg = ParchmentBg,
                card = ParchmentSurface,
                ink = ParchmentInk,
                subtle = ParchmentSubtle,
                accent = subject.accentColor,
                border = Color(0xFFD5C8AE)
            )
            PaperTheme.CRISP -> ReaderVisualPalette(
                deskBg = Color(0xFF1F2733),
                hardcoverBoard = Color(0xFF283A52),
                bg = CrispPaperBg,
                card = CrispPaperSurface,
                ink = CrispPaperInk,
                subtle = CrispPaperSubtle,
                accent = subject.accentColor,
                border = Color(0xFFDCE1EA)
            )
            PaperTheme.BLUEPRINT_DARK -> ReaderVisualPalette(
                deskBg = Color(0xFF070F19),
                hardcoverBoard = Color(0xFF1A2F4A),
                bg = BlueprintNightBg,
                card = BlueprintNightSurface,
                ink = BlueprintNightInk,
                subtle = BlueprintNightSubtle,
                accent = Color(0xFF7CC0FF),
                border = Color(0xFF284261)
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(palette.deskBg)
    ) {
        val isWideViewport = maxWidth > maxHeight && maxWidth >= 560.dp
        val useTwoPageSpread = isPdfMode && effectivePageCount >= 2 && when (preferences.readingLayout) {
            BookReadingLayout.AUTO -> isWideViewport
            BookReadingLayout.SINGLE_PAGE -> false
            BookReadingLayout.TWO_PAGE_SPREAD -> true
        }

        val spreadLeftIndex = (currentIndex / 2) * 2
        val spreadPageStep = if (useTwoPageSpread) 2 else 1

        val topBarPageLabel = if (useTwoPageSpread) {
            val leftNum = spreadLeftIndex + 1
            val rightNum = (spreadLeftIndex + 2).coerceAtMost(effectivePageCount)
            if (rightNum > leftNum) "Pages $leftNum–$rightNum of $effectivePageCount"
            else "Page $leftNum of $effectivePageCount"
        } else {
            val pct = (currentPageNumber * 100) / effectivePageCount.coerceAtLeast(1)
            "Page $currentPageNumber of $effectivePageCount ($pct%)"
        }

        if (pdfLoadError != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Unable to Open PDF Book",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = pdfLoadError.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) { Text("Return to Library") }
            }
        } else if (!isPdfMode && pages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No offline pages found for this volume.", color = Color.White)
            }
        } else if (useTwoPageSpread) {
            InteractiveOpenBookSpreadTurner(
                leftPageIndex = spreadLeftIndex,
                pageCount = effectivePageCount,
                paperColor = palette.bg,
                spineBorderColor = palette.border,
                hardcoverBoardColor = palette.hardcoverBoard,
                allowHorizontalSwipe = zoomScale <= 1.05f,
                onSpreadChanged = { newLeft ->
                    currentIndex = newLeft.coerceIn(0, effectivePageCount - 1)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) { pageIdx, isRightHalf ->
                val isVisibleActive = pageIdx == spreadLeftIndex || pageIdx == spreadLeftIndex + 1
                RealPdfBookPageView(
                    session = pdfSession,
                    sessionReadyVersion = sessionReadyVersion,
                    pageIndex = pageIdx,
                    totalPages = effectivePageCount,
                    paperTheme = preferences.paperTheme,
                    backgroundColor = palette.bg,
                    accentColor = palette.accent,
                    subtleColor = palette.subtle,
                    cropMargins = preferences.cropPdfMargins,
                    zoomScale = if (isVisibleActive) zoomScale else 1f,
                    panOffset = if (isVisibleActive) panOffset else Offset.Zero,
                    isActivePage = isVisibleActive,
                    isSpreadHalf = true,
                    onZoomPanChange = { s, o ->
                        zoomScale = s
                        panOffset = o
                    },
                    onTapPreviousPage = {
                        if (isRightHalf) {
                            showChrome = !showChrome
                        } else if (spreadLeftIndex >= 2) {
                            currentIndex = spreadLeftIndex - 2
                        }
                    },
                    onTapNextPage = {
                        if (!isRightHalf) {
                            showChrome = !showChrome
                        } else if (spreadLeftIndex + 2 < effectivePageCount) {
                            currentIndex = spreadLeftIndex + 2
                        }
                    },
                    onCenterTap = { showChrome = !showChrome }
                )
            }
        } else {
            InteractiveBookPageTurner(
                currentPageIndex = currentIndex.coerceIn(0, effectivePageCount - 1),
                pageCount = effectivePageCount,
                paperColor = palette.bg,
                spineBorderColor = palette.border,
                hardcoverBoardColor = palette.hardcoverBoard,
                allowHorizontalSwipe = zoomScale <= 1.05f,
                onPageChanged = { newIdx ->
                    currentIndex = newIdx.coerceIn(0, effectivePageCount - 1)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) { pageIdx ->
                if (isPdfMode) {
                    RealPdfBookPageView(
                        session = pdfSession,
                        sessionReadyVersion = sessionReadyVersion,
                        pageIndex = pageIdx,
                        totalPages = effectivePageCount,
                        paperTheme = preferences.paperTheme,
                        backgroundColor = palette.bg,
                        accentColor = palette.accent,
                        subtleColor = palette.subtle,
                        cropMargins = preferences.cropPdfMargins,
                        zoomScale = if (pageIdx == currentIndex) zoomScale else 1f,
                        panOffset = if (pageIdx == currentIndex) panOffset else Offset.Zero,
                        isActivePage = pageIdx == currentIndex,
                        onZoomPanChange = { s, o ->
                            zoomScale = s
                            panOffset = o
                        },
                        onTapPreviousPage = {
                            if (currentIndex > 0) currentIndex -= 1
                        },
                        onTapNextPage = {
                            if (currentIndex < effectivePageCount - 1) currentIndex += 1
                        },
                        onCenterTap = { showChrome = !showChrome }
                    )
                } else {
                    val page = pages[pageIdx.coerceIn(0, pages.lastIndex)]
                    TextBookPageContent(
                        page = page,
                        totalPages = pages.size,
                        fontSizeSp = preferences.fontSizeSp,
                        palette = palette,
                        existingNote = bookmarks.firstOrNull { it.pageNumber == page.pageNumber }?.noteText,
                        onCenterTap = { showChrome = !showChrome }
                    )
                }
            }
        }

        // Floating Reset Zoom Pill when student is zoomed into a diagram or formula
        if (isPdfMode && zoomScale > 1.08f && !showOpeningAnimation) {
            Surface(
                onClick = {
                    zoomScale = 1f
                    panOffset = Offset.Zero
                },
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(999.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 66.dp, end = 16.dp)
                    .testTag("reset_zoom_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.ZoomOutMap,
                        contentDescription = "Reset zoom",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${String.format("%.1f", zoomScale)}× · Reset",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }

        // Collapsible Top Reader Chrome
        AnimatedVisibility(
            visible = showChrome && !showOpeningAnimation,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ReaderTopBarOverlay(
                book = book,
                currentPageLabel = topBarPageLabel,
                isPdfMode = isPdfMode,
                isBookmarked = activeBookmark != null,
                palette = palette,
                onBack = onBack,
                onShowPageGrid = { showPageGridSheet = true },
                onShowJumpDialog = { showGoToPageDialog = true },
                onToggleBookmark = { onToggleBookmark(currentPageNumber, currentChapterLabel) },
                onShowNoteSheet = { showNoteSheet = true },
                onShowBookmarksSheet = { showBookmarksDrawer = true }
            )
        }

        // Collapsible Bottom Reader Controls + Live Thumbnail Scrubber Preview
        AnimatedVisibility(
            visible = showChrome && !showOpeningAnimation,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderBottomBarOverlay(
                currentIndex = currentIndex,
                effectivePageCount = effectivePageCount,
                isPdfMode = isPdfMode,
                isTwoPageSpreadActive = useTwoPageSpread,
                preferences = preferences,
                pdfSession = pdfSession,
                palette = palette,
                onPrevious = {
                    currentIndex = (currentIndex - spreadPageStep).coerceAtLeast(0)
                },
                onNext = {
                    currentIndex = (currentIndex + spreadPageStep).coerceAtMost(effectivePageCount - 1)
                },
                onScrubToIndex = { targetIdx ->
                    currentIndex = targetIdx.coerceIn(0, effectivePageCount - 1)
                },
                onSelectPaperTheme = onSelectPaperTheme,
                onToggleSpreadLayout = {
                    val nextLayout = if (useTwoPageSpread) {
                        BookReadingLayout.SINGLE_PAGE
                    } else {
                        BookReadingLayout.TWO_PAGE_SPREAD
                    }
                    onSelectReadingLayout(nextLayout)
                },
                onToggleCropMargins = {
                    onToggleCropPdfMargins(!preferences.cropPdfMargins)
                },
                onAdjustFontSize = onAdjustFontSize
            )
        }

        // Signature 3D Hardcover Book Opening Animation Overlay
        if (showOpeningAnimation) {
            BookOpeningOverlay(
                book = book,
                onAnimationComplete = { showOpeningAnimation = false }
            )
        }
    }

    if (showPageGridSheet && isPdfMode) {
        PdfPageThumbnailsSheet(
            session = pdfSession,
            totalPages = effectivePageCount,
            currentIndex = currentIndex,
            bookmarkedPages = bookmarkedPagesSet,
            paperTheme = preferences.paperTheme,
            onSelectPageIndex = { idx ->
                currentIndex = idx.coerceIn(0, effectivePageCount - 1)
                showPageGridSheet = false
            },
            onDismiss = { showPageGridSheet = false }
        )
    }

    if (showGoToPageDialog) {
        JumpToPageDialog(
            currentPageNumber = currentPageNumber,
            effectivePageCount = effectivePageCount,
            onJumpToPage = { pageNum ->
                currentIndex = (pageNum - 1).coerceIn(0, effectivePageCount - 1)
            },
            onDismiss = { showGoToPageDialog = false }
        )
    }

    if (showBookmarksDrawer) {
        ReaderBookmarksAndContentsSheet(
            isPdfMode = isPdfMode,
            bookmarks = bookmarks,
            pages = pages,
            onSelectPageNumber = { pageNum ->
                currentIndex = (pageNum - 1).coerceIn(0, effectivePageCount - 1)
                showBookmarksDrawer = false
            },
            onDismiss = { showBookmarksDrawer = false }
        )
    }

    if (showNoteSheet) {
        ReaderReviewNoteSheet(
            currentPageNumber = currentPageNumber,
            initialNote = activeBookmark?.noteText.orEmpty(),
            onSave = { noteDraft ->
                onSaveReviewNote(currentPageNumber, currentChapterLabel, noteDraft)
            },
            onDismiss = { showNoteSheet = false }
        )
    }
}

@Composable
private fun TextBookPageContent(
    page: BookPageEntity,
    totalPages: Int,
    fontSizeSp: Float,
    palette: ReaderVisualPalette,
    existingNote: String?,
    onCenterTap: () -> Unit
) {
    val scrollState = rememberScrollState()
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onCenterTap
            )
            .padding(start = 26.dp, end = 24.dp, top = 74.dp, bottom = 132.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = page.sectionLabel,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = palette.accent
            )
            Text(
                text = "${page.pageNumber} / $totalPages",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 10.sp,
                color = palette.subtle
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = page.chapterTitle,
            fontFamily = NewsreaderFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (fontSizeSp + 6f).sp,
            lineHeight = (fontSizeSp + 12f).sp,
            color = palette.ink
        )
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = palette.border)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.contentBody,
            fontFamily = NewsreaderFontFamily,
            fontSize = fontSizeSp.sp,
            lineHeight = (fontSizeSp * 1.56f).sp,
            color = palette.ink
        )

        if (!page.formulaBlock.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.card)
                    .border(1.dp, palette.accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Functions,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KEY EQUATIONS & REFERENCE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.accent
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = page.formulaBlock,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = (fontSizeSp - 2.5f).coerceAtLeast(12f).sp,
                        lineHeight = (fontSizeSp + 3f).sp,
                        color = palette.ink
                    )
                }
            }
        }

        if (!page.workedExample.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DraftingBrassLight.copy(alpha = 0.14f))
                    .border(1.dp, DraftingBrass.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = DraftingBrass,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WORKED EXAMPLE",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DraftingBrass
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = page.workedExample,
                        fontFamily = NewsreaderFontFamily,
                        fontSize = (fontSizeSp - 1f).coerceAtLeast(13f).sp,
                        lineHeight = (fontSizeSp * 1.45f).sp,
                        color = palette.ink
                    )
                }
            }
        }

        if (!existingNote.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.accent.copy(alpha = 0.10f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "YOUR STUDY NOTE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.accent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = existingNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.ink
                    )
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
