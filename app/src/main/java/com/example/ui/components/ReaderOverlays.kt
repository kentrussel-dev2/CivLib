package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookEntity
import com.example.data.local.BookPageEntity
import com.example.data.local.BookmarkEntity
import com.example.data.local.ReaderPreferences
import com.example.data.model.BookReadingLayout
import com.example.data.model.PaperTheme
import com.example.data.pdf.PdfBookSession
import com.example.ui.theme.DraftingBrass
import com.example.ui.theme.JetBrainsMonoFontFamily

data class ReaderVisualPalette(
    val deskBg: Color,
    val hardcoverBoard: Color,
    val bg: Color,
    val card: Color,
    val ink: Color,
    val subtle: Color,
    val accent: Color,
    val border: Color
)

@Composable
fun ReaderTopBarOverlay(
    book: BookEntity,
    currentPageLabel: String,
    isPdfMode: Boolean,
    isBookmarked: Boolean,
    isTwoPageSpreadActive: Boolean = false,
    canToggleSpread: Boolean = true,
    palette: ReaderVisualPalette,
    onBack: () -> Unit,
    onToggleSpreadLayout: () -> Unit = {},
    onShowPageGrid: () -> Unit,
    onShowJumpDialog: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShowNoteSheet: () -> Unit,
    onShowBookmarksSheet: () -> Unit,
    onOpenAiAssistant: () -> Unit = {}
) {
    var showHamburgerMenu by remember { mutableStateOf(false) }

    Surface(
        color = palette.card.copy(alpha = 0.96f),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("reader_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close reader",
                    tint = palette.ink
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${book.courseCode} · $currentPageLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.subtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Single clean Hamburger Menu button with labeled dropdown tools (including AI Assistant)
            Box {
                IconButton(
                    onClick = { showHamburgerMenu = true },
                    modifier = Modifier.testTag("reader_hamburger_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Reader tools menu",
                        tint = palette.ink
                    )
                }

                ReaderToolsHamburgerDropdown(
                    expanded = showHamburgerMenu,
                    onDismiss = { showHamburgerMenu = false },
                    isPdfMode = isPdfMode,
                    isBookmarked = isBookmarked,
                    isTwoPageSpreadActive = isTwoPageSpreadActive,
                    canToggleSpread = canToggleSpread,
                    palette = palette,
                    onOpenAiAssistant = onOpenAiAssistant,
                    onShowPageGrid = onShowPageGrid,
                    onShowJumpDialog = onShowJumpDialog,
                    onToggleBookmark = onToggleBookmark,
                    onShowNoteSheet = onShowNoteSheet,
                    onShowBookmarksSheet = onShowBookmarksSheet,
                    onToggleSpreadLayout = onToggleSpreadLayout
                )
            }
        }
    }
}

@Composable
fun ReaderToolsHamburgerDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isPdfMode: Boolean,
    isBookmarked: Boolean,
    isTwoPageSpreadActive: Boolean,
    canToggleSpread: Boolean,
    palette: ReaderVisualPalette,
    onOpenAiAssistant: () -> Unit,
    onShowPageGrid: () -> Unit,
    onShowJumpDialog: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShowNoteSheet: () -> Unit,
    onShowBookmarksSheet: () -> Unit,
    onToggleSpreadLayout: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = palette.card
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = palette.accent
                )
            },
            text = {
                Column {
                    Text(
                        text = "AI Study Assistant",
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                    Text(
                        text = "Explain page, find topics in book, or save notes",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.subtle
                    )
                }
            },
            onClick = {
                onDismiss()
                onOpenAiAssistant()
            },
            modifier = Modifier.testTag("menu_ai_assistant")
        )

        HorizontalDivider(color = palette.border.copy(alpha = 0.5f))

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.GridView,
                    contentDescription = null,
                    tint = palette.ink
                )
            },
            text = {
                Column {
                    Text("Book Pages Preview", fontWeight = FontWeight.SemiBold, color = palette.ink)
                    Text(
                        text = "Browse visual thumbnails of all pages",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.subtle
                    )
                }
            },
            onClick = {
                onDismiss()
                onShowPageGrid()
            },
            modifier = Modifier.testTag("reader_page_grid_button")
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Pin,
                    contentDescription = null,
                    tint = palette.ink
                )
            },
            text = {
                Column {
                    Text("Jump to Page Number", fontWeight = FontWeight.SemiBold, color = palette.ink)
                    Text(
                        text = "Type a page number to flip there",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.subtle
                    )
                }
            },
            onClick = {
                onDismiss()
                onShowJumpDialog()
            },
            modifier = Modifier.testTag("reader_jump_page_button")
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = if (isBookmarked) DraftingBrass else palette.ink
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isBookmarked) "Remove Page Bookmark" else "Bookmark This Page",
                        fontWeight = FontWeight.SemiBold,
                        color = palette.ink
                    )
                    Text(
                        text = "Pin page to your personal bookmarks",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.subtle
                    )
                }
            },
            onClick = {
                onDismiss()
                onToggleBookmark()
            },
            modifier = Modifier.testTag("reader_bookmark_button")
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.EditNote,
                    contentDescription = null,
                    tint = palette.ink
                )
            },
            text = {
                Column {
                    Text("Add / Edit Study Note", fontWeight = FontWeight.SemiBold, color = palette.ink)
                    Text(
                        text = "Write formula or exam note for this page",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.subtle
                    )
                }
            },
            onClick = {
                onDismiss()
                onShowNoteSheet()
            },
            modifier = Modifier.testTag("reader_note_button")
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = palette.ink
                )
            },
            text = {
                Column {
                    Text("Saved Bookmarks & Notes", fontWeight = FontWeight.SemiBold, color = palette.ink)
                    Text(
                        text = "View all bookmarked pages in this book",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.subtle
                    )
                }
            },
            onClick = {
                onDismiss()
                onShowBookmarksSheet()
            },
            modifier = Modifier.testTag("reader_toc_button")
        )

        if (canToggleSpread) {
            HorizontalDivider(color = palette.border.copy(alpha = 0.5f))
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ChromeReaderMode,
                        contentDescription = null,
                        tint = palette.ink
                    )
                },
                text = {
                    Column {
                        Text(
                            text = if (isTwoPageSpreadActive) "Switch to 1-Page Portrait"
                            else "Switch to 2-Page Landscape",
                            fontWeight = FontWeight.SemiBold,
                            color = palette.ink
                        )
                        Text(
                            text = if (isTwoPageSpreadActive) "Return to single-page portrait view"
                            else "Open two-page landscape textbook spread",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.subtle
                        )
                    }
                },
                onClick = {
                    onDismiss()
                    onToggleSpreadLayout()
                },
                modifier = Modifier.testTag("reader_top_spread_toggle")
            )
        }
    }
}

@Composable
fun ReaderBottomBarOverlay(
    currentIndex: Int,
    effectivePageCount: Int,
    isPdfMode: Boolean,
    isTwoPageSpreadActive: Boolean,
    preferences: ReaderPreferences,
    pdfSession: PdfBookSession?,
    palette: ReaderVisualPalette,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onScrubToIndex: (Int) -> Unit,
    onSelectPaperTheme: (PaperTheme) -> Unit,
    onToggleSpreadLayout: () -> Unit,
    onToggleCropMargins: () -> Unit,
    onAdjustFontSize: (Float) -> Unit
) {
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubValue by remember(currentIndex) { mutableFloatStateOf(currentIndex.toFloat()) }
    val previewIndex = scrubValue.toInt().coerceIn(0, (effectivePageCount - 1).coerceAtLeast(0))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Live visual thumbnail preview card while dragging page slider across a 300+ page PDF
        if (isScrubbing && effectivePageCount > 1) {
            val previewBmp by produceState<Bitmap?>(initialValue = null, pdfSession, previewIndex, isPdfMode) {
                if (isPdfMode && pdfSession != null) {
                    value = pdfSession.renderThumbnail(previewIndex, targetWidthPx = 180)
                }
            }
            Surface(
                color = palette.card,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .border(1.dp, palette.border, RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isPdfMode) {
                        Box(
                            modifier = Modifier
                                .width(84.dp)
                                .height(112.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.bg)
                                .border(0.5.dp, palette.border, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            val bmp = previewBmp
                            if (bmp != null && !bmp.isRecycled) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Preview page ${previewIndex + 1}",
                                    contentScale = ContentScale.Fit,
                                    colorFilter = pdfColorFilterForTheme(preferences.paperTheme),
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    val pct = ((previewIndex + 1) * 100) / effectivePageCount.coerceAtLeast(1)
                    Text(
                        text = "Page ${previewIndex + 1} / $effectivePageCount ($pct%)",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.ink
                    )
                }
            }
        }

        Surface(
            color = palette.card.copy(alpha = 0.97f),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPrevious,
                        enabled = currentIndex > 0,
                        modifier = Modifier.testTag("reader_prev_page_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous page",
                            tint = if (currentIndex > 0) palette.ink else palette.subtle.copy(alpha = 0.4f)
                        )
                    }

                    if (effectivePageCount > 1) {
                        Slider(
                            value = if (isScrubbing) scrubValue else currentIndex.toFloat(),
                            onValueChange = { nextVal ->
                                isScrubbing = true
                                scrubValue = nextVal
                            },
                            onValueChangeFinished = {
                                isScrubbing = false
                                onScrubToIndex(scrubValue.toInt().coerceIn(0, effectivePageCount - 1))
                            },
                            valueRange = 0f..(effectivePageCount - 1).toFloat(),
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    IconButton(
                        onClick = onNext,
                        enabled = currentIndex < effectivePageCount - 1,
                        modifier = Modifier.testTag("reader_next_page_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next page",
                            tint = if (currentIndex < effectivePageCount - 1) palette.ink
                            else palette.subtle.copy(alpha = 0.4f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Book Paper Theme selector
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        PaperTheme.entries.forEach { theme ->
                            val selected = theme == preferences.paperTheme
                            Surface(
                                onClick = { onSelectPaperTheme(theme) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) palette.accent else palette.bg,
                                modifier = Modifier
                                    .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                                    .testTag("paper_theme_${theme.name.lowercase()}")
                            ) {
                                Text(
                                    text = when (theme) {
                                        PaperTheme.PARCHMENT -> "Book"
                                        PaperTheme.CRISP -> "Crisp"
                                        PaperTheme.BLUEPRINT_DARK -> "Night"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White else palette.ink,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Book-mode controls (2-Page Landscape Spread for all books + Margin Trim for PDF / A- A+ for Text)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (effectivePageCount >= 2) {
                            Surface(
                                onClick = onToggleSpreadLayout,
                                shape = RoundedCornerShape(8.dp),
                                color = if (isTwoPageSpreadActive) palette.accent else palette.bg,
                                modifier = Modifier
                                    .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                                    .testTag("reader_spread_toggle")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ChromeReaderMode,
                                        contentDescription = "Toggle 2-page landscape spread",
                                        tint = if (isTwoPageSpreadActive) Color.White else palette.ink,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isTwoPageSpreadActive) "2-Page" else "1-Page",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isTwoPageSpreadActive) Color.White else palette.ink
                                    )
                                }
                            }
                        }

                        if (isPdfMode) {
                            Surface(
                                onClick = onToggleCropMargins,
                                shape = RoundedCornerShape(8.dp),
                                color = if (preferences.cropPdfMargins) palette.accent else palette.bg,
                                modifier = Modifier
                                    .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                                    .testTag("reader_crop_margins_toggle")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Crop,
                                        contentDescription = "Trim wide PDF margins",
                                        tint = if (preferences.cropPdfMargins) Color.White else palette.ink,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Trim",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (preferences.cropPdfMargins) Color.White else palette.ink
                                    )
                                }
                            }
                        } else {
                            Surface(
                                onClick = { onAdjustFontSize(-1.5f) },
                                shape = RoundedCornerShape(8.dp),
                                color = palette.bg,
                                modifier = Modifier.border(1.dp, palette.border, RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = "A-",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.ink,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Surface(
                                onClick = { onAdjustFontSize(+1.5f) },
                                shape = RoundedCornerShape(8.dp),
                                color = palette.bg,
                                modifier = Modifier.border(1.dp, palette.border, RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = "A+",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.ink,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPageThumbnailsSheet(
    session: PdfBookSession?,
    totalPages: Int,
    currentIndex: Int,
    bookmarkedPages: Set<Int>,
    paperTheme: PaperTheme,
    pages: List<BookPageEntity> = emptyList(),
    bookTitle: String = "Civil Engineering Volume",
    courseCode: String = "CE",
    onSelectPageIndex: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val gridState = rememberLazyGridState()
    LaunchedEffect(Unit) {
        if (currentIndex > 2) {
            gridState.scrollToItem((currentIndex - 1).coerceAtLeast(0))
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Book Pages ($totalPages)",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Tap any page to flip there",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(count = totalPages, key = { idx -> "thumb_$idx" }) { idx ->
                    val isCurrent = idx == currentIndex
                    val isBookmarked = (idx + 1) in bookmarkedPages
                    val cachedThumb = remember(session, idx) {
                        session?.getCachedThumbnail(idx)
                    }
                    val asyncThumb by produceState<Bitmap?>(initialValue = cachedThumb, session, idx) {
                        value = session?.getCachedThumbnail(idx)
                            ?: session?.renderThumbnail(idx, targetWidthPx = 240)
                    }
                    val thumbBmp = cachedThumb ?: asyncThumb
                    val pageRecord = pages.getOrNull(idx)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectPageIndex(idx) }
                            .testTag("page_thumb_$idx")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.72f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF7F2E7))
                                .border(
                                    width = if (isCurrent) 2.5.dp else 1.dp,
                                    color = if (isCurrent) DraftingBrass else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val bmp = thumbBmp
                            if (bmp != null && !bmp.isRecycled) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Thumbnail page ${idx + 1}",
                                    contentScale = ContentScale.Fit,
                                    colorFilter = pdfColorFilterForTheme(paperTheme),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(2.dp)
                                )
                            } else {
                                // Realistic miniature book page preview while PDF renders or for text volumes
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = courseCode,
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 7.5.sp,
                                                color = Color(0xFF7B412D)
                                            )
                                            Text(
                                                text = "${idx + 1}",
                                                fontFamily = JetBrainsMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 7.5.sp,
                                                color = Color(0xFF5D534A)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = pageRecord?.chapterTitle ?: "$bookTitle · Page ${idx + 1}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.5.sp,
                                            lineHeight = 10.5.sp,
                                            color = Color(0xFF281F1A),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(5.dp))
                                        // Miniature printed text lines representation
                                        val snippet = pageRecord?.contentBody
                                        if (!snippet.isNullOrBlank()) {
                                            Text(
                                                text = snippet,
                                                fontSize = 6.5.sp,
                                                lineHeight = 8.5.sp,
                                                color = Color(0xFF4A3F37),
                                                maxLines = 6,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else {
                                            repeat(6) { lineIdx ->
                                                Box(
                                                    modifier = Modifier
                                                        .padding(vertical = 2.dp)
                                                        .fillMaxWidth(if (lineIdx == 5) 0.62f else 0.96f)
                                                        .height(3.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(Color(0xFFD5CBB8))
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            // Simulated diagram/formula box inside page preview
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(22.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFEAE2D0))
                                                    .border(0.5.dp, Color(0xFFC5B8A2), RoundedCornerShape(4.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "P. ${idx + 1}",
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 8.sp,
                                                    color = Color(0xFF5D534A)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "— ${idx + 1} —",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 7.sp,
                                        color = Color(0xFF7A6E63),
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                }
                            }

                            if (isBookmarked) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = "Bookmarked",
                                    tint = DraftingBrass,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "P. ${idx + 1}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JumpToPageDialog(
    currentPageNumber: Int,
    effectivePageCount: Int,
    onJumpToPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var pageInput by remember { mutableStateOf(currentPageNumber.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Jump to Page") },
        text = {
            Column {
                Text(
                    text = "Enter a page number between 1 and $effectivePageCount.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { value -> pageInput = value.filter { it.isDigit() } },
                    singleLine = true,
                    label = { Text("Page Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("jump_page_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = pageInput.toIntOrNull()
                    if (parsed != null) {
                        onJumpToPage(parsed.coerceIn(1, effectivePageCount))
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_jump_page_button")
            ) { Text("Go") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderBookmarksAndContentsSheet(
    isPdfMode: Boolean,
    bookmarks: List<BookmarkEntity>,
    pages: List<BookPageEntity>,
    onSelectPageNumber: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
            Text(
                text = if (isPdfMode) "Bookmarks & Study Notes" else "Sections & Bookmarks",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (bookmarks.isNotEmpty()) {
                    items(bookmarks, key = { "drawer_bm_${it.id}" }) { bm ->
                        Surface(
                            onClick = { onSelectPageNumber(bm.pageNumber) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Bookmark,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Page ${bm.pageNumber}", style = MaterialTheme.typography.titleSmall)
                                    if (bm.noteText.isNotBlank()) {
                                        Text(bm.noteText, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                } else if (isPdfMode) {
                    item {
                        Text(
                            text = "No bookmarks saved in this PDF yet. Use the Hamburger menu (☰) to bookmark any equation, table, or diagram page.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                }

                if (!isPdfMode && pages.isNotEmpty()) {
                    items(pages.take(60), key = { it.id }) { p ->
                        Surface(
                            onClick = { onSelectPageNumber(p.pageNumber) },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "P.${p.pageNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = p.chapterTitle, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderReviewNoteSheet(
    currentPageNumber: Int,
    initialNote: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var noteDraft by remember(currentPageNumber, initialNote) { mutableStateOf(initialNote) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = "Review Note · Page $currentPageNumber",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = noteDraft,
                onValueChange = { noteDraft = it },
                placeholder = { Text("Save formula notes, exam reminders, or figure references…") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("review_note_text_field")
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
                Button(
                    onClick = {
                        onSave(noteDraft)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_review_note_button")
                ) { Text("Save Note") }
            }
        }
    }
}

/**
 * Left vertical option bar for 2-Page Landscape mode so controls sit cleanly on the left side
 * without covering the top or bottom of the two-page textbook spread.
 * Uses the same single Hamburger Dropdown Menu (with labels & AI Study Assistant).
 */
@Composable
fun ReaderLandscapeLeftBarOverlay(
    isPdfMode: Boolean,
    isBookmarked: Boolean,
    isTwoPageSpreadActive: Boolean,
    canToggleSpread: Boolean,
    canGoPrevious: Boolean,
    palette: ReaderVisualPalette,
    onBack: () -> Unit,
    onToggleSpreadLayout: () -> Unit,
    onShowPageGrid: () -> Unit,
    onShowJumpDialog: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShowNoteSheet: () -> Unit,
    onShowBookmarksSheet: () -> Unit,
    onOpenAiAssistant: () -> Unit = {},
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHamburgerMenu by remember { mutableStateOf(false) }

    Surface(
        color = palette.card.copy(alpha = 0.96f),
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        modifier = modifier
            .width(56.dp)
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("reader_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close reader",
                        tint = palette.ink,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Single Hamburger Menu button with labeled dropdown tools (including AI Assistant)
                Box {
                    Surface(
                        onClick = { showHamburgerMenu = true },
                        shape = RoundedCornerShape(10.dp),
                        color = palette.bg,
                        modifier = Modifier
                            .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                            .testTag("reader_landscape_hamburger_button")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Reader tools menu",
                                tint = palette.ink,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Menu",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = palette.ink
                            )
                        }
                    }

                    ReaderToolsHamburgerDropdown(
                        expanded = showHamburgerMenu,
                        onDismiss = { showHamburgerMenu = false },
                        isPdfMode = isPdfMode,
                        isBookmarked = isBookmarked,
                        isTwoPageSpreadActive = isTwoPageSpreadActive,
                        canToggleSpread = canToggleSpread,
                        palette = palette,
                        onOpenAiAssistant = onOpenAiAssistant,
                        onShowPageGrid = onShowPageGrid,
                        onShowJumpDialog = onShowJumpDialog,
                        onToggleBookmark = onToggleBookmark,
                        onShowNoteSheet = onShowNoteSheet,
                        onShowBookmarksSheet = onShowBookmarksSheet,
                        onToggleSpreadLayout = onToggleSpreadLayout
                    )
                }

                if (canToggleSpread) {
                    Surface(
                        onClick = onToggleSpreadLayout,
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTwoPageSpreadActive) palette.accent else palette.bg,
                        modifier = Modifier
                            .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                            .testTag("reader_top_spread_toggle")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ChromeReaderMode,
                                contentDescription = "Switch to 1-page portrait",
                                tint = if (isTwoPageSpreadActive) Color.White else palette.ink,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isTwoPageSpreadActive) "2P" else "1P",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isTwoPageSpreadActive) Color.White else palette.ink
                            )
                        }
                    }
                }
            }

            Surface(
                onClick = onPrevious,
                enabled = canGoPrevious,
                shape = RoundedCornerShape(10.dp),
                color = if (canGoPrevious) palette.bg else palette.bg.copy(alpha = 0.45f),
                modifier = Modifier
                    .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                    .testTag("reader_prev_page_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(width = 42.dp, height = 38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous pages",
                        tint = if (canGoPrevious) palette.ink else palette.subtle.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

/**
 * Right vertical option bar for 2-Page Landscape mode so paper theme, margin trim, page indicator,
 * and next-page controls sit on the right side without covering the two-page textbook spread.
 */
@Composable
fun ReaderLandscapeRightBarOverlay(
    spreadLeftIndex: Int,
    effectivePageCount: Int,
    isPdfMode: Boolean,
    preferences: ReaderPreferences,
    palette: ReaderVisualPalette,
    canGoNext: Boolean,
    onNext: () -> Unit,
    onShowJumpDialog: () -> Unit,
    onSelectPaperTheme: (PaperTheme) -> Unit,
    onToggleCropMargins: () -> Unit,
    onAdjustFontSize: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val leftNum = spreadLeftIndex + 1
    val rightNum = (spreadLeftIndex + 2).coerceAtMost(effectivePageCount)
    val spreadBadge = if (rightNum > leftNum) "$leftNum–$rightNum" else "$leftNum"

    Surface(
        color = palette.card.copy(alpha = 0.96f),
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
        modifier = modifier
            .width(60.dp)
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Compact Page Indicator Pill (tappable to jump to any page)
            Surface(
                onClick = onShowJumpDialog,
                shape = RoundedCornerShape(8.dp),
                color = palette.bg,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, palette.border, RoundedCornerShape(8.dp))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = spreadBadge,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = palette.ink,
                        maxLines = 1
                    )
                    Text(
                        text = "/$effectivePageCount",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = palette.subtle,
                        maxLines = 1
                    )
                }
            }

            // Vertical Paper Theme Stack (Book / Crisp / Night)
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                PaperTheme.entries.forEach { theme ->
                    val selected = theme == preferences.paperTheme
                    Surface(
                        onClick = { onSelectPaperTheme(theme) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) palette.accent else palette.bg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                            .testTag("paper_theme_${theme.name.lowercase()}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = when (theme) {
                                    PaperTheme.PARCHMENT -> "Book"
                                    PaperTheme.CRISP -> "Crisp"
                                    PaperTheme.BLUEPRINT_DARK -> "Night"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (selected) Color.White else palette.ink
                            )
                        }
                    }
                }
            }

            // Trim PDF Margins or Font Size Controls
            if (isPdfMode) {
                Surface(
                    onClick = onToggleCropMargins,
                    shape = RoundedCornerShape(8.dp),
                    color = if (preferences.cropPdfMargins) palette.accent else palette.bg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                        .testTag("reader_crop_margins_toggle")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Crop,
                            contentDescription = "Trim wide PDF margins",
                            tint = if (preferences.cropPdfMargins) Color.White else palette.ink,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Trim",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.5.sp,
                            color = if (preferences.cropPdfMargins) Color.White else palette.ink
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        onClick = { onAdjustFontSize(+1.5f) },
                        shape = RoundedCornerShape(8.dp),
                        color = palette.bg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                            Text("A+", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                        }
                    }
                    Surface(
                        onClick = { onAdjustFontSize(-1.5f) },
                        shape = RoundedCornerShape(8.dp),
                        color = palette.bg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, palette.border, RoundedCornerShape(8.dp))
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                            Text("A-", style = MaterialTheme.typography.labelSmall, color = palette.ink)
                        }
                    }
                }
            }

            // Next Spread Button
            Surface(
                onClick = onNext,
                enabled = canGoNext,
                shape = RoundedCornerShape(10.dp),
                color = if (canGoNext) palette.bg else palette.bg.copy(alpha = 0.45f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                    .testTag("reader_next_page_button")
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next pages",
                        tint = if (canGoNext) palette.ink else palette.subtle.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

