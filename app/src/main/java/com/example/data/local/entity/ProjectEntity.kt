package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ProjectItem

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val conversationsCount: Int = 0,
    val documentsCount: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun toModel(): ProjectItem = ProjectItem(
        id = id,
        userId = userId,
        name = name,
        description = description,
        conversationsCount = conversationsCount,
        documentsCount = documentsCount
    )

    companion object {
        fun fromModel(model: ProjectItem): ProjectEntity = ProjectEntity(
            id = model.id,
            userId = model.userId,
            name = model.name,
            description = model.description,
            conversationsCount = model.conversationsCount,
            documentsCount = model.documentsCount
        )
    }
}
