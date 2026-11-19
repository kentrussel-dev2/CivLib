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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookEntity
import com.example.data.local.BookPageEntity
import com.example.data.local.BookmarkEntity
import com.example.data.model.BookFormat
import com.example.data.model.BookSourceType
import com.example.data.model.CivilSubject
import com.example.data.model.DownloadProgress
import com.example.data.model.formatFileSize
import com.example.ui.components.HardcoverBookThumbnail
import com.example.ui.theme.NewsreaderFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: BookEntity?,
    pages: List<BookPageEntity>,
    bookmarks: List<BookmarkEntity>,
    downloadProgress: DownloadProgress?,
    isBookmarked: Boolean = false,
    isCompleted: Boolean = false,
    onBack: () -> Unit,
    onOpenReader: (pageNumber: Int) -> Unit,
    onDownloadBook: (BookEntity) -> Unit,
    onCancelDownload: (String) -> Unit,
    onRemoveOffline: (BookEntity) -> Unit,
    onToggleBookmark: (BookEntity) -> Unit = {},
    onToggleCompleted: (BookEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (book == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Loading volume record…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        return
    }

    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }
    val sourceLabel = remember(book.sourceType) {
        BookSourceType.entries.firstOrNull { it.name == book.sourceType }?.displayLabel
            ?: "Open-Access Repository"
    }
    val isPdf = book.formatType == BookFormat.PDF.name

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "YEAR ${subject.curriculumYear} · ${book.courseCode} / ${subject.shortName.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("detail_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to library",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { onToggleCompleted(book) },
                            modifier = Modifier.testTag("detail_completed_button")
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = if (isCompleted) "Mark in progress" else "Mark completed",
                                tint = if (isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onToggleBookmark(book) },
                            modifier = Modifier.testTag("detail_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Remove from bookmarks" else "Save to bookmarks",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Asymmetric Gallery Monograph Showcase
            item(key = "showcase") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
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
                        modifier = Modifier.width(134.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPdf) "FULL PDF MONOGRAPH" else "FULL PUBLIC TEXT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = book.title,
                            fontFamily = NewsreaderFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp,
                            lineHeight = 27.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = book.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (book.isOfflineAvailable) {
                                buildString {
                                    append("OFFLINE READY")
                                    if (book.fileSizeBytes > 0L) append(" · ${formatFileSize(book.fileSizeBytes)}")
                                }
                            } else {
                                "OPEN ACCESS · DOWNLOADABLE"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (book.isOfflineAvailable) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Primary Action Row
            item(key = "actions") {
                Spacer(modifier = Modifier.height(22.dp))
                if (downloadProgress != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = downloadProgress.statusText,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = downloadProgress.formattedSize,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val frac = downloadProgress.fraction
                        if (frac != null) {
                            LinearProgressIndicator(
                                progress = { frac },
                                modifier = Modifier.fillMaxWidth().height(3.dp)
                            )
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(3.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onCancelDownload(book.id) },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel")
                        }
                    }
                } else if (book.isOfflineAvailable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onOpenReader(book.currentPage) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("open_book_reader_button"),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (book.lastOpenedAt > 0L) {
                                    "Open Book · Page ${book.currentPage}"
                                } else {
                                    "Open Full Book"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = { onRemoveOffline(book) },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Remove offline download",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { onDownloadBook(book) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("download_full_pdf_button"),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Download & Read Full PDF Book",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Abstract / Overview
            item(key = "abstract") {
                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    text = "SYNOPSIS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Swiss Tabular Bibliographic Specification Sheet
            item(key = "metadata_specs") {
                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    text = "BIBLIOGRAPHIC INDEX",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                MetadataRow("ISBN / ID", book.isbn)
                MetadataRow("Course", "${book.courseCode} — ${subject.displayName}")
                MetadataRow(
                    "Extent",
                    if (book.totalPages > 0) "${book.totalPages} pages" else "Detected on download"
                )
                if (book.fileSizeBytes > 0L) {
                    MetadataRow("File Size", formatFileSize(book.fileSizeBytes))
                }
                MetadataRow("Edition", "${book.edition} (${book.publishYear})")
                MetadataRow("Publisher", book.publisher)
                MetadataRow("Archive", sourceLabel)
            }

            // Saved Bookmarks & Notes
            if (bookmarks.isNotEmpty() && book.isOfflineAvailable) {
                item(key = "book_bookmarks_header") {
                    Spacer(modifier = Modifier.height(26.dp))
                    Text(
                        text = "BOOKMARKS & NOTES (${bookmarks.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                }
                items(bookmarks, key = { "bm_${it.id}" }) { bm ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenReader(bm.pageNumber) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Page ${bm.pageNumber} · ${bm.chapterTitle}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                if (bm.noteText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = bm.noteText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Open page",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }

            // Full-text sections index (Project Gutenberg)
            if (pages.isNotEmpty() && !isPdf) {
                item(key = "toc_heading") {
                    Spacer(modifier = Modifier.height(26.dp))
                    Text(
                        text = "SECTIONS (${pages.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                }
                items(pages.take(30), key = { it.id }) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenReader(page.pageNumber) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "P.${page.pageNumber.toString().padStart(3, '0')}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = page.chapterTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
    HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
}
