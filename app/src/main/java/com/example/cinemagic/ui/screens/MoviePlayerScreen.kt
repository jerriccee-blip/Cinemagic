package com.example.cinemagic.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cinemagic.domain.MovieProject
import com.example.cinemagic.ui.viewmodel.PlayerState
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceBorder
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.CinemaTextTertiary
import java.io.File

@Composable
fun MoviePlayerScreen(
    movie: MovieProject,
    playerState: PlayerState,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextSceneClick: () -> Unit,
    onPrevSceneClick: () -> Unit,
    onJumpToScene: (Int) -> Unit,
    onToggleSoundtrack: () -> Unit,
    onToggleSubtitles: () -> Unit,
    onOpenScriptEditor: () -> Unit,
    onReplayMovie: () -> Unit
) {
    if (playerState.showCredits) {
        MovieCreditsView(
            movie = movie,
            onReplay = onReplayMovie,
            onOpenScript = onOpenScriptEditor,
            onBackToStudio = onBackClick
        )
        return
    }

    val currentScene = movie.scenes.getOrNull(playerState.currentSceneIndex)
    val totalScenes = movie.scenes.size

    // Smooth Ken Burns pan & zoom animation
    val infiniteTransition = rememberInfiniteTransition(label = "ken_burns")
    val zoomScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zoom"
    )
    val panX by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pan"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Player Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = movie.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold,
                        letterSpacing = 1.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "SCENE ${playerState.currentSceneIndex + 1} OF $totalScenes: ${currentScene?.title?.uppercase() ?: ""}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CinemaTextSecondary,
                        fontSize = 10.sp
                    )
                )
            }

            IconButton(onClick = onOpenScriptEditor) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Screenplay",
                    tint = CinemaCyan
                )
            }
        }

        // CINEMA LETTERBOX SCREEN
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            // Screenplay frame container
            val aspect = if (movie.aspectRatio.ratio > 0.6f) movie.aspectRatio.ratio else 1.77f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, CinemaSurfaceBorder, RoundedCornerShape(4.dp))
            ) {
                // Scene visual with Ken Burns animation
                val imageFile = currentScene?.imagePath?.let { File(it) }
                if (imageFile != null && imageFile.exists()) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = currentScene.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = panX
                            },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback visual canvas
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CinemaObsidian),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(64.dp))
                    }
                }

                // Dramatic Dark Gradient at bottom of frame for Subtitles
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Camera Shot Overlay (Top Left of Frame)
                if (currentScene != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "CAM: ${currentScene.cameraShot}",
                            color = CinemaTextSecondary,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Synchronized Lower-Third Subtitles
                if (playerState.showSubtitles && currentScene != null && currentScene.dialogue.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = CinemaGold.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentScene.speaker.uppercase(),
                                color = CinemaGold,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "\"${currentScene.dialogue}\"",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (currentScene.soundEffects.isNotBlank()) {
                            Text(
                                text = currentScene.soundEffects,
                                color = CinemaCyan,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // BOTTOM CONTROLS & TIMELINE
        Surface(
            color = CinemaSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                // Scene Progress Scrubber
                val progressFraction = if (playerState.sceneDurationMs > 0) {
                    (playerState.sceneTimeElapsedMs.toFloat() / playerState.sceneDurationMs).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = CinemaGold,
                    trackColor = CinemaSurfaceBorder
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Scene Selector Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    movie.scenes.forEachIndexed { idx, sc ->
                        val isCurrent = idx == playerState.currentSceneIndex
                        Surface(
                            color = if (isCurrent) CinemaGold else CinemaSurfaceBorder,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clickable { onJumpToScene(idx) }
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Control Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Soundtrack Drone Toggle
                    IconButton(onClick = onToggleSoundtrack) {
                        Icon(
                            imageVector = if (playerState.isSoundtrackOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Soundtrack",
                            tint = if (playerState.isSoundtrackOn) CinemaCyan else CinemaTextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Prev Scene
                    IconButton(
                        onClick = onPrevSceneClick,
                        modifier = Modifier.testTag("prev_scene_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Scene",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Main Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(CinemaGold, CircleShape)
                            .clickable(onClick = onPlayPauseClick)
                            .testTag("play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Next Scene
                    IconButton(
                        onClick = onNextSceneClick,
                        modifier = Modifier.testTag("next_scene_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Scene",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Subtitle Toggle
                    IconButton(onClick = onToggleSubtitles) {
                        Icon(
                            imageVector = if (playerState.showSubtitles) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                            contentDescription = "Subtitles",
                            tint = if (playerState.showSubtitles) CinemaGold else CinemaTextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieCreditsView(
    movie: MovieProject,
    onReplay: () -> Unit,
    onOpenScript: () -> Unit,
    onBackToStudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = CinemaGold.copy(alpha = 0.2f),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold)
        ) {
            Text(
                text = "THE END",
                color = CinemaGold,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = movie.title.uppercase(),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Credits roll list
        CreditLine(role = "DIRECTED BY", name = movie.directorStyle.displayName)
        CreditLine(role = "STARRING", name = "${movie.castCharacterName} as ${movie.castCharacterRole}")
        CreditLine(role = "SCREENPLAY BY", name = "Gemini AI Director")
        CreditLine(role = "CINEMATOGRAPHY", name = "${movie.genre.displayName} Anamorphic 35mm")
        CreditLine(role = "ORIGINAL SOUNDTRACK", name = movie.soundtrackVibe)

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onReplay,
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Replay Film", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onOpenScript,
                colors = ButtonDefaults.buttonColors(containerColor = CinemaSurfaceElevated, contentColor = CinemaTextPrimary),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder)
            ) {
                Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Screenplay")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBackToStudio,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = CinemaTextSecondary)
        ) {
            Text("Back to Movie Studio")
        }
    }
}

@Composable
private fun CreditLine(role: String, name: String) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = role,
            style = MaterialTheme.typography.labelSmall.copy(
                color = CinemaTextTertiary,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = CinemaTextPrimary,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center
        )
    }
}
