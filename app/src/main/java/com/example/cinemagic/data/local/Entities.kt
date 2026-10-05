package com.example.cinemagic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val logline: String,
    val genre: String,
    val directorStyle: String,
    val aspectRatio: String,
    val castPhotoPath: String?,
    val castCharacterName: String,
    val castCharacterRole: String,
    val promptScript: String,
    val soundtrackVibe: String,
    val createdAt: Long
)

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val movieId: String,
    val sceneNumber: Int,
    val title: String,
    val location: String,
    val cameraShot: String,
    val actionDescription: String,
    val speaker: String,
    val dialogue: String,
    val soundEffects: String,
    val imagePrompt: String,
    val imagePath: String?,
    val durationSeconds: Int
)
