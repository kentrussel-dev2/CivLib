package com.example.data.firebase

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

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
    val studyGoal: String = "Current Semester Courses",
    val onboardingCompleted: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

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
