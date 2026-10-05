package com.example.cinemagic.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinemagic.domain.MovieProject
import com.example.cinemagic.domain.ScriptScene
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceBorder
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.CinemaTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptEditorScreen(
    movie: MovieProject,
    onBackClick: () -> Unit,
    onPlaySceneClick: (Int) -> Unit,
    onDialogueUpdated: (Int, String) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Screenplay & Shot List",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CinemaTextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CinemaTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val fullScript = buildString {
                            appendLine("${movie.title.uppercase()}")
                            appendLine("LOGLINE: ${movie.logline}")
                            appendLine("DIRECTED IN STYLE OF: ${movie.directorStyle.displayName}")
                            appendLine("SOUNDTRACK: ${movie.soundtrackVibe}")
                            appendLine("==========================================")
                            movie.scenes.forEach { sc ->
                                appendLine("\nSCENE ${sc.sceneNumber}: ${sc.title.uppercase()}")
                                appendLine(sc.location)
                                appendLine("[CAMERA: ${sc.cameraShot}]")
                                appendLine(sc.actionDescription)
                                appendLine("\n${sc.speaker.uppercase()}")
                                appendLine("\"${sc.dialogue}\"")
                                appendLine(sc.soundEffects)
                                appendLine("------------------------------------------")
                            }
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Screenplay", fullScript))
                        Toast.makeText(context, "Screenplay copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Screenplay", tint = CinemaGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CinemaObsidian)
            )
        },
        containerColor = CinemaObsidian
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screenplay Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = movie.genre.primaryColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = movie.genre.displayName.uppercase(),
                                    color = movie.genre.primaryColor,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "Dir: ${movie.directorStyle.displayName}",
                                style = MaterialTheme.typography.labelSmall.copy(color = CinemaTextSecondary)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = CinemaGold
                            )
                        )

                        Text(
                            text = movie.logline,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CinemaTextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )

                        Text(
                            text = "Score: ${movie.soundtrackVibe}",
                            style = MaterialTheme.typography.labelSmall.copy(color = CinemaCyan)
                        )
                    }
                }
            }

            // Scenes Screenplay Breakdown
            itemsIndexed(movie.scenes) { index, scene ->
                SceneScriptCard(
                    scene = scene,
                    onPlayClick = { onPlaySceneClick(index) },
                    onSaveDialogue = { newDialogue -> onDialogueUpdated(index, newDialogue) }
                )
            }
        }
    }
}

@Composable
private fun SceneScriptCard(
    scene: ScriptScene,
    onPlayClick: () -> Unit,
    onSaveDialogue: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedDialogue by remember(scene.dialogue) { mutableStateOf(scene.dialogue) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scene_script_card_${scene.sceneNumber}"),
        colors = CardDefaults.cardColors(containerColor = CinemaSurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Slugline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCENE ${scene.sceneNumber}: ${scene.location}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold,
                        fontSize = 12.sp
                    )
                )

                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Scene",
                        tint = CinemaCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Camera Direction
            Surface(
                color = CinemaObsidian,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = "CAM: ${scene.cameraShot}",
                    color = CinemaTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Action
            Text(
                text = scene.actionDescription,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CinemaTextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Character & Dialogue Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = scene.speaker.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CinemaGold,
                                letterSpacing = 1.sp
                            )
                        )

                        IconButton(
                            onClick = {
                                if (isEditing) {
                                    onSaveDialogue(editedDialogue)
                                    isEditing = false
                                } else {
                                    isEditing = true
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Dialogue",
                                tint = if (isEditing) CinemaGold else CinemaTextTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (isEditing) {
                        OutlinedTextField(
                            value = editedDialogue,
                            onValueChange = { editedDialogue = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CinemaObsidian,
                                unfocusedContainerColor = CinemaObsidian,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CinemaGold
                            )
                        )

                        Button(
                            onClick = {
                                onSaveDialogue(editedDialogue)
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold, contentColor = Color.Black),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Save Line", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "\"${scene.dialogue}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }

            // Foley SFX
            if (scene.soundEffects.isNotBlank()) {
                Text(
                    text = scene.soundEffects,
                    color = CinemaCyan,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
