package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.firebase.CloudStudyNote
import com.example.data.firebase.UserProfile
import com.example.data.local.BookEntity
import com.example.data.model.CivilSubject
import com.example.ui.components.HardcoverBookThumbnail
import com.example.ui.theme.EspressoBg
import com.example.ui.theme.EspressoCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.EspressoInkMuted
import com.example.ui.theme.PlusJakartaFontFamily
import com.example.ui.theme.SageOlive
import com.example.ui.theme.SandCream
import com.example.ui.theme.SandCreamLight
import com.example.ui.theme.SandCreamMuted
import com.google.firebase.auth.FirebaseUser

/**
 * 1-to-1 Layout Twin of the Right Reference Screen ("Profile") connected to Google Account & Firestore:
 * - Top Espresso header ("Sync" · "Profile" · "Logout")
 * - Split horizontal seam between Espresso (#4D3C36) and Olive-Sage (#9C9A76)
 *   with overlapping circular Google profile photo centered across the boundary
 * - Student display name, editable study track/year mantra, and progress counts
 * - 2-tab underlined switcher ("My Shelf" | "Recommend")
 * - Edge-to-edge tight 3-column square book cover mosaic directly underneath
 */
@Composable
fun ProfileVaultScreen(
    currentUser: FirebaseUser,
    userProfile: UserProfile?,
    allBooks: List<BookEntity>,
    personalSavedBooks: List<BookEntity>,
    completedCount: Int,
    cloudNotes: List<CloudStudyNote>,
    onImportPdfClick: () -> Unit,
    onRefreshCatalog: () -> Unit,
    onUpdateProfilePreferences: (studyMantra: String, curriculumYear: Int) -> Unit,
    onSignOutClick: () -> Unit,
    onBookClick: (String) -> Unit,
    onOpenBookAtPage: (bookId: String, pageNumber: Int) -> Unit,
    onDeleteCloudNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = My Shelf (Personal Saved + Notes), 1 = Recommend (Full Library Wall Mosaic)
    var selectedTab by rememberSaveable { mutableIntStateOf(1) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val displayName = userProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser.email?.substringBefore("@")
        ?: "Civil Engineer"
    val photoUrl = userProfile?.photoUrl?.takeIf { it.isNotBlank() }
        ?: currentUser.photoUrl?.toString()
    val curriculumYear = (userProfile?.curriculumYear ?: 1L).toInt().coerceIn(1, 5)
    val studyMantra = userProfile?.studyMantra?.takeIf { it.isNotBlank() }
        ?: "Year $curriculumYear · BS Civil Engineering"

    val mosaicRows = remember(allBooks) { allBooks.chunked(3) }
    val savedRows = remember(personalSavedBooks) { personalSavedBooks.chunked(3) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EspressoBg),
        contentPadding = PaddingValues(bottom = 108.dp)
    ) {
        // 1. Split Espresso + Olive-Sage Header with Overlapping Circular Google Avatar
        item(key = "profile_split_header") {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Upper Dark Espresso Band
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EspressoBg)
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sync",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 14.sp,
                                color = SandCreamLight.copy(alpha = 0.88f),
                                modifier = Modifier
                                    .clickable(onClick = onRefreshCatalog)
                                    .padding(vertical = 4.dp)
                                    .testTag("profile_sync_action")
                            )
                            Text(
                                text = "Profile",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 23.sp,
                                color = SandCreamLight
                            )
                            Text(
                                text = "Logout",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 14.sp,
                                color = SandCreamLight.copy(alpha = 0.88f),
                                modifier = Modifier
                                    .clickable(onClick = onSignOutClick)
                                    .padding(vertical = 4.dp)
                                    .testTag("profile_logout_action")
                            )
                        }
                        Spacer(modifier = Modifier.height(74.dp))
                    }

                    // Lower Olive-Sage Band (#9C9A76)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SageOlive),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(66.dp))

                        Text(
                            text = displayName,
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 23.sp,
                            color = EspressoInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clickable { showEditProfileDialog = true }
                                .padding(horizontal = 16.dp, vertical = 2.dp)
                                .testTag("edit_profile_mantra_button")
                        ) {
                            Text(
                                text = "Year $curriculumYear · $studyMantra",
                                fontFamily = PlusJakartaFontFamily,
                                fontSize = 13.sp,
                                color = EspressoInkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit study track",
                                tint = EspressoInkMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${personalSavedBooks.size} saved · $completedCount completed · ${cloudNotes.size} notes",
                            fontFamily = PlusJakartaFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = EspressoInkMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Two-Column Underlined Tab Bar (1-to-1 with mockup)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            ProfileUnderlineTab(
                                label = "My Shelf (${personalSavedBooks.size})",
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                modifier = Modifier.weight(1f).testTag("profile_tab_shelf")
                            )
                            ProfileUnderlineTab(
                                label = "Recommend",
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                modifier = Modifier.weight(1f).testTag("profile_tab_recommend")
                            )
                        }
                    }
                }

                // Overlapping Circular Google Profile Portrait straddling the Espresso / Sage seam
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 66.dp)
                        .size(120.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .border(3.dp, SageOlive.copy(alpha = 0.92f), CircleShape)
                        .background(EspressoCard)
                ) {
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Google account profile picture",
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.img_library_hero_1795058942930),
                            error = painterResource(id = R.drawable.img_library_hero_1795058942930),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_library_hero_1795058942930),
                            contentDescription = "Civil engineering student portrait",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // 2. Edge-to-Edge Tight 3-Column Book Cover Wall OR Personal Shelf + Notes
        if (selectedTab == 1) {
            items(mosaicRows, key = { row -> "wall_${row.joinToString("_") { it.id }}" }) { rowBooks ->
                TightBookMosaicRow(
                    rowBooks = rowBooks,
                    onBookTap = { book ->
                        if (book.isOfflineAvailable) onOpenBookAtPage(book.id, book.currentPage)
                        else onBookClick(book.id)
                    }
                )
            }
        } else {
            if (personalSavedBooks.isEmpty()) {
                item(key = "profile_empty_saved") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your personal shelf is empty",
                            style = MaterialTheme.typography.titleMedium,
                            color = SandCreamLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bookmark textbooks from Library or import your own PDF to sync them to your Google account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SandCreamMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            onClick = onImportPdfClick,
                            shape = RoundedCornerShape(24.dp),
                            color = SandCream
                        ) {
                            Text(
                                text = "Import Local PDF",
                                fontFamily = PlusJakartaFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = EspressoInk,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            } else {
                items(savedRows, key = { row -> "saved_${row.joinToString("_") { it.id }}" }) { rowBooks ->
                    TightBookMosaicRow(
                        rowBooks = rowBooks,
                        onBookTap = { book ->
                            if (book.isOfflineAvailable) onOpenBookAtPage(book.id, book.currentPage)
                            else onBookClick(book.id)
                        }
                    )
                }
            }

            if (cloudNotes.isNotEmpty()) {
                item(key = "profile_notes_header") {
                    Text(
                        text = "Synced Study Notes (${cloudNotes.size})",
                        fontFamily = PlusJakartaFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = SandCreamLight,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp)
                    )
                }
                items(cloudNotes, key = { it.id }) { note ->
                    Surface(
                        color = EspressoCard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .clickable { onOpenBookAtPage(note.bookId, note.pageNumber.toInt()) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = null,
                                tint = SandCream,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Page ${note.pageNumber} · ${note.bookTitle}",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = SandCreamLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (note.noteText.isNotBlank()) {
                                    Text(
                                        text = note.noteText,
                                        fontFamily = PlusJakartaFontFamily,
                                        fontSize = 12.sp,
                                        color = SandCreamMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onDeleteCloudNote(note.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete study note",
                                    tint = SandCreamMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditProfileDialog) {
        var mantraInput by remember(studyMantra) { mutableStateOf(studyMantra) }
        var yearSelection by remember(curriculumYear) { mutableIntStateOf(curriculumYear) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = EspressoCard,
            titleContentColor = SandCreamLight,
            textContentColor = SandCreamLight,
            title = {
                Text(
                    text = "Study Profile",
                    fontFamily = PlusJakartaFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = SandCreamLight
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Select your current Civil Engineering year:",
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = 13.sp,
                        color = SandCreamMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..4).forEach { yr ->
                            val active = yearSelection == yr
                            Surface(
                                onClick = { yearSelection = yr },
                                shape = RoundedCornerShape(10.dp),
                                color = if (active) SandCream else EspressoBg,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Y$yr",
                                    fontFamily = PlusJakartaFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (active) EspressoInk else SandCreamLight,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = mantraInput,
                        onValueChange = { if (it.length <= 120) mantraInput = it },
                        label = { Text("Study Goal / Track", color = SandCreamMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SandCreamLight,
                            unfocusedTextColor = SandCreamLight,
                            focusedBorderColor = SandCream,
                            unfocusedBorderColor = SageOlive,
                            cursorColor = SandCream
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfilePreferences(mantraInput, yearSelection)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SandCream,
                        contentColor = EspressoInk
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.SemiBold, color = EspressoInk)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = SandCreamLight)
                }
            }
        )
    }
}

@Composable
private fun ProfileUnderlineTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(top = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = PlusJakartaFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.5.sp,
            color = if (selected) EspressoInk else EspressoInk.copy(alpha = 0.72f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(if (selected) EspressoInk else Color.Transparent)
        )
    }
}

@Composable
private fun TightBookMosaicRow(
    rowBooks: List<BookEntity>,
    onBookTap: (BookEntity) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EspressoBg)
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (slot in 0 until 3) {
            val book = rowBooks.getOrNull(slot)
            if (book != null) {
                val subject = remember(book.subjectKey) { CivilSubject.fromKey(book.subjectKey) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.92f)
                        .clickable { onBookTap(book) }
                        .testTag("profile_mosaic_${book.id}")
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
                        elevation = 0.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
