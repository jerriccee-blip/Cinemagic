package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cinemagic.ui.screens.CreateMovieScreen
import com.example.cinemagic.ui.screens.GeneratingMovieScreen
import com.example.cinemagic.ui.screens.HomeScreen
import com.example.cinemagic.ui.screens.MoviePlayerScreen
import com.example.cinemagic.ui.screens.ScriptEditorScreen
import com.example.cinemagic.ui.viewmodel.MovieViewModel
import com.example.cinemagic.ui.viewmodel.ScreenState
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CinemaObsidian
                ) {
                    CineMagicApp()
                }
            }
        }
    }
}

@Composable
fun CineMagicApp(viewModel: MovieViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allMovies by viewModel.allMovies.collectAsStateWithLifecycle()
    val createForm by viewModel.createForm.collectAsStateWithLifecycle()
    val generationState by viewModel.generationState.collectAsStateWithLifecycle()
    val activeMovie by viewModel.activeMovie.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()

    // Handle back button per sub-screen
    BackHandler(enabled = currentScreen != ScreenState.HOME) {
        when (currentScreen) {
            ScreenState.CREATE -> viewModel.navigateTo(ScreenState.HOME)
            ScreenState.GENERATING -> viewModel.navigateTo(ScreenState.CREATE)
            ScreenState.PLAYER -> viewModel.navigateTo(ScreenState.HOME)
            ScreenState.SCRIPT_EDITOR -> {
                if (activeMovie != null) {
                    viewModel.navigateTo(ScreenState.PLAYER)
                } else {
                    viewModel.navigateTo(ScreenState.HOME)
                }
            }
            ScreenState.HOME -> { /* system handles exiting */ }
        }
    }

    when (currentScreen) {
        ScreenState.HOME -> {
            HomeScreen(
                movies = allMovies,
                onCreateMovieClick = { viewModel.navigateTo(ScreenState.CREATE) },
                onMovieClick = { movie -> viewModel.startMoviePlayback(movie) },
                onPresetClick = { preset -> viewModel.applyPreset(preset) },
                onEditScriptClick = { movie -> viewModel.openScriptEditor(movie) },
                onDeleteMovieClick = { movie -> viewModel.deleteMovie(movie) }
            )
        }

        ScreenState.CREATE -> {
            CreateMovieScreen(
                formState = createForm,
                onBackClick = { viewModel.navigateTo(ScreenState.HOME) },
                onPhotoSelected = { uri -> viewModel.onPhotoSelected(uri) },
                onScriptChanged = { script -> viewModel.updateScriptPrompt(script) },
                onGenreSelected = { genre -> viewModel.updateGenre(genre) },
                onDirectorSelected = { director -> viewModel.updateDirector(director) },
                onAspectRatioSelected = { ratio -> viewModel.updateAspectRatio(ratio) },
                onCharacterInfoChanged = { name, role -> viewModel.updateCharacterInfo(name, role) },
                onGenerateClick = { viewModel.generateMovie() }
            )
        }

        ScreenState.GENERATING -> {
            GeneratingMovieScreen(
                state = generationState,
                onBackToStudio = { viewModel.navigateTo(ScreenState.HOME) }
            )
        }

        ScreenState.PLAYER -> {
            activeMovie?.let { movie ->
                MoviePlayerScreen(
                    movie = movie,
                    playerState = playerState,
                    onBackClick = { viewModel.navigateTo(ScreenState.HOME) },
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onNextSceneClick = { viewModel.nextScene() },
                    onPrevSceneClick = { viewModel.previousScene() },
                    onJumpToScene = { idx -> viewModel.jumpToScene(idx) },
                    onToggleSoundtrack = { viewModel.toggleSoundtrack() },
                    onToggleSubtitles = { viewModel.toggleSubtitles() },
                    onOpenScriptEditor = { viewModel.openScriptEditor(movie) },
                    onReplayMovie = { viewModel.startMoviePlayback(movie, 0) }
                )
            } ?: run {
                viewModel.navigateTo(ScreenState.HOME)
            }
        }

        ScreenState.SCRIPT_EDITOR -> {
            activeMovie?.let { movie ->
                ScriptEditorScreen(
                    movie = movie,
                    onBackClick = { viewModel.navigateTo(ScreenState.PLAYER) },
                    onPlaySceneClick = { sceneIdx -> viewModel.startMoviePlayback(movie, sceneIdx) },
                    onDialogueUpdated = { sceneIdx, newDialogue ->
                        viewModel.updateSceneDialogueInEditor(sceneIdx, newDialogue)
                    }
                )
            } ?: run {
                viewModel.navigateTo(ScreenState.HOME)
            }
        }
    }
}
