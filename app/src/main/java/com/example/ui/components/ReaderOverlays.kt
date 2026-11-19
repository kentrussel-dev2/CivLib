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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
    palette: ReaderVisualPalette,
    onBack: () -> Unit,
    onShowPageGrid: () -> Unit,
    onShowJumpDialog: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShowNoteSheet: () -> Unit,
    onShowBookmarksSheet: () -> Unit
) {
    Surface(
        color = palette.card.copy(alpha = 0.96f),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 6.dp),
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
            if (isPdfMode) {
                IconButton(
                    onClick = onShowPageGrid,
                    modifier = Modifier.testTag("reader_page_grid_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = "Browse book pages visually",
                        tint = palette.ink
                    )
                }
            }
            IconButton(
                onClick = onShowJumpDialog,
                modifier = Modifier.testTag("reader_jump_page_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Pin,
                    contentDescription = "Jump to page number",
                    tint = palette.ink
                )
            }
            IconButton(
                onClick = onToggleBookmark,
                modifier = Modifier.testTag("reader_bookmark_button")
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark
                    else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark page",
                    tint = if (isBookmarked) DraftingBrass else palette.ink
                )
            }
            IconButton(
                onClick = onShowNoteSheet,
                modifier = Modifier.testTag("reader_note_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.EditNote,
                    contentDescription = "Add study review note",
                    tint = palette.ink
                )
            }
            IconButton(
                onClick = onShowBookmarksSheet,
                modifier = Modifier.testTag("reader_toc_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Contents and bookmarks",
                    tint = palette.ink
                )
            }
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

                    // Book-mode controls (PDF: 2-Page Spread + Margin Trim; Text: A-/A+)
                    if (isPdfMode) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                        contentDescription = "Toggle 2-page book spread",
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
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
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
                .height(480.dp)
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
                    val thumbBmp by produceState<Bitmap?>(initialValue = null, session, idx) {
                        value = session?.renderThumbnail(idx, targetWidthPx = 200)
                    }

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
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                            text = "No bookmarks saved in this PDF yet. Tap the bookmark icon on any equation, table, or diagram page to pin it here.",
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
