package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.firebase.attemptAutoSignIn
import com.example.data.firebase.onGoogleSignInClicked
import com.example.data.firebase.safeFirebaseAuthOrNull
import com.example.data.model.CivilSubject
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

data class ProfileSurveyResult(
    val displayName: String,
    val email: String?,
    val curriculumYear: Int,
    val primarySubjectKey: String,
    val studyGoal: String,
    val studyMantra: String
)

private data class YearSurveyOption(
    val year: Int,
    val badge: String,
    val title: String,
    val subtitle: String,
    val defaultSubject: CivilSubject
)

private val YearSurveyOptions = listOf(
    YearSurveyOption(
        year = 1,
        badge = "YEAR 1",
        title = "1st Year · Engineering Foundations",
        subtitle = "Statics, Dynamics, Engineering Calculus & Physics (CE 101–104)",
        defaultSubject = CivilSubject.MECHANICS
    ),
    YearSurveyOption(
        year = 2,
        badge = "YEAR 2",
        title = "2nd Year · Surveying & Materials",
        subtitle = "Geomatics, Strength of Materials & Concrete Tech (CE 201–208)",
        defaultSubject = CivilSubject.SURVEYING
    ),
    YearSurveyOption(
        year = 3,
        badge = "YEAR 3",
        title = "3rd Year · Core Civil Disciplines",
        subtitle = "Structural Analysis, Hydraulics, Soil Mechanics & Highway (CE 301–308)",
        defaultSubject = CivilSubject.STRUCTURAL
    ),
    YearSurveyOption(
        year = 4,
        badge = "YEAR 4",
        title = "4th Year · Design, Capstone & Board Exam",
        subtitle = "Reinforced Concrete/Steel Design, Environmental & Construction Mgmt (CE 401–408)",
        defaultSubject = CivilSubject.CONSTRUCTION
    )
)

private data class StudyGoalOption(
    val title: String,
    val subtitle: String,
    val mantraPrefix: String
)

private val StudyGoalOptions = listOf(
    StudyGoalOption(
        title = "Current Semester Courses",
        subtitle = "Follow my year's chronological textbooks & problem sets",
        mantraPrefix = "BS Civil Engineering · Chronological Track"
    ),
    StudyGoalOption(
        title = "CE Licensure & Board Exam Review",
        subtitle = "Focus on high-yield board exam formulas, MSTE, HGE & PSAD",
        mantraPrefix = "Future Registered Civil Engineer · Board Review"
    ),
    StudyGoalOption(
        title = "Structural & Foundation Design",
        subtitle = "Deep-dive into RC, Steel, Bridge & Geotechnical references",
        mantraPrefix = "BSCE · Structural & Geotechnical Track"
    ),
    StudyGoalOption(
        title = "Capstone Project & Field Reference",
        subtitle = "Codes, estimating, hydraulics & construction management",
        mantraPrefix = "BSCE · Capstone & Field Engineering"
    )
)

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onSurveyCompleted: (ProfileSurveyResult) -> Unit = { onAuthSuccess() },
    initialShowSurvey: Boolean = false,
    initialName: String = "",
    initialEmail: String = "",
    initialYear: Int = 1,
    initialSubjectKey: String = CivilSubject.MECHANICS.name,
    initialGoal: String = StudyGoalOptions.first().title,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isAutoChecking by remember { mutableStateOf(!initialShowSurvey) }
    var isSigningIn by remember { mutableStateOf(false) }
    var showSurveyWizard by rememberSaveable { mutableStateOf(initialShowSurvey) }
    var surveyStep by rememberSaveable { mutableIntStateOf(0) } // 0..3 (4 questions)

    var studentName by rememberSaveable { mutableStateOf(initialName) }
    var studentEmail by rememberSaveable { mutableStateOf(initialEmail) }
    var selectedYear by rememberSaveable { mutableIntStateOf(initialYear.coerceIn(1, 4)) }
    var selectedSubjectKey by rememberSaveable { mutableStateOf(initialSubjectKey) }
    var selectedGoal by rememberSaveable { mutableStateOf(initialGoal) }

    LaunchedEffect(Unit) {
        if (!initialShowSurvey) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                    isAutoChecking = false
                    val fbUser = safeFirebaseAuthOrNull()?.currentUser
                    if (fbUser != null) {
                        studentName = fbUser.displayName.orEmpty()
                        studentEmail = fbUser.email.orEmpty()
                    }
                    showSurveyWizard = true
                },
                onUnauthenticated = {
                    isAutoChecking = false
                },
                scope = scope
            )
        }
    }

    Surface(
        color = EspressoBg,
        contentColor = SandCreamLight,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!showSurveyWizard) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Circular library emblem matching Profile portrait framing
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .shadow(10.dp, CircleShape)
                            .clip(CircleShape)
                            .border(3.dp, SageOlive, CircleShape)
                            .background(EspressoCard)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_library_hero_1795058942930),
                            contentDescription = "CivLib Civil Engineering Library",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CivLib",
                        fontFamily = PlayfairDisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        letterSpacing = (-0.5).sp,
                        color = SandCreamLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your personal Civil Engineering library,\nYear 1–4 roadmap & 2-page PDF book reader.",
                        fontFamily = LoraFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = SandCreamMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Button(
                        onClick = {
                            isSigningIn = true
                            val auth = safeFirebaseAuthOrNull()
                            val resId = context.resources.getIdentifier(
                                "default_web_client_id",
                                "string",
                                context.packageName
                            )
                            if (auth == null || resId == 0) {
                                isSigningIn = false
                                surveyStep = 0
                                showSurveyWizard = true
                            } else {
                                onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = {
                                        isSigningIn = false
                                        val fbUser = auth.currentUser
                                        if (fbUser != null) {
                                            if (studentName.isBlank()) studentName = fbUser.displayName.orEmpty()
                                            if (studentEmail.isBlank()) studentEmail = fbUser.email.orEmpty()
                                        }
                                        surveyStep = 0
                                        showSurveyWizard = true
                                    },
                                    onAuthError = {
                                        isSigningIn = false
                                        surveyStep = 0
                                        showSurveyWizard = true
                                    },
                                    scope = scope,
                                    onAuthCancelled = {
                                        isSigningIn = false
                                        surveyStep = 0
                                        showSurveyWizard = true
                                    }
                                )
                            }
                        },
                        enabled = !isSigningIn,
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SandCream,
                            contentColor = EspressoInk,
                            disabledContainerColor = SageOlive,
                            disabledContentColor = EspressoInk
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isSigningIn) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = EspressoInk,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Connecting Google Profile…",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = EspressoInk
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            ) {
                                Text(
                                    text = "G",
                                    fontFamily = DmSansFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = EspressoInk
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sign in with Google & Personalize",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.5.sp,
                                color = EspressoInk
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Warm Sand-Cream editorial feature card
                    Surface(
                        color = SandCream,
                        contentColor = EspressoInk,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AuthBenefitRow(
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                title = "Personalized Year 1–4 Recommendations",
                                subtitle = "Answer 4 quick questions to tailor your textbook shelf to your year & course"
                            )
                            AuthBenefitRow(
                                icon = Icons.Filled.Bookmark,
                                title = "Personal Bookmarks & Study Notes",
                                subtitle = "Save your shelf, current pages, and exam notes to your profile"
                            )
                            AuthBenefitRow(
                                icon = Icons.Filled.CloudDone,
                                title = "2-Page Landscape Book Reader",
                                subtitle = "Read downloaded or imported PDFs as an open two-page textbook"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Takes 15 seconds · Customize your year, course focus, and recommendations.",
                        fontFamily = DmSansFontFamily,
                        fontSize = 12.sp,
                        color = SandCreamMuted,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // 4-Step Interactive Button Survey Wizard
                    StudentProfileSurveyCard(
                        step = surveyStep,
                        studentName = studentName,
                        onNameChange = { studentName = it },
                        studentEmail = studentEmail,
                        onEmailChange = { studentEmail = it },
                        selectedYear = selectedYear,
                        onSelectYear = { yr ->
                            selectedYear = yr
                            // Auto-pick a matching subject for that year if current doesn't match
                            val defaultSubj = YearSurveyOptions.firstOrNull { it.year == yr }?.defaultSubject
                                ?: CivilSubject.STRUCTURAL
                            selectedSubjectKey = defaultSubj.name
                        },
                        selectedSubjectKey = selectedSubjectKey,
                        onSelectSubjectKey = { selectedSubjectKey = it },
                        selectedGoal = selectedGoal,
                        onSelectGoal = { selectedGoal = it },
                        onBack = {
                            if (surveyStep > 0) surveyStep -= 1
                            else if (!initialShowSurvey) showSurveyWizard = false
                        },
                        onNext = {
                            if (surveyStep < 3) {
                                surveyStep += 1
                            } else {
                                val chosenSubject = CivilSubject.fromKey(selectedSubjectKey)
                                val goalSpec = StudyGoalOptions.firstOrNull { it.title == selectedGoal }
                                    ?: StudyGoalOptions.first()
                                val finalName = studentName.trim().ifBlank {
                                    studentEmail.substringBefore("@").ifBlank { "Civil Engineer" }
                                }
                                val finalEmail = studentEmail.trim().ifBlank {
                                    "${finalName.lowercase().replace(Regex("[^a-z0-9]"), "")}@gmail.com"
                                }
                                val generatedMantra = "${chosenSubject.shortName} (${chosenSubject.courseCode}) · ${goalSpec.title}"
                                onSurveyCompleted(
                                    ProfileSurveyResult(
                                        displayName = finalName,
                                        email = finalEmail,
                                        curriculumYear = selectedYear,
                                        primarySubjectKey = chosenSubject.name,
                                        studyGoal = selectedGoal,
                                        studyMantra = generatedMantra
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentProfileSurveyCard(
    step: Int,
    studentName: String,
    onNameChange: (String) -> Unit,
    studentEmail: String,
    onEmailChange: (String) -> Unit,
    selectedYear: Int,
    onSelectYear: (Int) -> Unit,
    selectedSubjectKey: String,
    onSelectSubjectKey: (String) -> Unit,
    selectedGoal: String,
    onSelectGoal: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Badge + Step Progress Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = SageOlive.copy(alpha = 0.28f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = SandCream,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Google Profile Setup · Question ${step + 1} of 4",
                        fontFamily = DmSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = SandCreamLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4-Segment Progress Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (idx in 0..3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (idx <= step) SandCream else EspressoCard
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main Survey Card
        Surface(
            color = SandCream,
            contentColor = EspressoInk,
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = step,
                label = "survey_step_transition"
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    when (currentStep) {
                        0 -> {
                            Text(
                                text = "1. Connect Your Google Profile",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp,
                                color = EspressoInk
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "What is your name and Google email so we can attach your personal shelf and notes?",
                                fontFamily = LoraFontFamily,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp,
                                color = EspressoInkMuted
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedTextField(
                                value = studentName,
                                onValueChange = { if (it.length <= 60) onNameChange(it) },
                                label = { Text("Your Name (e.g. Engr. Alex Rivera)") },
                                placeholder = { Text("Enter your full name or nickname") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = EspressoInk,
                                    unfocusedTextColor = EspressoInk,
                                    focusedBorderColor = EspressoInk,
                                    unfocusedBorderColor = EspressoInk.copy(alpha = 0.35f),
                                    focusedLabelColor = EspressoInk,
                                    unfocusedLabelColor = EspressoInkMuted,
                                    cursorColor = EspressoInk
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = studentEmail,
                                onValueChange = { if (it.length <= 80) onEmailChange(it) },
                                label = { Text("Google Email (optional)") },
                                placeholder = { Text("you@gmail.com") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = EspressoInk,
                                    unfocusedTextColor = EspressoInk,
                                    focusedBorderColor = EspressoInk,
                                    unfocusedBorderColor = EspressoInk.copy(alpha = 0.35f),
                                    focusedLabelColor = EspressoInk,
                                    unfocusedLabelColor = EspressoInkMuted,
                                    cursorColor = EspressoInk
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Quick-tap preset name chips if user wants 1-tap setup
                            Text(
                                text = "Or tap a quick title prefix:",
                                fontFamily = DmSansFontFamily,
                                fontSize = 11.5.sp,
                                color = EspressoInkMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Engr. ", "BSCE Student", "Future RCE").forEach { preset ->
                                    Surface(
                                        onClick = {
                                            onNameChange(
                                                if (preset == "Engr. " && !studentName.startsWith("Engr."))
                                                    "Engr. $studentName"
                                                else preset
                                            )
                                        },
                                        shape = RoundedCornerShape(999.dp),
                                        color = SageOlive.copy(alpha = 0.35f)
                                    ) {
                                        Text(
                                            text = preset.trim(),
                                            fontFamily = DmSansFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp,
                                            color = EspressoInk,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        1 -> {
                            Text(
                                text = "2. What Year Are You In?",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp,
                                color = EspressoInk
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap your current BS Civil Engineering curriculum year so we can prioritize your textbooks:",
                                fontFamily = LoraFontFamily,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp,
                                color = EspressoInkMuted
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                YearSurveyOptions.forEach { option ->
                                    val isSelected = selectedYear == option.year
                                    SurveyChoiceButton(
                                        badge = option.badge,
                                        title = option.title,
                                        subtitle = option.subtitle,
                                        selected = isSelected,
                                        onClick = { onSelectYear(option.year) }
                                    )
                                }
                            }
                        }

                        2 -> {
                            Text(
                                text = "3. Current Course or Subject Focus?",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = EspressoInk
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pick the Civil Engineering subject you're studying most right now (Year $selectedYear courses highlighted first):",
                                fontFamily = LoraFontFamily,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp,
                                color = EspressoInkMuted
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            val orderedSubjects = remember(selectedYear) {
                                CivilSubject.entries.sortedWith(
                                    compareBy<CivilSubject> { if (it.curriculumYear == selectedYear) 0 else 1 }
                                        .thenBy { it.sequenceOrder }
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                orderedSubjects.chunked(2).forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        pair.forEach { subject ->
                                            val isSelected = selectedSubjectKey == subject.name
                                            val isYearMatch = subject.curriculumYear == selectedYear
                                            Surface(
                                                onClick = { onSelectSubjectKey(subject.name) },
                                                shape = RoundedCornerShape(14.dp),
                                                color = if (isSelected) EspressoInk else Color.White.copy(alpha = 0.72f),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(
                                                        width = if (isSelected) 2.dp else if (isYearMatch) 1.5.dp else 1.dp,
                                                        color = if (isSelected) EspressoInk
                                                        else if (isYearMatch) SageOlive
                                                        else EspressoInk.copy(alpha = 0.16f),
                                                        shape = RoundedCornerShape(14.dp)
                                                    )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "${subject.courseCode} · Y${subject.curriculumYear}",
                                                            fontFamily = DmSansFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.5.sp,
                                                            color = if (isSelected) SandCream else subject.accentColor
                                                        )
                                                        if (isSelected) {
                                                            Icon(
                                                                imageVector = Icons.Filled.CheckCircle,
                                                                contentDescription = null,
                                                                tint = SandCream,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Text(
                                                        text = subject.shortName,
                                                        fontFamily = DmSansFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.5.sp,
                                                        color = if (isSelected) SandCreamLight else EspressoInk,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = subject.displayName,
                                                        fontFamily = DmSansFontFamily,
                                                        fontSize = 10.5.sp,
                                                        lineHeight = 13.sp,
                                                        color = if (isSelected) SandCreamMuted else EspressoInkMuted,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            val chosenSubject = CivilSubject.fromKey(selectedSubjectKey)
                            Text(
                                text = "4. What Is Your Primary Study Goal?",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = EspressoInk
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "We'll curate your Recommend tab and Home banner around this goal:",
                                fontFamily = LoraFontFamily,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp,
                                color = EspressoInkMuted
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                StudyGoalOptions.forEach { goal ->
                                    val isSelected = selectedGoal == goal.title
                                    SurveyChoiceButton(
                                        badge = null,
                                        title = goal.title,
                                        subtitle = goal.subtitle,
                                        selected = isSelected,
                                        onClick = { onSelectGoal(goal.title) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Live Recommendation Preview Summary
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SageOlive.copy(alpha = 0.28f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(
                                        text = "YOUR PERSONALIZED PROFILE PREVIEW",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = EspressoInkMuted
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "${studentName.ifBlank { "Civil Engineer" }} · Year $selectedYear · ${chosenSubject.shortName} (${chosenSubject.courseCode})",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = EspressoInk
                                    )
                                    Text(
                                        text = "Goal: $selectedGoal",
                                        fontFamily = LoraFontFamily,
                                        fontSize = 12.sp,
                                        color = EspressoInkMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom Navigation Buttons (Back / Next or Finish)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = EspressoInk,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (currentStep == 0) "Back" else "Previous",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                color = EspressoInk
                            )
                        }

                        Button(
                            onClick = onNext,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EspressoInk,
                                contentColor = SandCreamLight
                            ),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Text(
                                text = if (currentStep < 3) "Next Question" else "Build My Recommendations",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = SandCreamLight
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (currentStep < 3) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = SandCreamLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SurveyChoiceButton(
    badge: String?,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) EspressoInk else Color.White.copy(alpha = 0.75f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) EspressoInk else EspressoInk.copy(alpha = 0.16f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (badge != null) {
                    Text(
                        text = badge,
                        fontFamily = DmSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        color = if (selected) SandCream else EspressoInkMuted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = title,
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (selected) SandCreamLight else EspressoInk
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = if (selected) SandCreamMuted else EspressoInkMuted
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) SandCream else EspressoInk.copy(alpha = 0.1f))
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Selected",
                        tint = EspressoInk,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthBenefitRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(SageOlive.copy(alpha = 0.45f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EspressoInk,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                color = EspressoInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = DmSansFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.5.sp,
                color = EspressoInkMuted
            )
        }
    }
}
