package com.example.cinemagic.data.local

import com.example.cinemagic.domain.AspectRatioMode
import com.example.cinemagic.domain.DirectorStyle
import com.example.cinemagic.domain.MovieGenre
import com.example.cinemagic.domain.MovieProject
import com.example.cinemagic.domain.ScriptScene
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MovieRepository(private val movieDao: MovieDao) {

    val allMovies: Flow<List<MovieProject>> = movieDao.getAllMoviesWithScenes().map { list ->
        list.map { it.toDomain() }
    }

    fun getMovie(movieId: String): Flow<MovieProject?> = movieDao.getMovieById(movieId).map { it?.toDomain() }

    suspend fun saveMovie(movie: MovieProject) {
        val movieEntity = MovieEntity(
            id = movie.id,
            title = movie.title,
            logline = movie.logline,
            genre = movie.genre.name,
            directorStyle = movie.directorStyle.name,
            aspectRatio = movie.aspectRatio.name,
            castPhotoPath = movie.castPhotoPath,
            castCharacterName = movie.castCharacterName,
            castCharacterRole = movie.castCharacterRole,
            promptScript = movie.promptScript,
            soundtrackVibe = movie.soundtrackVibe,
            createdAt = movie.createdAt
        )
        val sceneEntities = movie.scenes.map { scene ->
            SceneEntity(
                movieId = movie.id,
                sceneNumber = scene.sceneNumber,
                title = scene.title,
                location = scene.location,
                cameraShot = scene.cameraShot,
                actionDescription = scene.actionDescription,
                speaker = scene.speaker,
                dialogue = scene.dialogue,
                soundEffects = scene.soundEffects,
                imagePrompt = scene.imagePrompt,
                imagePath = scene.imagePath,
                durationSeconds = scene.durationSeconds
            )
        }

        movieDao.insertMovie(movieEntity)
        movieDao.deleteScenesForMovie(movie.id)
        movieDao.insertScenes(sceneEntities)
    }

    suspend fun deleteMovie(movieId: String) {
        movieDao.deleteScenesForMovie(movieId)
        movieDao.deleteMovieById(movieId)
    }

    private fun MovieWithScenes.toDomain(): MovieProject {
        val genre = try { MovieGenre.valueOf(movie.genre) } catch (e: Exception) { MovieGenre.CYBERPUNK }
        val director = try { DirectorStyle.valueOf(movie.directorStyle) } catch (e: Exception) { DirectorStyle.VILLENEUVE }
        val aspect = try { AspectRatioMode.valueOf(movie.aspectRatio) } catch (e: Exception) { AspectRatioMode.ANAMORPHIC }

        val sortedScenes = scenes.sortedBy { it.sceneNumber }.map { s ->
            ScriptScene(
                sceneNumber = s.sceneNumber,
                title = s.title,
                location = s.location,
                cameraShot = s.cameraShot,
                actionDescription = s.actionDescription,
                speaker = s.speaker,
                dialogue = s.dialogue,
                soundEffects = s.soundEffects,
                imagePrompt = s.imagePrompt,
                imagePath = s.imagePath,
                durationSeconds = s.durationSeconds
            )
        }

        return MovieProject(
            id = movie.id,
            title = movie.title,
            logline = movie.logline,
            genre = genre,
            directorStyle = director,
            aspectRatio = aspect,
            castPhotoPath = movie.castPhotoPath,
            castCharacterName = movie.castCharacterName,
            castCharacterRole = movie.castCharacterRole,
            promptScript = movie.promptScript,
            scenes = sortedScenes,
            soundtrackVibe = movie.soundtrackVibe,
            createdAt = movie.createdAt
        )
    }
}
