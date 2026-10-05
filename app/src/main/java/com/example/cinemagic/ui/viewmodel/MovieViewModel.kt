package com.example.cinemagic.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinemagic.data.local.AppDatabase
import com.example.cinemagic.data.local.MovieRepository
import com.example.cinemagic.data.remote.GeminiMovieService
import com.example.cinemagic.domain.AspectRatioMode
import com.example.cinemagic.domain.DirectorStyle
import com.example.cinemagic.domain.MovieGenre
import com.example.cinemagic.domain.MovieProject
import com.example.cinemagic.domain.PresetTemplate
import com.example.cinemagic.domain.ScriptScene
import com.example.cinemagic.util.CinematicAudioPlayer
import com.example.cinemagic.util.ImageStorageHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ScreenState {
    HOME,
    CREATE,
    GENERATING,
    PLAYER,
    SCRIPT_EDITOR
}

data class CreateFormState(
    val castBitmap: Bitmap? = null,
    val castPhotoPath: String? = null,
    val characterName: String = "The Protagonist",
    val characterRole: String = "Lead Role",
    val visualDescription: String = "Charismatic hero with intense presence",
    val scriptPrompt: String = "",
    val genre: MovieGenre = MovieGenre.CYBERPUNK,
    val directorStyle: DirectorStyle = DirectorStyle.VILLENEUVE,
    val aspectRatio: AspectRatioMode = AspectRatioMode.ANAMORPHIC,
    val isAnalyzingPhoto: Boolean = false,
    val analysisMessage: String? = null
)

data class GenerationState(
    val currentStage: String = "Preparing Production Studio...",
    val progress: Float = 0.05f,
    val logMessages: List<String> = emptyList(),
    val error: String? = null
)

data class PlayerState(
    val currentSceneIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isSoundtrackOn: Boolean = true,
    val showSubtitles: Boolean = true,
    val showCredits: Boolean = false,
    val sceneTimeElapsedMs: Long = 0L,
    val sceneDurationMs: Long = 9000L
)

class MovieViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MovieRepository
    private val geminiService = GeminiMovieService()
    val audioPlayer = CinematicAudioPlayer(application.applicationContext)

    val allMovies: StateFlow<List<MovieProject>>

    private val _currentScreen = MutableStateFlow(ScreenState.HOME)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _createForm = MutableStateFlow(CreateFormState())
    val createForm: StateFlow<CreateFormState> = _createForm.asStateFlow()

    private val _generationState = MutableStateFlow(GenerationState())
    val generationState: StateFlow<GenerationState> = _generationState.asStateFlow()

    private val _activeMovie = MutableStateFlow<MovieProject?>(null)
    val activeMovie: StateFlow<MovieProject?> = _activeMovie.asStateFlow()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var playerTimerJob: Job? = null

    init {
        val db = AppDatabase.getInstance(application)
        repository = MovieRepository(db.movieDao())

        allMovies = repository.allMovies.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        audioPlayer.onDialogueCompleted = {
            viewModelScope.launch {
                // When character dialogue finishes, let visual linger for 2.5 seconds before auto-advancing
                delay(2500)
                if (_playerState.value.isPlaying && !_playerState.value.showCredits) {
                    nextScene()
                }
            }
        }
    }

    fun navigateTo(screen: ScreenState) {
        if (_currentScreen.value == ScreenState.PLAYER && screen != ScreenState.PLAYER) {
            pauseMovie()
        }
        _currentScreen.value = screen
    }

    fun applyPreset(preset: PresetTemplate) {
        val sampleActorBmp = ImageStorageHelper.createSampleActorBitmap(preset.characterName, preset.genre)
        viewModelScope.launch {
            val path = ImageStorageHelper.saveBitmapToFile(getApplication(), sampleActorBmp, "preset_cast_")
            _createForm.update {
                it.copy(
                    castBitmap = sampleActorBmp,
                    castPhotoPath = path,
                    characterName = preset.characterName,
                    characterRole = preset.characterRole,
                    scriptPrompt = preset.initialScript,
                    genre = preset.genre,
                    directorStyle = preset.directorStyle
                )
            }
            _currentScreen.value = ScreenState.CREATE
        }
    }

    fun onPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _createForm.update { it.copy(isAnalyzingPhoto = true, analysisMessage = "Scanning portrait with AI Casting Director...") }

            val savedPath = ImageStorageHelper.saveImageFromUri(getApplication(), uri)
            val bitmap = savedPath?.let { ImageStorageHelper.loadBitmap(it) }

            if (bitmap != null) {
                _createForm.update { it.copy(castBitmap = bitmap, castPhotoPath = savedPath) }

                // Analyze with Gemini
                val profile = geminiService.analyzeCastPhoto(bitmap)
                _createForm.update {
                    it.copy(
                        isAnalyzingPhoto = false,
                        analysisMessage = "Casting match: ${profile.characterName} as ${profile.characterRole}",
                        characterName = profile.characterName,
                        characterRole = profile.characterRole,
                        visualDescription = profile.visualDescription,
                        genre = profile.suggestedGenre
                    )
                }
            } else {
                _createForm.update { it.copy(isAnalyzingPhoto = false, analysisMessage = "Could not load photo") }
            }
        }
    }

    fun updateScriptPrompt(prompt: String) {
        _createForm.update { it.copy(scriptPrompt = prompt) }
    }

    fun updateGenre(genre: MovieGenre) {
        _createForm.update { it.copy(genre = genre) }
    }

    fun updateDirector(director: DirectorStyle) {
        _createForm.update { it.copy(directorStyle = director) }
    }

    fun updateAspectRatio(ratio: AspectRatioMode) {
        _createForm.update { it.copy(aspectRatio = ratio) }
    }

    fun updateCharacterInfo(name: String, role: String) {
        _createForm.update { it.copy(characterName = name, characterRole = role) }
    }

    fun generateMovie() {
        val form = _createForm.value
        val brief = form.scriptPrompt.ifBlank { "A high-stakes mission where ${form.characterName} faces an unexpected rival." }

        _currentScreen.value = ScreenState.GENERATING
        _generationState.value = GenerationState(
            currentStage = "Writing Screenplay with AI Director...",
            progress = 0.15f,
            logMessages = listOf("Initialized production for: ${form.genre.displayName}", "Consulting director aesthetic: ${form.directorStyle.displayName}")
        )

        viewModelScope.launch {
            try {
                // Ensure we have a cast bitmap
                var castBmp = form.castBitmap
                var castPath = form.castPhotoPath
                if (castBmp == null) {
                    castBmp = ImageStorageHelper.createSampleActorBitmap(form.characterName, form.genre)
                    castPath = ImageStorageHelper.saveBitmapToFile(getApplication(), castBmp, "default_cast_")
                    _createForm.update { it.copy(castBitmap = castBmp, castPhotoPath = castPath) }
                }

                // Stage 1: Generate Script
                addLog("Screenplay draft in progress...")
                delay(800)
                val generatedData = geminiService.generateMovie(
                    briefScript = brief,
                    genre = form.genre,
                    director = form.directorStyle,
                    characterName = form.characterName,
                    characterRole = form.characterRole,
                    visualDesc = form.visualDescription
                )

                _generationState.update {
                    it.copy(
                        currentStage = "Cinematography & Visual Scene Generation...",
                        progress = 0.45f
                    )
                }
                addLog("Screenplay generated: \"${generatedData.title}\" (4 Scenes)")
                addLog("Camera blocking & anamorphic lighting...")

                // Stage 2: Render Scenes
                val finalScenes = mutableListOf<ScriptScene>()
                val totalScenes = generatedData.scenes.size

                generatedData.scenes.forEachIndexed { index, scene ->
                    val sceneNum = index + 1
                    _generationState.update {
                        it.copy(
                            currentStage = "Rendering Scene $sceneNum of $totalScenes: ${scene.title}",
                            progress = 0.45f + (0.40f * (sceneNum.toFloat() / totalScenes))
                        )
                    }
                    addLog("Filming: ${scene.location} [${scene.cameraShot}]")

                    // Try generating real AI scene visual or render stylized cinematic frame
                    var renderedImagePath: String? = null
                    try {
                        val aiBmp = geminiService.generateSceneImage(scene.imagePrompt, form.aspectRatio.label)
                        if (aiBmp != null) {
                            renderedImagePath = ImageStorageHelper.saveBitmapToFile(getApplication(), aiBmp, "gemini_scene_")
                            addLog("AI Keyframe synthesized for Scene $sceneNum")
                        }
                    } catch (e: Exception) {
                        // fallback to cinematic compositor
                    }

                    if (renderedImagePath == null) {
                        renderedImagePath = ImageStorageHelper.renderCinematicFrame(
                            context = getApplication(),
                            castBitmap = castBmp,
                            sceneNumber = sceneNum,
                            sceneTitle = scene.title,
                            genre = form.genre,
                            actionSummary = scene.actionDescription
                        )
                    }

                    finalScenes.add(scene.copy(imagePath = renderedImagePath))
                }

                // Stage 3: Audio Mastering & Wrap
                _generationState.update {
                    it.copy(
                        currentStage = "Sound Mixing & Mastering Final Cut...",
                        progress = 0.95f
                    )
                }
                addLog("Mastering soundtrack: ${generatedData.soundtrackVibe}")
                delay(600)

                val movie = MovieProject(
                    title = generatedData.title,
                    logline = generatedData.logline,
                    genre = form.genre,
                    directorStyle = form.directorStyle,
                    aspectRatio = form.aspectRatio,
                    castPhotoPath = castPath,
                    castCharacterName = form.characterName,
                    castCharacterRole = form.characterRole,
                    promptScript = brief,
                    scenes = finalScenes,
                    soundtrackVibe = generatedData.soundtrackVibe
                )

                repository.saveMovie(movie)
                _activeMovie.value = movie
                addLog("Movie wrapped successfully!")

                delay(500)
                startMoviePlayback(movie)

            } catch (e: Exception) {
                e.printStackTrace()
                _generationState.update {
                    it.copy(error = "Production error: ${e.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    private fun addLog(msg: String) {
        _generationState.update {
            it.copy(logMessages = it.logMessages + msg)
        }
    }

    fun startMoviePlayback(movie: MovieProject, startSceneIndex: Int = 0) {
        _activeMovie.value = movie
        _currentScreen.value = ScreenState.PLAYER

        val scene = movie.scenes.getOrNull(startSceneIndex)
        val durationMs = ((scene?.durationSeconds ?: 9) * 1000).toLong()

        _playerState.value = PlayerState(
            currentSceneIndex = startSceneIndex,
            isPlaying = true,
            isSoundtrackOn = true,
            showSubtitles = true,
            showCredits = false,
            sceneTimeElapsedMs = 0L,
            sceneDurationMs = durationMs
        )

        audioPlayer.startCinematicAmbientDrone(0.40f)
        playCurrentSceneDialogue()
        startPlaybackTimer()
    }

    fun togglePlayPause() {
        val current = _playerState.value.isPlaying
        if (current) {
            pauseMovie()
        } else {
            resumeMovie()
        }
    }

    fun pauseMovie() {
        _playerState.update { it.copy(isPlaying = false) }
        playerTimerJob?.cancel()
        audioPlayer.stopDialogue()
        audioPlayer.stopAmbient()
    }

    fun resumeMovie() {
        if (_playerState.value.showCredits) {
            startMoviePlayback(_activeMovie.value ?: return, 0)
            return
        }
        _playerState.update { it.copy(isPlaying = true) }
        if (_playerState.value.isSoundtrackOn) {
            audioPlayer.startCinematicAmbientDrone(0.40f)
        }
        playCurrentSceneDialogue()
        startPlaybackTimer()
    }

    fun toggleSoundtrack() {
        val newState = !_playerState.value.isSoundtrackOn
        _playerState.update { it.copy(isSoundtrackOn = newState) }
        if (newState && _playerState.value.isPlaying) {
            audioPlayer.startCinematicAmbientDrone(0.40f)
        } else {
            audioPlayer.stopAmbient()
        }
    }

    fun toggleSubtitles() {
        _playerState.update { it.copy(showSubtitles = !it.showSubtitles) }
    }

    fun nextScene() {
        val movie = _activeMovie.value ?: return
        val nextIdx = _playerState.value.currentSceneIndex + 1

        if (nextIdx >= movie.scenes.size) {
            // End of movie -> roll credits!
            _playerState.update { it.copy(showCredits = true, isPlaying = false) }
            audioPlayer.stopDialogue()
            playerTimerJob?.cancel()
        } else {
            jumpToScene(nextIdx)
        }
    }

    fun previousScene() {
        val currentIdx = _playerState.value.currentSceneIndex
        if (currentIdx > 0) {
            jumpToScene(currentIdx - 1)
        } else {
            jumpToScene(0)
        }
    }

    fun jumpToScene(index: Int) {
        val movie = _activeMovie.value ?: return
        val validIdx = index.coerceIn(0, movie.scenes.size - 1)
        val scene = movie.scenes[validIdx]
        val dur = (scene.durationSeconds * 1000).toLong()

        _playerState.update {
            it.copy(
                currentSceneIndex = validIdx,
                showCredits = false,
                sceneTimeElapsedMs = 0L,
                sceneDurationMs = dur,
                isPlaying = true
            )
        }

        playCurrentSceneDialogue()
        startPlaybackTimer()
    }

    private fun playCurrentSceneDialogue() {
        val movie = _activeMovie.value ?: return
        val scene = movie.scenes.getOrNull(_playerState.value.currentSceneIndex) ?: return
        if (scene.dialogue.isNotBlank()) {
            audioPlayer.speakDialogue(scene.dialogue, "scene_${scene.sceneNumber}")
        }
    }

    private fun startPlaybackTimer() {
        playerTimerJob?.cancel()
        playerTimerJob = viewModelScope.launch {
            val interval = 100L
            while (isActive && _playerState.value.isPlaying && !_playerState.value.showCredits) {
                delay(interval)
                _playerState.update { current ->
                    val updatedElapsed = current.sceneTimeElapsedMs + interval
                    current.copy(sceneTimeElapsedMs = updatedElapsed)
                }
            }
        }
    }

    fun openScriptEditor(movie: MovieProject) {
        _activeMovie.value = movie
        _currentScreen.value = ScreenState.SCRIPT_EDITOR
    }

    fun updateSceneDialogueInEditor(sceneIndex: Int, newDialogue: String) {
        val movie = _activeMovie.value ?: return
        if (sceneIndex in movie.scenes.indices) {
            val updatedScenes = movie.scenes.toMutableList()
            updatedScenes[sceneIndex] = updatedScenes[sceneIndex].copy(dialogue = newDialogue)
            val updatedMovie = movie.copy(scenes = updatedScenes)
            _activeMovie.value = updatedMovie
            viewModelScope.launch {
                repository.saveMovie(updatedMovie)
            }
        }
    }

    fun deleteMovie(movie: MovieProject) {
        viewModelScope.launch {
            repository.deleteMovie(movie.id)
            if (_activeMovie.value?.id == movie.id) {
                _activeMovie.value = null
                _currentScreen.value = ScreenState.HOME
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
