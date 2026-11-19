package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ReaderPreferences
import com.example.data.model.PaperTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.DmSansFontFamily
import com.example.ui.theme.EspressoBg
import com.example.ui.theme.EspressoCard
import com.example.ui.theme.EspressoInk
import com.example.ui.theme.EspressoInkMuted
import com.example.ui.theme.LoraFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.SageOlive
import com.example.ui.theme.SandCream
import com.example.ui.theme.SandCreamLight
import com.example.ui.theme.SandCreamMuted
import com.google.firebase.auth.FirebaseUser

import androidx.compose.foundation.lazy.LazyRow
import com.example.data.firebase.scoreBookForProfile

/**
 * Instagram-Style Editorial Study Profile ("the IG system"):
 * - Espresso upper band ("Sync" · "Profile" · "Survey") — always-on offline study session (no logout)
 * - Mathematically centered circular Student Avatar straddling the Espresso / Olive-Sage seam
 * - Playfair Display student name, editable Year 1–4 study mantra & multi-select survey button
 * - IG-style 3-column metrics strip (Saved | Completed | Notes)
 * - STICKY / FIXED 2-tab bar ("My Shelf" | "Recommend") pinned while scrolling
 * - Chosen Subject Focus filter chips + Crisp edge-to-edge 3-column IG book cover mosaic grid
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileVaultScreen(
    currentUser: FirebaseUser?,
    userProfile: UserProfile?,
    allBooks: List<BookEntity>,
    personalSavedBooks: List<BookEntity>,
    completedCount: Int,
    cloudNotes: List<CloudStudyNote>,
    readerPreferences: ReaderPreferences = ReaderPreferences(),
    onImportPdfClick: () -> Unit,
    onRefreshCatalog: () -> Unit,
    onUpdateProfilePreferences: (studyMantra: String, curriculumYear: Int) -> Unit,
    onSaveProfileSurvey: (ProfileSurveyResult) -> Unit = { res ->
        onUpdateProfilePreferences(res.studyMantra, res.curriculumYear)
    },
    onSelectPaperTheme: (PaperTheme) -> Unit = {},
    onSelectFontSize: (Float) -> Unit = {},
    onToggleCropPdfMargins: (Boolean) -> Unit = {},
    onToggleAutoSyncOnLaunch: (Boolean) -> Unit = {},
    onToggleResetToPortraitTrimmed: (Boolean) -> Unit = {},
    onSaveCustomGeminiApiKey: (String) -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onSwitchGoogleAccount: () -> Unit = {},
    onBookClick: (String) -> Unit,
    onOpenBookAtPage: (bookId: String, pageNumber: Int) -> Unit,
    onDeleteCloudNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = My Shelf (Personal Saved + Notes), 1 = Recommend (IG 3-Column Book Grid)
    var selectedTab by rememberSaveable { mutableIntStateOf(1) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showFullSurveyModal by rememberSaveable { mutableStateOf(false) }
    var showSettingsModal by rememberSaveable { mutableStateOf(false) }
    var activeRecommendSubjectFilter by rememberSaveable { mutableStateOf<String?>(null) }

    val displayName = userProfile?.displayName?.takeIf { it.isNotBlank() }
        ?: currentUser?.displayName?.takeIf { it.isNotBlank() }
        ?: "Civil Engineer"
    val photoUrl = userProfile?.photoUrl?.takeIf { it.isNotBlank() }
        ?: currentUser?.photoUrl?.toString()
    val curriculumYear = (userProfile?.curriculumYear ?: 1L).toInt().coerceIn(1, 5)
    val yearLabel = if (curriculumYear == 5) "Masters / Grad" else "Year $curriculumYear"

    val chosenSubjectKeys = remember(userProfile, curriculumYear) {
        userProfile?.effectiveSubjectKeys?.takeIf { it.isNotEmpty() }
            ?: CivilSubject.entries.filter { it.curriculumYear == curriculumYear }.map { it.name }
                .ifEmpty { listOf(CivilSubject.MECHANICS.name) }
    }
    val chosenSubjects = remember(chosenSubjectKeys) {
        chosenSubjectKeys.map { CivilSubject.fromKey(it) }.distinct()
    }
    val primarySubject = chosenSubjects.firstOrNull() ?: CivilSubject.MECHANICS

    val chosenGoals = remember(userProfile) {
        userProfile?.effectiveStudyGoals?.takeIf { it.isNotEmpty() }
            ?: listOf("Current Semester Courses")
    }
    val studyGoal = chosenGoals.first()
    val studyMantra = userProfile?.studyMantra?.takeIf { it.isNotBlank() }
        ?: "${chosenSubjects.take(2).joinToString(", ") { it.shortName }} · $studyGoal"

    if (showFullSurveyModal) {
        AuthScreen(
            onAuthSuccess = { showFullSurveyModal = false },
            onSurveyCompleted = { result ->
                onSaveProfileSurvey(result)
                activeRecommendSubjectFilter = null
                showFullSurveyModal = false
                selectedTab = 1
            },
            initialShowSurvey = true,
            initialName = displayName,
            initialYear = curriculumYear,
            initialSubjectKey = primarySubject.name,
            initialSubjectKeys = chosenSubjectKeys,
            initialGoal = studyGoal,
            initialGoals = chosenGoals,
            onDismissSurvey = { showFullSurveyModal = false },
            modifier = modifier
        )
        return
    }

    // Rank recommended books based on ALL of the student's chosen subjects, study goals, and curriculum year
    val recommendedBooks = remember(allBooks, userProfile, curriculumYear, chosenSubjectKeys, chosenGoals, activeRecommendSubjectFilter) {
        val baseSorted = allBooks.sortedWith(
            compareByDescending<BookEntity> { book ->
                scoreBookForProfile(book, userProfile)
            }.thenBy { book ->
                val idx = chosenSubjectKeys.indexOfFirst { it.equals(book.subjectKey, ignoreCase = true) }
                if (idx >= 0) idx else 99
            }.thenBy { book ->
                val subjYear = CivilSubject.fromKey(book.subjectKey).curriculumYear
                if (subjYear == curriculumYear) 0 else 1
            }.thenBy { book ->
                CivilSubject.fromKey(book.subjectKey).sequenceOrder
            }.thenBy { it.courseCode }
        )
        if (activeRecommendSubjectFilter.isNullOrBlank()) {
            baseSorted
        } else {
            baseSorted.filter { it.subjectKey.equals(activeRecommendSubjectFilter, ignoreCase = true) }
        }
    }

    val mosaicRows = remember(recommendedBooks) { recommendedBooks.chunked(3) }
    val savedRows = remember(personalSavedBooks) { personalSavedBooks.chunked(3) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EspressoBg),
        contentPadding = PaddingValues(bottom = 114.dp)
    ) {
        // 1. Split Espresso + Olive-Sage Header with Mathematically Centered Circular Student Avatar
        item(key = "profile_split_header") {
            val upperBandHeight = 116.dp
            val avatarSize = 112.dp
            val avatarTopOffset = upperBandHeight - (avatarSize / 2) // 60.dp: dead-center on seam

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Upper Dark Espresso Band (fixed deterministic height below status bar)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EspressoBg)
                            .statusBarsPadding()
                            .height(upperBandHeight)
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
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = SandCreamLight.copy(alpha = 0.88f),
                                modifier = Modifier
                                    .clickable(onClick = onRefreshCatalog)
                                    .padding(vertical = 6.dp, horizontal = 4.dp)
                                    .testTag("profile_sync_action")
                            )
                            Text(
                                text = "Profile",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                color = SandCreamLight
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Survey",
                                    fontFamily = DmSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    color = SandCream,
                                    modifier = Modifier
                                        .clickable { showFullSurveyModal = true }
                                        .padding(vertical = 6.dp, horizontal = 4.dp)
                                        .testTag("profile_survey_top_action")
                                )
                                Surface(
                                    onClick = { showSettingsModal = true },
                                    shape = RoundedCornerShape(999.dp),
                                    color = EspressoCard,
                                    modifier = Modifier.testTag("profile_settings_top_action")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = "App & Reader Settings",
                                            tint = SandCream,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Settings",
                                            fontFamily = DmSansFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = SandCreamLight
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Lower Olive-Sage Band (#9C9A76)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SageOlive)
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Exact clearance for lower half of avatar (56dp) + 12dp breathing room
                        Spacer(modifier = Modifier.height(68.dp))

                        Text(
                            text = displayName,
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 25.sp,
                            color = EspressoInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Editable Study Track / Year Mantra
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { showFullSurveyModal = true }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .testTag("edit_profile_mantra_button")
                        ) {
                            Text(
                                text = "$yearLabel · $studyMantra",
                                fontFamily = LoraFontFamily,
                                fontSize = 13.5.sp,
                                color = EspressoInkMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit study survey",
                                tint = EspressoInkMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Study Survey Customization Pill + Settings Pill + Chosen Goals Summary Pill
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { showFullSurveyModal = true },
                                shape = RoundedCornerShape(999.dp),
                                color = SandCream,
                                modifier = Modifier.testTag("profile_open_survey_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit Study Survey",
                                        tint = EspressoInk,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    val focusLabel = if (chosenSubjects.size == 1) {
                                        chosenSubjects.first().shortName
                                    } else {
                                        "${chosenSubjects.size} Focus Subjects"
                                    }
                                    Text(
                                        text = "Survey · $focusLabel",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = EspressoInk
                                    )
                                }
                            }

                            Surface(
                                onClick = { showSettingsModal = true },
                                shape = RoundedCornerShape(999.dp),
                                color = EspressoInk,
                                modifier = Modifier.testTag("profile_open_settings_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = "Settings",
                                        tint = SandCream,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Settings",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = SandCream
                                    )
                                }
                            }

                            Surface(
                                onClick = { showFullSurveyModal = true },
                                shape = RoundedCornerShape(999.dp),
                                color = EspressoInk.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${chosenGoals.size} ${if (chosenGoals.size == 1) "Goal" else "Goals"}",
                                    fontFamily = DmSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = EspressoInk,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Instagram-style 3-Column Metrics Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IgMetricColumn(
                                count = personalSavedBooks.size.toString(),
                                label = "Saved",
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(EspressoInk.copy(alpha = 0.18f))
                            )
                            IgMetricColumn(
                                count = completedCount.toString(),
                                label = "Completed",
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(24.dp)
                                    .background(EspressoInk.copy(alpha = 0.18f))
                            )
                            IgMetricColumn(
                                count = cloudNotes.size.toString(),
                                label = "Notes",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Overlapping Circular Student Profile Portrait mathematically centered on the Espresso / Sage seam
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = avatarTopOffset)
                        .size(avatarSize)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .border(3.5.dp, SageOlive, CircleShape)
                        .background(EspressoCard)
                ) {
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Student profile picture",
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

        // 2. FIXED / STICKY Instagram-Style Tab Bar ("My Shelf" | "Recommend")
        stickyHeader(key = "profile_sticky_tabs") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SageOlive)
            ) {
                HorizontalDivider(
                    thickness = 0.75.dp,
                    color = EspressoInk.copy(alpha = 0.14f)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    ProfileStickyIgTab(
                        label = "My Shelf (${personalSavedBooks.size})",
                        icon = if (selectedTab == 0) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_tab_shelf")
                    )
                    ProfileStickyIgTab(
                        label = "Recommend",
                        icon = if (selectedTab == 1) Icons.Filled.GridOn else Icons.Outlined.GridOn,
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_tab_recommend")
                    )
                }
            }
        }

        // 3. Edge-to-Edge Tight 3-Column Instagram Book Grid
        if (selectedTab == 1) {
            item(key = "recommend_chosen_filters") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EspressoBg)
                        .padding(vertical = 10.dp)
                ) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item(key = "filter_all_chosen") {
                            val isAll = activeRecommendSubjectFilter == null
                            Surface(
                                onClick = { activeRecommendSubjectFilter = null },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isAll) SandCream else EspressoCard
                            ) {
                                Text(
                                    text = "Recommended For You · $yearLabel",
                                    fontFamily = DmSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = if (isAll) EspressoInk else SandCreamLight,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        items(chosenSubjects, key = { "rec_chip_${it.name}" }) { subj ->
                            val isSelected = activeRecommendSubjectFilter.equals(subj.name, ignoreCase = true)
                            Surface(
                                onClick = {
                                    activeRecommendSubjectFilter = if (isSelected) null else subj.name
                                },
                                shape = RoundedCornerShape(999.dp),
                                color = if (isSelected) SandCream else SageOlive.copy(alpha = 0.28f)
                            ) {
                                Text(
                                    text = "${subj.shortName} (${subj.courseCode})",
                                    fontFamily = DmSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) EspressoInk else SandCreamLight,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

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
                            .padding(horizontal = 28.dp, vertical = 38.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = SandCreamMuted,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Your personal shelf is empty",
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            color = SandCreamLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bookmark textbooks from Library or import your own PDF to build your personal IG-style shelf saved in your study cache.",
                            fontFamily = DmSansFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = SandCreamMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Surface(
                            onClick = onImportPdfClick,
                            shape = RoundedCornerShape(24.dp),
                            color = SandCream
                        ) {
                            Text(
                                text = "Import Local PDF",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp,
                                color = EspressoInk,
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp)
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
                        fontFamily = PlayfairDisplayFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 19.sp,
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
                                    fontFamily = LoraFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.5.sp,
                                    color = SandCreamLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (note.noteText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = note.noteText,
                                        fontFamily = DmSansFontFamily,
                                        fontSize = 12.sp,
                                        color = SandCreamMuted,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(
                                onClick = { onDeleteCloudNote(note.id) },
                                modifier = Modifier.size(40.dp)
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

    if (showSettingsModal) {
        ProfileSettingsDialog(
            preferences = readerPreferences,
            totalBooksCount = allBooks.size,
            savedBooksCount = personalSavedBooks.size,
            notesCount = cloudNotes.size,
            onDismiss = { showSettingsModal = false },
            onSelectPaperTheme = onSelectPaperTheme,
            onSelectFontSize = onSelectFontSize,
            onToggleCropPdfMargins = onToggleCropPdfMargins,
            onToggleAutoSyncOnLaunch = onToggleAutoSyncOnLaunch,
            onToggleResetToPortraitTrimmed = onToggleResetToPortraitTrimmed,
            onSaveCustomGeminiApiKey = onSaveCustomGeminiApiKey,
            onSyncCatalogNow = {
                onRefreshCatalog()
                showSettingsModal = false
            },
            onOpenSurvey = {
                showSettingsModal = false
                showFullSurveyModal = true
            }
        )
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
                    fontFamily = PlayfairDisplayFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = SandCreamLight
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Select your current Engineering year or Masters level:",
                        fontFamily = DmSansFontFamily,
                        fontSize = 13.sp,
                        color = SandCreamMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..5).forEach { yr ->
                            val active = yearSelection == yr
                            Surface(
                                onClick = { yearSelection = yr },
                                shape = RoundedCornerShape(10.dp),
                                color = if (active) SandCream else EspressoBg,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (yr == 5) "MS" else "Y$yr",
                                    fontFamily = DmSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (active) EspressoInk else SandCreamLight,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 9.dp)
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
private fun ProfileSettingsDialog(
    preferences: ReaderPreferences,
    totalBooksCount: Int,
    savedBooksCount: Int,
    notesCount: Int,
    onDismiss: () -> Unit,
    onSelectPaperTheme: (PaperTheme) -> Unit,
    onSelectFontSize: (Float) -> Unit,
    onToggleCropPdfMargins: (Boolean) -> Unit,
    onToggleAutoSyncOnLaunch: (Boolean) -> Unit,
    onToggleResetToPortraitTrimmed: (Boolean) -> Unit,
    onSaveCustomGeminiApiKey: (String) -> Unit,
    onSyncCatalogNow: () -> Unit,
    onOpenSurvey: () -> Unit
) {
    var apiKeyDraft by remember(preferences.customGeminiApiKey) {
        mutableStateOf(preferences.customGeminiApiKey)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = EspressoCard,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "App & Reader Settings",
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = SandCreamLight
                        )
                        Text(
                            text = "Appearance, reading defaults, auto-sync & AI Study Assistant",
                            fontFamily = DmSansFontFamily,
                            fontSize = 12.sp,
                            color = SandCreamMuted
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Done",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = SandCream
                        )
                    }
                }

                HorizontalDivider(color = SandCream.copy(alpha = 0.14f))

                // 1. APPEARANCE & PAPER THEME
                Text(
                    text = "APPEARANCE & DEFAULT PAPER THEME",
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.9.sp,
                    color = SandCream
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themes = listOf(
                        PaperTheme.PARCHMENT to "Book Parchment",
                        PaperTheme.CRISP to "Crisp White",
                        PaperTheme.BLUEPRINT_DARK to "Blueprint Night"
                    )
                    themes.forEach { (theme, label) ->
                        val selected = preferences.paperTheme == theme
                        Surface(
                            onClick = { onSelectPaperTheme(theme) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) SandCream else EspressoBg,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (selected) SandCream else SageOlive.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (selected) EspressoInk else SandCreamLight,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 11.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }

                // Font Size Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Default Reading Text Size",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            color = SandCreamLight
                        )
                        Text(
                            text = "${preferences.fontSizeSp.toInt()} sp",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = SandCream
                        )
                    }
                    Slider(
                        value = preferences.fontSizeSp,
                        onValueChange = { onSelectFontSize(it) },
                        valueRange = 14f..24f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = SandCream,
                            activeTrackColor = SandCream,
                            inactiveTrackColor = EspressoBg
                        )
                    )
                }

                HorizontalDivider(color = SandCream.copy(alpha = 0.14f))

                // 2. READER DEFAULTS (PORTRAIT TRIMMED ON OPEN)
                Text(
                    text = "BOOK READER DEFAULTS",
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.9.sp,
                    color = SandCream
                )

                SettingsSwitchRow(
                    title = "Reset to Portrait Trimmed on Book Open",
                    subtitle = "Every time you open a book, start in 1-Page Portrait view with PDF margins trimmed",
                    checked = preferences.resetToPortraitTrimmedOnOpen,
                    onCheckedChange = onToggleResetToPortraitTrimmed
                )

                SettingsSwitchRow(
                    title = "Trim PDF White Margins by Default",
                    subtitle = "Zoom slightly to crop blank outer PDF borders for crisp engineering diagrams",
                    checked = preferences.cropPdfMargins,
                    onCheckedChange = onToggleCropPdfMargins
                )

                HorizontalDivider(color = SandCream.copy(alpha = 0.14f))

                // 3. CATALOG AUTO-SYNC
                Text(
                    text = "LIBRARY SYNC & ENGINEERING CATALOG",
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.9.sp,
                    color = SandCream
                )

                SettingsSwitchRow(
                    title = "Auto-Sync Catalog Every Time App Opens",
                    subtitle = "Automatically check for updated Civil, Allied, Masters, Math & Physics textbooks on launch",
                    checked = preferences.autoSyncOnLaunch,
                    onCheckedChange = onToggleAutoSyncOnLaunch
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        onClick = onSyncCatalogNow,
                        shape = RoundedCornerShape(12.dp),
                        color = SandCream,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Sync Catalog Now",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = EspressoInk,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        )
                    }
                    Surface(
                        onClick = onOpenSurvey,
                        shape = RoundedCornerShape(12.dp),
                        color = EspressoBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SageOlive.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Retake Study Survey",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = SandCreamLight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        )
                    }
                }

                HorizontalDivider(color = SandCream.copy(alpha = 0.14f))

                // 4. AI STUDY ASSISTANT SETTINGS
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = SandCream,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "AI STUDY ASSISTANT (FREE LLM + OPTIONAL GEMINI KEY)",
                        fontFamily = DmSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = SandCream
                    )
                }
                Text(
                    text = "Built-in Free Engineering AI works immediately with zero key required. Optionally paste a free-tier Google Gemini API key below for Gemini 2.0 Flash responses.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 11.8.sp,
                    color = SandCreamMuted,
                    lineHeight = 16.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = apiKeyDraft,
                        onValueChange = { apiKeyDraft = it },
                        placeholder = {
                            Text(
                                text = "Optional: AIzaSy... (Leave blank for built-in Free AI)",
                                fontSize = 11.5.sp,
                                color = SandCreamMuted.copy(alpha = 0.7f)
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SandCreamLight,
                            unfocusedTextColor = SandCreamLight,
                            focusedBorderColor = SandCream,
                            unfocusedBorderColor = SageOlive.copy(alpha = 0.5f),
                            cursorColor = SandCream
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        onClick = { onSaveCustomGeminiApiKey(apiKeyDraft) },
                        shape = RoundedCornerShape(12.dp),
                        color = SageOlive
                    ) {
                        Text(
                            text = "Save Key",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = EspressoInk,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp)
                        )
                    }
                }

                // 5. STUDY CACHE STATUS
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EspressoBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Always-On Study Cache Active · $totalBooksCount catalog textbooks · $savedBooksCount saved on shelf · $notesCount study notes cached offline",
                        fontFamily = DmSansFontFamily,
                        fontSize = 11.5.sp,
                        color = SandCreamMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EspressoBg)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = SandCreamLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = DmSansFontFamily,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                color = SandCreamMuted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = EspressoInk,
                checkedTrackColor = SandCream,
                uncheckedThumbColor = SandCreamMuted,
                uncheckedTrackColor = EspressoCard
            )
        )
    }
}

@Composable
private fun IgMetricColumn(
    count: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = count,
            fontFamily = PlayfairDisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = EspressoInk
        )
        Text(
            text = label,
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = EspressoInkMuted
        )
    }
}

@Composable
private fun ProfileStickyIgTab(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(top = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) EspressoInk else EspressoInk.copy(alpha = 0.62f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontFamily = DmSansFontFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (selected) EspressoInk else EspressoInk.copy(alpha = 0.68f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
                        .aspectRatio(0.76f)
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
                        aspectRatio = null,
                        cornerRadius = 0.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
