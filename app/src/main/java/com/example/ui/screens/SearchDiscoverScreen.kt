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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.BookEntity
import com.example.data.model.BookSourceType
import com.example.data.model.CivilSubject
import com.example.data.model.DiscoverBookResult
import com.example.data.model.DownloadProgress
import com.example.data.model.SearchSourceFilter
import com.example.data.model.formatFileSize
import com.example.ui.components.HardcoverBookThumbnail
import com.example.ui.theme.NewsreaderFontFamily
import com.example.ui.viewmodel.SearchUiState

private val QuickCivilQueries = listOf(
    "Structural Analysis",
    "Soil Mechanics",
    "Hydraulics",
    "Reinforced Concrete",
    "Surveying",
    "Highway Engineering",
    "9780070661486"
)

@Composable
fun SearchDiscoverScreen(
    searchState: SearchUiState,
    downloadProgressMap: Map<String, DownloadProgress>,
    isOnline: Boolean,
    onQueryChanged: (String) -> Unit,
    onExecuteSearch: (String?) -> Unit,
    onSelectSourceFilter: (SearchSourceFilter) -> Unit,
    onOpenLocalBook: (String) -> Unit,
    onDownloadRemoteBook: (DiscoverBookResult) -> Unit,
    onCancelDownload: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val showLocal = searchState.sourceFilter == SearchSourceFilter.ALL ||
        searchState.sourceFilter == SearchSourceFilter.LOCAL
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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "search_header") {
            Column {
                Text(
                    text = "OPEN-ACCESS INDEX",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Catalog Search",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item(key = "search_input") {
            Column {
                OutlinedTextField(
                    value = searchState.query,
                    onValueChange = onQueryChanged,
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = if (searchState.isbnBadgeLabel != null) {
                                Icons.Filled.QrCode2
                            } else {
                                Icons.Filled.Search
                            },
                            contentDescription = "Search input"
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchState.query.isNotEmpty()) {
                                IconButton(
                                    onClick = { onQueryChanged("") },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                }
                            }
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    onExecuteSearch(null)
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(5.dp),
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .testTag("execute_search_button")
                            ) {
                                Text("Search", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onExecuteSearch(null)
                        }
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_isbn_keyword_field")
                )

                if (searchState.isbnBadgeLabel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${searchState.isbnBadgeLabel.uppercase()} · QUERYING LOCAL, ARCHIVE, OAPEN & OPEN LIBRARY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Clean underlined repository filter tabs
        item(key = "source_filters") {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(SearchSourceFilter.entries) { filter ->
                    val selected = searchState.sourceFilter == filter
                    Column(
                        modifier = Modifier
                            .clickable { onSelectSourceFilter(filter) }
                            .padding(vertical = 4.dp)
                            .testTag("source_filter_${filter.name.lowercase()}")
                    ) {
                        Text(
                            text = filter.label.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.onBackground
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(1.5.dp)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.secondary
                                    else Color.Transparent
                                )
                        )
                    }
                }
            }
        }

        if (!isOnline || searchState.statusMessage != null) {
            item(key = "status_banner") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (!isOnline) Icons.Filled.CloudOff else Icons.Filled.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = searchState.statusMessage
                            ?: "Offline — searching your local catalog and downloaded PDFs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (searchState.query.isBlank() && !searchState.hasSearched && !searchState.isSearching) {
            item(key = "quick_topics_editorial") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = "SUGGESTED SUBJECTS & ISBN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(QuickCivilQueries) { chipQuery ->
                            Surface(
                                onClick = {
                                    focusManager.clearFocus()
                                    onExecuteSearch(chipQuery)
                                },
                                shape = RoundedCornerShape(5.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.testTag("quick_search_chip_$chipQuery")
                            ) {
                                Text(
                                    text = chipQuery,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    SearchArchiveDirectoryNote()
                }
            }
        } else {
            if (showLocal && searchState.localResults.isNotEmpty()) {
                item(key = "local_section_title") {
                    Column {
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "IN YOUR LIBRARY (${searchState.localResults.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                items(searchState.localResults, key = { "local_${it.id}" }) { localBook ->
                    LocalSearchResultRow(
                        book = localBook,
                        onOpen = { onOpenLocalBook(localBook.id) }
                    )
                }
            }

            if (searchState.isSearching) {
                item(key = "loading_indicator") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "QUERYING OPEN-ACCESS ARCHIVES…",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (filteredRemote.isNotEmpty()) {
                item(key = "remote_section_title") {
                    Column {
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "OPEN-ACCESS REPOSITORIES (${filteredRemote.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                items(filteredRemote, key = { it.id }) { remoteBook ->
                    RemoteDiscoverBookRow(
                        book = remoteBook,
                        downloadProgress = downloadProgressMap[remoteBook.id],
                        onDownload = { onDownloadRemoteBook(remoteBook) },
                        onCancelDownload = { onCancelDownload(remoteBook.id) },
                        onOpenExisting = { onOpenLocalBook(remoteBook.id) }
                    )
                }
            }

            if (!searchState.isSearching &&
                (if (showLocal) searchState.localResults.isEmpty() else true) &&
                filteredRemote.isEmpty()
            ) {
                item(key = "no_matches") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "No matching volumes found",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try broader civil engineering terms (structural analysis, soil mechanics, hydraulics) or check the ISBN digits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalSearchResultRow(
    book: BookEntity,
    onOpen: () -> Unit
) {
    val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("local_search_result_${book.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
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
                modifier = Modifier.width(52.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildString {
                        append(book.courseCode)
                        if (book.totalPages > 0) append(" · ${book.totalPages}P")
                        append(if (book.isOfflineAvailable) " · OFFLINE" else " · CATALOG")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = book.title,
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.5.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedButton(
                onClick = onOpen,
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(if (book.isOfflineAvailable) "Read" else "View")
            }
        }
        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun RemoteDiscoverBookRow(
    book: DiscoverBookResult,
    downloadProgress: DownloadProgress?,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onOpenExisting: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("remote_search_result_${book.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            HardcoverBookThumbnail(
                title = book.title,
                author = book.author,
                courseCode = book.subject.courseCode,
                subject = book.subject,
                coverUrl = book.coverUrl,
                coverArtKey = book.subject.defaultCoverArtKey,
                totalPages = book.estimatedPages ?: 0,
                isOfflineAvailable = book.isOfflineDownloaded,
                showTextOverlay = false,
                elevation = 2.dp,
                modifier = Modifier.width(58.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildString {
                        append(book.sourceType.displayLabel.uppercase())
                        if (book.estimatedPages != null && book.estimatedPages > 0) {
                            append(" · ~${book.estimatedPages}P")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = book.title,
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val metaLine = buildString {
                    if (book.isbn.isNotBlank()) append("ISBN ${book.isbn}")
                    if (book.publishYear != null) {
                        if (isNotEmpty()) append(" · ")
                        append(book.publishYear)
                    }
                    if (book.fileSizeBytes > 0L) {
                        if (isNotEmpty()) append(" · ")
                        append(formatFileSize(book.fileSizeBytes))
                    }
                }
                if (metaLine.isNotBlank()) {
                    Text(
                        text = metaLine,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${book.subject.courseCode} · ${book.subject.shortName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (downloadProgress != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = downloadProgress.formattedSize,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            IconButton(
                                onClick = onCancelDownload,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Cancel,
                                    contentDescription = "Cancel PDF download",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else if (book.isOfflineDownloaded) {
                        OutlinedButton(
                            onClick = onOpenExisting,
                            shape = RoundedCornerShape(5.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Open Book")
                        }
                    } else {
                        Button(
                            onClick = onDownload,
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("download_remote_book_${book.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (book.sourceType == BookSourceType.GUTENBERG) "Save Text"
                                else "Get PDF"
                            )
                        }
                    }
                }
            }
        }

        if (downloadProgress != null) {
            val frac = downloadProgress.fraction
            if (frac != null) {
                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier.fillMaxWidth().height(2.5.dp)
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.5.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
        HorizontalDivider(thickness = 0.75.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun SearchArchiveDirectoryNote() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "CONNECTED ARCHIVES",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        ArchiveSourceLine("Internet Archive", "Full-length unrestricted 300–1,100+ page Civil Engineering PDFs.")
        ArchiveSourceLine("OAPEN & DOAB", "Peer-reviewed open-access university PDFs from Springer, CRC & Taylor & Francis.")
        ArchiveSourceLine("Open Library", "Global ISBN-10 / ISBN-13 catalog lookup.")
        ArchiveSourceLine("Project Gutenberg", "Public-domain mechanics and classic engineering texts.")
    }
}

@Composable
private fun ArchiveSourceLine(name: String, detail: String) {
    Column {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
