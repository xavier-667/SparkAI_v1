package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ProjectItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val conversationsCount: Int = 0,
    val documentsCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MemoryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val key: String = "",
    val value: String = "",
    val category: String = "Preference",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class StudyQuizCard(
    val id: String,
    val subject: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class ResearchReport(
    val id: String,
    val title: String,
    val query: String,
    val executiveSummary: String,
    val keyFindings: List<String>,
    val sources: List<GroundingCitation>,
    val dateFormatted: String
)
