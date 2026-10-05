package com.example.cinemagic.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinemagic.ui.viewmodel.GenerationState
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaMagenta
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceBorder
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.CinemaTextTertiary

@Composable
fun GeneratingMovieScreen(
    state: GenerationState,
    onBackToStudio: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reel_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CinemaObsidian)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.error != null) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(60.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Production Interrupted",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = state.error,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = CinemaTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Button(
                onClick = onBackToStudio,
                colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black)
            ) {
                Text("Return to Studio")
            }
        } else {
            // Animated Glowing Film Reel
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(CinemaGold.copy(alpha = 0.25f), Color.Transparent)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "Film Reel",
                    tint = CinemaGold,
                    modifier = Modifier
                        .size(56.dp)
                        .rotate(angle)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = CinemaCyan.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaCyan.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "AI DIRECTOR AT WORK",
                    color = CinemaCyan,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.currentStage,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CinemaTextPrimary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .testTag("generation_progress"),
                color = CinemaGold,
                trackColor = CinemaSurfaceBorder,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Studio Production Steps Monitor
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "LIVE PRODUCTION PIPELINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = CinemaTextTertiary,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ProductionStepRow(
                        title = "1. Screenplay & Story Arc",
                        subtitle = "Gemini Screenwriter & Narrative Engine",
                        isDone = state.progress > 0.35f,
                        isActive = state.progress <= 0.35f
                    )

                    ProductionStepRow(
                        title = "2. Casting & Character Persona",
                        subtitle = "Facial blocking & aesthetic styling",
                        isDone = state.progress > 0.50f,
                        isActive = state.progress in 0.35f..0.50f
                    )

                    ProductionStepRow(
                        title = "3. Cinematography & Keyframes",
                        subtitle = "4-Scene Anamorphic Shot Generation",
                        isDone = state.progress > 0.85f,
                        isActive = state.progress in 0.50f..0.85f
                    )

                    ProductionStepRow(
                        title = "4. Audio Mastering & Sound Mixing",
                        subtitle = "Subtitles, Foley SFX & Ambient Score",
                        isDone = state.progress >= 0.95f,
                        isActive = state.progress in 0.85f..0.95f
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Streaming Console Logs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    reverseLayout = true
                ) {
                    items(state.logMessages.reversed()) { log ->
                        Text(
                            text = "> $log",
                            color = CinemaTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductionStepRow(
    title: String,
    subtitle: String,
    isDone: Boolean,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (iconColor, bgColor) = when {
            isDone -> Pair(CinemaCyan, CinemaCyan.copy(alpha = 0.15f))
            isActive -> Pair(CinemaGold, CinemaGold.copy(alpha = 0.15f))
            else -> Pair(CinemaTextTertiary, CinemaSurfaceElevated)
        }

        Surface(
            color = bgColor,
            shape = CircleShape,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isDone) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(iconColor, CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) CinemaGold else if (isDone) CinemaTextPrimary else CinemaTextTertiary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CinemaTextTertiary,
                    fontSize = 10.sp
                )
            )
        }
    }
}
