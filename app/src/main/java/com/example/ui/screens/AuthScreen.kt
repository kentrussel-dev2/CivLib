package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
    val email: String? = null,
    val curriculumYear: Int,
    val primarySubjectKey: String,
    val selectedSubjectKeys: List<String>,
    val studyGoal: String,
    val selectedGoals: List<String>,
    val studyMantra: String
)

private val EngineeringCoursePrograms = listOf(
    "BS Civil Engineering (BSCE)",
    "MS / Masters in Civil & Structural Eng. (MSCE)",
    "Mechanical, Electrical & Allied Engineering",
    "Architecture & Construction Management",
    "Engineering Math, Physics & General STEM"
)

private data class YearSurveyOption(
    val year: Int,
    val badge: String,
    val title: String,
    val subtitle: String,
    val defaultSubjects: List<CivilSubject>
)

private val YearSurveyOptions = listOf(
    YearSurveyOption(
        year = 1,
        badge = "YEAR 1",
        title = "1st Year · Engineering Foundations, Math & Physics",
        subtitle = "Engineering Calculus, Physics, Statics & Dynamics (ENGR 101 / CE 101)",
        defaultSubjects = listOf(CivilSubject.MATH_PHYSICS, CivilSubject.MECHANICS)
    ),
    YearSurveyOption(
        year = 2,
        badge = "YEAR 2",
        title = "2nd Year · Surveying, Materials & Allied Eng.",
        subtitle = "Geomatics, Strength of Materials, Concrete & Allied ME/EE (CE 201–210)",
        defaultSubjects = listOf(CivilSubject.SURVEYING, CivilSubject.MATERIALS, CivilSubject.ALLIED_ENGINEERING)
    ),
    YearSurveyOption(
        year = 3,
        badge = "YEAR 3",
        title = "3rd Year · Core Civil Disciplines",
        subtitle = "Structural Analysis, Hydraulics, Soil Mechanics & Highway (CE 301–308)",
        defaultSubjects = listOf(
            CivilSubject.STRUCTURAL,
            CivilSubject.HYDRAULICS,
            CivilSubject.GEOTECHNICAL,
            CivilSubject.TRANSPORTATION
        )
    ),
    YearSurveyOption(
        year = 4,
        badge = "YEAR 4",
        title = "4th Year · Design, Capstone & Board Exam",
        subtitle = "Reinforced Concrete/Steel Design, Environmental & Construction Mgmt (CE 401–408)",
        defaultSubjects = listOf(CivilSubject.ENVIRONMENTAL, CivilSubject.CONSTRUCTION, CivilSubject.STRUCTURAL)
    ),
    YearSurveyOption(
        year = 5,
        badge = "MASTERS / GRADUATE",
        title = "Masters, Graduate & Licensed Engineer (MSCE / RCE)",
        subtitle = "Finite Element Method (FEM), Structural Dynamics, Seismic, Plates & Shells (MSCE 501+)",
        defaultSubjects = listOf(CivilSubject.MASTERS_GRADUATE, CivilSubject.STRUCTURAL, CivilSubject.GEOTECHNICAL)
    )
)

private data class StudyGoalOption(
    val title: String,
    val subtitle: String,
    val shortBadge: String
)

private val StudyGoalOptions = listOf(
    StudyGoalOption(
        title = "Current Semester Courses",
        subtitle = "Follow my year's chronological textbooks & problem sets",
        shortBadge = "Semester Track"
    ),
    StudyGoalOption(
        title = "CE Licensure & Board Exam Review",
        subtitle = "Focus on high-yield board exam formulas, MSTE, HGE & PSAD",
        shortBadge = "Board Review"
    ),
    StudyGoalOption(
        title = "Structural & Foundation Design",
        subtitle = "Deep-dive into RC, Steel, Bridge & Geotechnical references",
        shortBadge = "Design Track"
    ),
    StudyGoalOption(
        title = "Masters, FEM & Earthquake Engineering",
        subtitle = "Graduate structural dynamics, finite element analysis, plates & shells",
        shortBadge = "Masters / MSCE"
    ),
    StudyGoalOption(
        title = "Engineering Math, Calculus & Physics Mastery",
        subtitle = "Differential/integral calculus, differential equations & engineering physics",
        shortBadge = "Math & Physics"
    ),
    StudyGoalOption(
        title = "Capstone Project & Field Reference",
        subtitle = "Codes, estimating, hydraulics & construction management",
        shortBadge = "Capstone & Field"
    )
)

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onSurveyCompleted: (ProfileSurveyResult) -> Unit = { onAuthSuccess() },
    initialShowSurvey: Boolean = true,
    initialName: String = "",
    initialEmail: String = "",
    initialYear: Int = 1,
    initialSubjectKey: String = CivilSubject.MECHANICS.name,
    initialSubjectKeys: List<String> = emptyList(),
    initialGoal: String = StudyGoalOptions.first().title,
    initialGoals: List<String> = emptyList(),
    onDismissSurvey: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var surveyStep by rememberSaveable { mutableIntStateOf(0) } // 0..3 (4 questions)
    var studentName by rememberSaveable { mutableStateOf(initialName) }
    var selectedCourseProgram by rememberSaveable {
        mutableStateOf(
            if (initialYear >= 5) EngineeringCoursePrograms[1] else EngineeringCoursePrograms[0]
        )
    }
    var selectedYear by rememberSaveable { mutableIntStateOf(initialYear.coerceIn(1, 5)) }

    val seedSubjects = remember(initialSubjectKeys, initialSubjectKey) {
        val list = initialSubjectKeys.filter { it.isNotBlank() }
        if (list.isNotEmpty()) list.joinToString("|")
        else initialSubjectKey.ifBlank { CivilSubject.MECHANICS.name }
    }
    var selectedSubjectsRaw by rememberSaveable { mutableStateOf(seedSubjects) }
    val selectedSubjectKeys = remember(selectedSubjectsRaw) {
        selectedSubjectsRaw.split("|").map { it.trim() }.filter { it.isNotBlank() }
            .ifEmpty { listOf(CivilSubject.MECHANICS.name) }
    }

    val seedGoals = remember(initialGoals, initialGoal) {
        val list = initialGoals.filter { it.isNotBlank() }
        if (list.isNotEmpty()) list.joinToString("|")
        else initialGoal.ifBlank { StudyGoalOptions.first().title }
    }
    var selectedGoalsRaw by rememberSaveable { mutableStateOf(seedGoals) }
    val selectedGoals = remember(selectedGoalsRaw) {
        selectedGoalsRaw.split("|").map { it.trim() }.filter { it.isNotBlank() }
            .ifEmpty { listOf(StudyGoalOptions.first().title) }
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Compact CivLib Header Emblem
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .border(2.dp, SageOlive, CircleShape)
                            .background(EspressoCard)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_library_hero_1795058942930),
                            contentDescription = "CivLib Civil Engineering Library",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CivLib Study Profile",
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = SandCreamLight
                        )
                        Text(
                            text = "Always-on offline study session · Saved in device cache",
                            fontFamily = DmSansFontFamily,
                            fontSize = 11.5.sp,
                            color = SandCreamMuted
                        )
                    }
                }

                // 4-Step Interactive Multi-Select Button Survey Wizard
                StudentProfileSurveyCard(
                    step = surveyStep,
                    studentName = studentName,
                    onNameChange = { studentName = it },
                    selectedCourseProgram = selectedCourseProgram,
                    onSelectCourseProgram = { courseProg ->
                        selectedCourseProgram = courseProg
                        when {
                            courseProg.contains("Masters", ignoreCase = true) -> {
                                selectedYear = 5
                                selectedSubjectsRaw = listOf(
                                    CivilSubject.MASTERS_GRADUATE.name,
                                    CivilSubject.STRUCTURAL.name,
                                    CivilSubject.GEOTECHNICAL.name
                                ).joinToString("|")
                            }
                            courseProg.contains("Mechanical", ignoreCase = true) -> {
                                selectedSubjectsRaw = listOf(
                                    CivilSubject.ALLIED_ENGINEERING.name,
                                    CivilSubject.MATH_PHYSICS.name,
                                    CivilSubject.MECHANICS.name
                                ).joinToString("|")
                            }
                            courseProg.contains("Math", ignoreCase = true) -> {
                                selectedSubjectsRaw = listOf(
                                    CivilSubject.MATH_PHYSICS.name,
                                    CivilSubject.MECHANICS.name
                                ).joinToString("|")
                            }
                            courseProg.contains("Architecture", ignoreCase = true) -> {
                                selectedSubjectsRaw = listOf(
                                    CivilSubject.CONSTRUCTION.name,
                                    CivilSubject.STRUCTURAL.name,
                                    CivilSubject.MATERIALS.name
                                ).joinToString("|")
                            }
                        }
                    },
                    selectedYear = selectedYear,
                    onSelectYear = { yr ->
                        selectedYear = yr
                        // Pre-select the subjects belonging to that year if user hasn't picked multiple yet
                        val yearSubjects = CivilSubject.entries
                            .filter { it.curriculumYear == yr }
                            .map { it.name }
                        if (yearSubjects.isNotEmpty()) {
                            selectedSubjectsRaw = yearSubjects.joinToString("|")
                        }
                    },
                    selectedSubjectKeys = selectedSubjectKeys,
                    onToggleSubjectKey = { key ->
                        val current = selectedSubjectKeys.toMutableList()
                        if (key in current) {
                            if (current.size > 1) current.remove(key)
                        } else {
                            current.add(key)
                        }
                        selectedSubjectsRaw = current.joinToString("|")
                    },
                    onSelectMultipleSubjects = { keys ->
                        if (keys.isNotEmpty()) {
                            selectedSubjectsRaw = keys.distinct().joinToString("|")
                        }
                    },
                    selectedGoals = selectedGoals,
                    onToggleGoal = { goalTitle ->
                        val current = selectedGoals.toMutableList()
                        if (goalTitle in current) {
                            if (current.size > 1) current.remove(goalTitle)
                        } else {
                            current.add(goalTitle)
                        }
                        selectedGoalsRaw = current.joinToString("|")
                    },
                    canDismissOnFirstStep = onDismissSurvey != null,
                    onBack = {
                        if (surveyStep > 0) {
                            surveyStep -= 1
                        } else {
                            onDismissSurvey?.invoke()
                        }
                    },
                    onNext = {
                        if (surveyStep < 3) {
                            surveyStep += 1
                        } else {
                            val chosenSubjects = selectedSubjectKeys.map { CivilSubject.fromKey(it) }.distinct()
                            val primarySubj = chosenSubjects.firstOrNull() ?: CivilSubject.MECHANICS
                            val finalName = studentName.trim().ifBlank { "Civil Engineer" }
                            val courseShort = when {
                                selectedCourseProgram.contains("Masters", ignoreCase = true) -> "MSCE"
                                selectedCourseProgram.contains("Mechanical", ignoreCase = true) -> "Allied Eng"
                                selectedCourseProgram.contains("Architecture", ignoreCase = true) -> "Arch/Const"
                                selectedCourseProgram.contains("Math", ignoreCase = true) -> "STEM/Math"
                                else -> "BSCE"
                            }
                            val subjectSummary = if (chosenSubjects.size <= 2) {
                                chosenSubjects.joinToString(", ") { it.shortName }
                            } else {
                                "${chosenSubjects.take(2).joinToString(", ") { it.shortName }} +${chosenSubjects.size - 2}"
                            }
                            val goalSummary = selectedGoals.mapNotNull { gTitle ->
                                StudyGoalOptions.firstOrNull { it.title == gTitle }?.shortBadge
                            }.ifEmpty { listOf("Semester Track") }.joinToString(" + ")

                            val generatedMantra = "$courseShort · $subjectSummary · $goalSummary"
                            onSurveyCompleted(
                                ProfileSurveyResult(
                                    displayName = finalName,
                                    email = initialEmail.takeIf { it.isNotBlank() },
                                    curriculumYear = selectedYear,
                                    primarySubjectKey = primarySubj.name,
                                    selectedSubjectKeys = chosenSubjects.map { it.name },
                                    studyGoal = selectedGoals.first(),
                                    selectedGoals = selectedGoals,
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

@Composable
fun StudentProfileSurveyCard(
    step: Int,
    studentName: String,
    onNameChange: (String) -> Unit,
    selectedCourseProgram: String = EngineeringCoursePrograms.first(),
    onSelectCourseProgram: (String) -> Unit = {},
    selectedYear: Int,
    onSelectYear: (Int) -> Unit,
    selectedSubjectKeys: List<String>,
    onToggleSubjectKey: (String) -> Unit,
    onSelectMultipleSubjects: (List<String>) -> Unit,
    selectedGoals: List<String>,
    onToggleGoal: (String) -> Unit,
    canDismissOnFirstStep: Boolean = false,
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
                        text = "Study Survey · Question ${step + 1} of 4",
                        fontFamily = DmSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = SandCreamLight
                    )
                }
            }

            if (step == 2) {
                Text(
                    text = "${selectedSubjectKeys.size} subjects chosen",
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SandCream
                )
            } else if (step == 3) {
                Text(
                    text = "${selectedGoals.size} goals chosen",
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SandCream
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

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
                                text = "1. Your Name & Engineering Course",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = EspressoInk
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No registration needed. Your name, course, reading progress, and notes stay saved automatically on your device.",
                                fontFamily = LoraFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 18.5.sp,
                                color = EspressoInkMuted
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = studentName,
                                onValueChange = { if (it.length <= 60) onNameChange(it) },
                                label = { Text("Your Name (e.g. Heart Garcia)") },
                                placeholder = { Text("e.g. Heart Garcia") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("survey_name_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Engr. ", "BSCE Student", "MSCE", "Future RCE").forEach { preset ->
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
                                            fontSize = 11.sp,
                                            color = EspressoInk,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "What is your Course / Engineering Program?",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = EspressoInk
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                EngineeringCoursePrograms.forEach { prog ->
                                    val isSelected = selectedCourseProgram == prog
                                    Surface(
                                        onClick = { onSelectCourseProgram(prog) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) EspressoInk else Color.White.copy(alpha = 0.75f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) EspressoInk else EspressoInk.copy(alpha = 0.16f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 9.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = prog,
                                                fontFamily = DmSansFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.5.sp,
                                                color = if (isSelected) SandCreamLight else EspressoInk
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = SandCream,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            Text(
                                text = "2. What Year / Level Are You In?",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp,
                                color = EspressoInk
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap your current BS Civil Engineering curriculum year so we can highlight your courses:",
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
                                text = "3. Course & Subject Focus",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = EspressoInk
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Choose as many Civil Engineering subjects as you want to focus on (tap multiple to select/deselect):",
                                fontFamily = LoraFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = EspressoInkMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Multi-Select Shortcut Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val yearSubjectNames = remember(selectedYear) {
                                    CivilSubject.entries
                                        .filter { it.curriculumYear == selectedYear }
                                        .map { it.name }
                                }
                                Surface(
                                    onClick = { onSelectMultipleSubjects(yearSubjectNames) },
                                    shape = RoundedCornerShape(999.dp),
                                    color = SageOlive.copy(alpha = 0.35f)
                                ) {
                                    Text(
                                        text = "Select All Year $selectedYear (${yearSubjectNames.size})",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = EspressoInk,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                    )
                                }
                                Surface(
                                    onClick = {
                                        onSelectMultipleSubjects(CivilSubject.entries.map { it.name })
                                    },
                                    shape = RoundedCornerShape(999.dp),
                                    color = SageOlive.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "Select All 9 Subjects",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = EspressoInk,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

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
                                            val isSelected = subject.name in selectedSubjectKeys
                                            val isYearMatch = subject.curriculumYear == selectedYear
                                            Surface(
                                                onClick = { onToggleSubjectKey(subject.name) },
                                                shape = RoundedCornerShape(14.dp),
                                                color = if (isSelected) EspressoInk else Color.White.copy(alpha = 0.72f),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = if (isSelected) 2.dp else if (isYearMatch) 1.5.dp else 1.dp,
                                                    color = if (isSelected) EspressoInk
                                                    else if (isYearMatch) SageOlive
                                                    else EspressoInk.copy(alpha = 0.16f)
                                                ),
                                                modifier = Modifier.weight(1f)
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
                                                                contentDescription = "Selected",
                                                                tint = SandCream,
                                                                modifier = Modifier.size(15.dp)
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
                            val chosenSubjects = selectedSubjectKeys.map { CivilSubject.fromKey(it) }.distinct()
                            Text(
                                text = "4. Primary Study Goals",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = EspressoInk
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Select one or more study goals — we'll rank your textbook recommendations based on everything you chose:",
                                fontFamily = LoraFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = EspressoInkMuted
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                StudyGoalOptions.forEach { goal ->
                                    val isSelected = goal.title in selectedGoals
                                    SurveyChoiceButton(
                                        badge = null,
                                        title = goal.title,
                                        subtitle = goal.subtitle,
                                        selected = isSelected,
                                        onClick = { onToggleGoal(goal.title) }
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
                                        text = "YOUR PERSONALIZED STUDY PLAN",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = EspressoInkMuted
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "${studentName.ifBlank { "Civil Engineer" }} · Year $selectedYear",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = EspressoInk
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Focus (${chosenSubjects.size}): ${chosenSubjects.joinToString(", ") { "${it.shortName} (${it.courseCode})" }}",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = EspressoInk
                                    )
                                    Text(
                                        text = "Goals (${selectedGoals.size}): ${selectedGoals.joinToString(" · ")}",
                                        fontFamily = LoraFontFamily,
                                        fontSize = 11.5.sp,
                                        color = EspressoInkMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom Navigation Buttons (Previous / Next Question or Recommend)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentStep > 0 || canDismissOnFirstStep) {
                            TextButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = EspressoInk,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentStep == 0) "Cancel" else "Previous",
                                    fontFamily = DmSansFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EspressoInk
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Button(
                            onClick = onNext,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EspressoInk,
                                contentColor = SandCreamLight
                            ),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag(if (currentStep < 3) "survey_next_button" else "survey_recommend_button")
                        ) {
                            Text(
                                text = if (currentStep < 3) "Next Question" else "Recommend",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp,
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
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) EspressoInk else EspressoInk.copy(alpha = 0.16f)
        ),
        modifier = Modifier.fillMaxWidth()
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
