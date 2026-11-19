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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.CloudStudyNote
import com.example.data.firebase.sanitizeFirestoreDocId
import com.example.data.local.BookEntity
import com.example.data.local.BookmarkEntity
import com.example.data.model.BookSourceType
import com.example.data.model.CivilSubject
import com.example.data.model.formatFileSize
import com.example.ui.components.HardcoverBookThumbnail
import com.example.ui.theme.EspressoBg
import com.example.ui.theme.EspressoCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.NewsreaderFontFamily
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SageOlive
import com.example.ui.theme.SandCream
import com.example.ui.theme.SandCreamLight
import com.example.ui.theme.SandCreamMuted

/**
 * Tab 3: Personal Bookmarks Library — Her saved textbooks, offline PDFs, and cloud-synced study notes.
 * Every text color uses SandCreamLight / SandCream / SandCreamMuted on EspressoBg (zero black text).
 */
@Composable
fun OfflineVaultScreen(
    personalSavedBooks: List<BookEntity>,
    offlineBooks: List<BookEntity>,
    completedBookIds: Set<String>,
    bookmarks: List<BookmarkEntity>,
    cloudNotes: List<CloudStudyNote>,
    isOnline: Boolean,
    onImportPdfClick: () -> Unit,
    onOpenBookDetail: (String) -> Unit,
    onOpenBookAtPage: (bookId: String, pageNumber: Int) -> Unit,
    onToggleBookmarkBook: (BookEntity) -> Unit,
    onToggleCompletedBook: (BookEntity) -> Unit,
    onRemoveOfflineBook: (BookEntity) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onDeleteCloudNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = Saved Books (Personal Library), 1 = Offline PDFs, 2 = Study Notes
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val totalOfflineBytes = remember(offlineBooks) { offlineBooks.sumOf { it.fileSizeBytes } }
    val totalNotesCount = maxOf(cloudNotes.size, bookmarks.size)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EspressoBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "vault_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOnline) "PERSONAL LIBRARY · CLOUD SYNCED" else "PERSONAL LIBRARY · OFFLINE",
                        style = MaterialTheme.typography.labelMedium,
                        color = SandCreamMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bookmarks",
                        style = MaterialTheme.typography.displayMedium,
                        color = SandCreamLight
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onImportPdfClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SandCream,
                        contentColor = EspressoInk
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("vault_import_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.UploadFile,
                        contentDescription = "Import local PDF",
                        tint = EspressoInk,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Import",
                        style = MaterialTheme.typography.labelLarge,
                        color = EspressoInk
                    )
                }
            }
        }

        // Personal Library Metrics Strip
        item(key = "vault_stats") {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    VaultLedgerMetric(
                        label = "SAVED BOOKS",
                        value = personalSavedBooks.size.toString().padStart(2, '0')
                    )
                    VaultLedgerMetric(
                        label = "OFFLINE PDFS",
                        value = offlineBooks.size.toString().padStart(2, '0')
                    )
                    VaultLedgerMetric(
                        label = "STORAGE",
                        value = formatFileSize(totalOfflineBytes).ifBlank { "0 MB" }
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
            }
        }

        // Three Underlined Editorial Tabs
        item(key = "vault_tabs") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                EditorialVaultTab(
                    label = "SAVED (${personalSavedBooks.size})",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("bookmarks_tab_saved")
                )
                EditorialVaultTab(
                    label = "OFFLINE (${offlineBooks.size})",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("offline_tab_books")
                )
                EditorialVaultTab(
                    label = "NOTES ($totalNotesCount)",
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.testTag("offline_tab_notes")
                )
            }
        }

        when (selectedTab) {
            0 -> {
                if (personalSavedBooks.isEmpty()) {
                    item(key = "empty_saved_books") {
                        EmptyVaultPanel(
                            title = "No saved textbooks yet",
                            subtitle = "Bookmark any Civil Engineering textbook from Library or Home, download an open-access PDF, or import your own PDF to build your personal shelf.",
                            actionLabel = "Import Local PDF",
                            onAction = onImportPdfClick
                        )
                    }
                } else {
                    items(personalSavedBooks, key = { "saved_${it.id}" }) { book ->
                        val docId = sanitizeFirestoreDocId(book.id)
                        val isCompleted = docId in completedBookIds || book.id in completedBookIds
                        PersonalSavedBookRow(
                            book = book,
                            isCompleted = isCompleted,
                            onOpen = {
                                if (book.isOfflineAvailable) onOpenBookAtPage(book.id, book.currentPage)
                                else onOpenBookDetail(book.id)
                            },
                            onToggleBookmark = { onToggleBookmarkBook(book) },
                            onToggleCompleted = { onToggleCompletedBook(book) }
                        )
                    }
                }
            }

            1 -> {
                if (offlineBooks.isEmpty()) {
                    item(key = "empty_offline_books") {
                        EmptyVaultPanel(
                            title = "No offline PDFs stored yet",
                            subtitle = "Download any full-length civil engineering textbook from Library or Home, or import a PDF directly from your device.",
                            actionLabel = "Import Local PDF",
                            onAction = onImportPdfClick
                        )
                    }
                } else {
                    items(offlineBooks, key = { "offline_${it.id}" }) { book ->
                        OfflineBookRow(
                            book = book,
                            onOpen = { onOpenBookAtPage(book.id, book.currentPage) },
                            onRemove = { onRemoveOfflineBook(book) }
                        )
                    }
                }
            }

            else -> {
                if (cloudNotes.isNotEmpty()) {
                    items(cloudNotes, key = { "cloud_note_${it.id}" }) { note ->
                        CloudNoteRow(
                            note = note,
                            onOpen = { onOpenBookAtPage(note.bookId, note.pageNumber.toInt()) },
                            onDelete = { onDeleteCloudNote(note.id) }
                        )
                    }
                } else if (bookmarks.isNotEmpty()) {
                    items(bookmarks, key = { "local_note_${it.id}" }) { bookmark ->
                        BookmarkNoteRow(
                            bookmark = bookmark,
                            onOpen = { onOpenBookAtPage(bookmark.bookId, bookmark.pageNumber) },
                            onDelete = { onDeleteBookmark(bookmark.id) }
                        )
                    }
                } else {
                    item(key = "empty_bookmarks") {
                        EmptyVaultPanel(
                            title = "No study notes yet",
                            subtitle = "While reading any book in the 3D PDF Reader, tap the bookmark icon to pin key equations or exam review notes to your Google account.",
                            actionLabel = null,
                            onAction = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultLedgerMetric(label: String, value: String) {
    Column {
        Text(
            text = value,
            fontFamily = NewsreaderFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = SandCreamLight
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SandCreamMuted
        )
    }
}

@Composable
private fun EditorialVaultTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) SandCreamLight else SandCreamMuted
        )
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .width(38.dp)
                .height(2.5.dp)
                .background(if (selected) SandCream else Color.Transparent)
        )
    }
}

@Composable
private fun PersonalSavedBookRow(
    book: BookEntity,
    isCompleted: Boolean,
    onOpen: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleCompleted: () -> Unit
) {
    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }
    val progress = (book.currentPage.toFloat() / book.totalPages.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("personal_saved_book_${book.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
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
                elevation = 2.dp,
                modifier = Modifier.width(54.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "YEAR ${subject.curriculumYear} · ${book.courseCode}" +
                        if (isCompleted) " · COMPLETED" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) SageOlive else SandCream,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = book.title,
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = SandCreamLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${book.author} · P.${book.currentPage}/${book.totalPages}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SandCreamMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(7.dp))
                LinearProgressIndicator(
                    progress = { if (isCompleted) 1f else progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = if (isCompleted) SageOlive else SandCream,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onToggleCompleted, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = if (isCompleted) "Mark in progress" else "Mark completed",
                        tint = if (isCompleted) SageOlive else SandCreamMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onToggleBookmark, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "Remove from personal bookmarks",
                        tint = SandCream,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun OfflineBookRow(
    book: BookEntity,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }
    val sourceLabel = remember(book.sourceType) {
        BookSourceType.entries.firstOrNull { it.name == book.sourceType }?.displayLabel
            ?: "Offline PDF"
    }
    val progress = (book.currentPage.toFloat() / book.totalPages.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("offline_book_card_${book.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
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
                isOfflineAvailable = true,
                showTextOverlay = false,
                elevation = 2.dp,
                modifier = Modifier.width(54.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${book.courseCode} · ${sourceLabel.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SandCream,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = book.title,
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = SandCreamLight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = buildString {
                        append("P.${book.currentPage}/${book.totalPages}")
                        if (book.fileSizeBytes > 0L) append(" · ${formatFileSize(book.fileSizeBytes)}")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = SandCreamMuted
                )
                Spacer(modifier = Modifier.height(7.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = SandCream,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                OutlinedButton(
                    onClick = onOpen,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Read", style = MaterialTheme.typography.labelLarge, color = SandCreamLight)
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Remove offline file",
                        tint = SandCreamMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun CloudNoteRow(
    note: CloudStudyNote,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("cloud_note_card_${note.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = null,
                tint = SandCream,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(16.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PAGE ${note.pageNumber} · ${note.chapterTitle.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SandCream,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = note.bookTitle,
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.5.sp,
                    color = SandCreamLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note.noteText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = note.noteText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SandCreamMuted
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete study note",
                    tint = SandCreamMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun BookmarkNoteRow(
    bookmark: BookmarkEntity,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("bookmark_card_${bookmark.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = null,
                tint = SandCream,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(16.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PAGE ${bookmark.pageNumber} · ${bookmark.chapterTitle.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = SandCream,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = bookmark.bookTitle,
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.5.sp,
                    color = SandCreamLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (bookmark.noteText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = bookmark.noteText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SandCreamMuted
                    )
                }
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete bookmark or note",
                    tint = SandCreamMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun EmptyVaultPanel(
    title: String,
    subtitle: String,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .background(EspressoCard, RoundedCornerShape(14.dp))
            .padding(24.dp)
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
            style = MaterialTheme.typography.bodySmall,
            color = SandCreamMuted
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SandCream,
                    contentColor = EspressoInk
                )
            ) {
                Text(actionLabel, color = EspressoInk, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
