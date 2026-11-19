package com.example.data.firebase

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

import com.example.data.local.BookEntity
import com.example.data.model.CivilSubject

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String? = null,
    val photoUrl: String? = null,
    val studyMantra: String = "BS Civil Engineering · Chronological Track",
    val curriculumYear: Long = 1L,
    val primarySubjectKey: String = "MECHANICS",
    val selectedSubjectKeys: List<String> = listOf("MECHANICS"),
    val studyGoal: String = "Current Semester Courses",
    val selectedGoals: List<String> = listOf("Current Semester Courses"),
    val onboardingCompleted: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val effectiveSubjectKeys: List<String>
        get() = selectedSubjectKeys.filter { it.isNotBlank() }.ifEmpty {
            listOf(primarySubjectKey.ifBlank { "MECHANICS" })
        }.distinct()

    val effectiveStudyGoals: List<String>
        get() = selectedGoals.filter { it.isNotBlank() }.ifEmpty {
            listOf(studyGoal.ifBlank { "Current Semester Courses" })
        }.distinct()
}

/**
 * Scores how strongly a [BookEntity] matches the student's chosen subjects (multi-select),
 * study goals (multi-select), and curriculum year from their study survey.
 */
fun scoreBookForProfile(book: BookEntity, profile: UserProfile?): Int {
    if (profile == null) return 0
    var score = 0
    val bookSubj = CivilSubject.fromKey(book.subjectKey)
    val chosenSubjs = profile.effectiveSubjectKeys.map { it.uppercase() }.toSet()
    val chosenGoals = profile.effectiveStudyGoals
    val targetYear = profile.curriculumYear.toInt().coerceIn(1, 5)

    // 1. Direct match with any of the student's chosen subject focuses (highest priority)
    val subjectIndex = profile.effectiveSubjectKeys.indexOfFirst {
        it.equals(book.subjectKey, ignoreCase = true)
    }
    if (subjectIndex >= 0) {
        score += 120 - (subjectIndex * 5).coerceAtMost(35)
    }

    // 2. Match with the student's current curriculum year
    if (bookSubj.curriculumYear == targetYear) {
        score += 45
    }

    // 3. Multi-select Study Goal & Program boosts
    val textBlob = "${book.title} ${book.description} ${book.courseCode}".lowercase()
    val mantraLower = profile.studyMantra.lowercase()
    if (mantraLower.contains("masters") || mantraLower.contains("msce") || targetYear == 5) {
        if (bookSubj == CivilSubject.MASTERS_GRADUATE || bookSubj == CivilSubject.STRUCTURAL || bookSubj == CivilSubject.GEOTECHNICAL) {
            score += 30
        }
    }
    if (mantraLower.contains("mechanical") || mantraLower.contains("electrical") || mantraLower.contains("allied")) {
        if (bookSubj == CivilSubject.ALLIED_ENGINEERING || bookSubj == CivilSubject.MATH_PHYSICS || bookSubj == CivilSubject.MECHANICS) {
            score += 30
        }
    }
    if (mantraLower.contains("math") || mantraLower.contains("physics") || mantraLower.contains("stem")) {
        if (bookSubj == CivilSubject.MATH_PHYSICS || bookSubj == CivilSubject.MECHANICS) {
            score += 35
        }
    }

    for (goal in chosenGoals) {
        when {
            goal.contains("Current Semester", ignoreCase = true) -> {
                if (bookSubj.curriculumYear == targetYear) score += 30
                if (book.isCuratedStarter) score += 15
            }
            goal.contains("Licensure", ignoreCase = true) || goal.contains("Board Exam", ignoreCase = true) -> {
                if (bookSubj.name in setOf("MECHANICS", "MATERIALS", "STRUCTURAL", "HYDRAULICS", "GEOTECHNICAL", "SURVEYING", "MATH_PHYSICS")) {
                    score += 25
                }
                if (textBlob.contains("analysis") || textBlob.contains("mechanics") ||
                    textBlob.contains("hydraulics") || textBlob.contains("principles") ||
                    textBlob.contains("design")
                ) {
                    score += 15
                }
            }
            goal.contains("Structural", ignoreCase = true) || goal.contains("Foundation", ignoreCase = true) -> {
                if (bookSubj.name in setOf("STRUCTURAL", "GEOTECHNICAL", "MATERIALS", "MASTERS_GRADUATE")) {
                    score += 35
                }
            }
            goal.contains("Masters", ignoreCase = true) || goal.contains("Thesis", ignoreCase = true) || goal.contains("FEM", ignoreCase = true) -> {
                if (bookSubj.name in setOf("MASTERS_GRADUATE", "STRUCTURAL", "GEOTECHNICAL", "MATH_PHYSICS")) {
                    score += 40
                }
            }
            goal.contains("Math", ignoreCase = true) || goal.contains("Physics", ignoreCase = true) || goal.contains("Allied", ignoreCase = true) -> {
                if (bookSubj.name in setOf("MATH_PHYSICS", "ALLIED_ENGINEERING", "MECHANICS")) {
                    score += 40
                }
            }
            goal.contains("Capstone", ignoreCase = true) || goal.contains("Field", ignoreCase = true) -> {
                if (bookSubj.name in setOf("CONSTRUCTION", "ENVIRONMENTAL", "TRANSPORTATION", "HYDRAULICS", "SURVEYING")) {
                    score += 35
                }
            }
        }
    }

    // 4. Bonus for offline-ready and curated starter volumes
    if (book.isOfflineAvailable) score += 12
    if (book.isCuratedStarter) score += 8
    if (bookSubj.name in chosenSubjs && bookSubj.curriculumYear == targetYear) score += 20

    return score
}

@Keep
@IgnoreExtraProperties
data class PersonalBook(
    @DocumentId val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val title: String = "",
    val author: String = "",
    val subjectKey: String = "STRUCTURAL",
    val courseCode: String = "",
    val coverUrl: String? = null,
    val coverArtKey: String = "structural",
    val totalPages: Long = 0L,
    val currentPage: Long = 1L,
    val bookmarked: Boolean = false,
    val completed: Boolean = false,
    val lastOpenedAt: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CloudStudyNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val pageNumber: Long = 1L,
    val chapterTitle: String = "",
    val noteText: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

sealed interface CloudUiState<out T> {
    data object Loading : CloudUiState<Nothing>
    data class Success<T>(val data: T) : CloudUiState<T>
    data class Error(val message: String) : CloudUiState<Nothing>
}
