package com.example.cinemagic.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class MovieWithScenes(
    @Embedded val movie: MovieEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "movieId"
    )
    val scenes: List<SceneEntity>
)

@Dao
interface MovieDao {
    @Transaction
    @Query("SELECT * FROM movies ORDER BY createdAt DESC")
    fun getAllMoviesWithScenes(): Flow<List<MovieWithScenes>>

    @Transaction
    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    fun getMovieById(movieId: String): Flow<MovieWithScenes?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenes(scenes: List<SceneEntity>)

    @Query("DELETE FROM scenes WHERE movieId = :movieId")
    suspend fun deleteScenesForMovie(movieId: String)

    @Query("DELETE FROM movies WHERE id = :movieId")
    suspend fun deleteMovieById(movieId: String)

    @Query("UPDATE scenes SET dialogue = :dialogue WHERE id = :sceneId")
    suspend fun updateSceneDialogue(sceneId: Long, dialogue: String)

    @Query("UPDATE scenes SET imagePath = :imagePath WHERE id = :sceneId")
    suspend fun updateSceneImage(sceneId: Long, imagePath: String)
}
