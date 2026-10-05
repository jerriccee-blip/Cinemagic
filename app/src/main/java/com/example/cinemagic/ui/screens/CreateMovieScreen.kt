package com.example.cinemagic.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cinemagic.domain.AspectRatioMode
import com.example.cinemagic.domain.DirectorStyle
import com.example.cinemagic.domain.MovieGenre
import com.example.cinemagic.ui.viewmodel.CreateFormState
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaObsidian
import com.example.ui.theme.CinemaSurface
import com.example.ui.theme.CinemaSurfaceBorder
import com.example.ui.theme.CinemaSurfaceElevated
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.CinemaTextTertiary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateMovieScreen(
    formState: CreateFormState,
    onBackClick: () -> Unit,
    onPhotoSelected: (Uri) -> Unit,
    onScriptChanged: (String) -> Unit,
    onGenreSelected: (MovieGenre) -> Unit,
    onDirectorSelected: (DirectorStyle) -> Unit,
    onAspectRatioSelected: (AspectRatioMode) -> Unit,
    onCharacterInfoChanged: (String, String) -> Unit,
    onGenerateClick: () -> Unit
) {
    // Compliant Android Photo Picker (zero broad storage permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPhotoSelected(uri)
        }
    }

    val samplePrompts = listOf(
        "A rogue cyber detective confronts an informant in a rainy neon alley over a stolen data drive.",
        "An explorer in a derelict Martian outpost triggers an ancient planetary terraforming beacon.",
        "A private investigator at a foggy dock reveals the secret key to a sunken safe.",
        "An exiled warrior in a ruined castle draws an elemental blade as shadows close in."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Production Studio",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CinemaObsidian
                )
            )
        },
        containerColor = CinemaObsidian,
        bottomBar = {
            Surface(
                color = CinemaSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = onGenerateClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CinemaGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_movie_button")
                    ) {
                        Icon(imageVector = Icons.Default.Movie, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Roll Camera: Generate Movie",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // STEP 1: Cast Photo
            item {
                SectionHeader(stepNumber = "1", title = "Cast Your Lead Actor", subtitle = "Upload your photo. Gemini analyzes facial traits & style.")

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CinemaSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (formState.castBitmap != null) {
                            // Cast portrait preview
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, CinemaGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = formState.castBitmap.asImageBitmap(),
                                    contentDescription = "Cast Photo Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = formState.characterName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CinemaTextPrimary
                                )
                            )

                            Text(
                                text = formState.characterRole,
                                style = MaterialTheme.typography.bodySmall.copy(color = CinemaCyan)
                            )

                            if (formState.analysisMessage != null) {
                                Text(
                                    text = formState.analysisMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CinemaGold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CinemaSurfaceElevated,
                                    contentColor = CinemaTextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder)
                            ) {
                                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Change Photo", fontSize = 12.sp)
                            }
                        } else {
                            // Upload prompt
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(CinemaSurfaceElevated)
                                    .border(2.dp, CinemaSurfaceBorder, CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (formState.isAnalyzingPhoto) {
                                    CircularProgressIndicator(color = CinemaGold, modifier = Modifier.size(36.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Upload Photo",
                                        tint = CinemaGold,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Upload Your Portrait / Selfie",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CinemaTextPrimary
                                )
                            )

                            Text(
                                text = "PNG or JPEG from your gallery. We cast you directly in every scene.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CinemaTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                ),
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )

                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CinemaGold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("upload_photo_button")
                            ) {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Select Photo from Gallery", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // STEP 2: Script Premise
            item {
                SectionHeader(stepNumber = "2", title = "Describe The Scene & Script", subtitle = "A brief idea or opening scene. Gemini will expand it into a 4-scene narrative.")

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = formState.scriptPrompt,
                    onValueChange = onScriptChanged,
                    placeholder = {
                        Text(
                            text = "e.g. A high-stakes heist confrontation where I discover the code on my cybernetic arm is the master key...",
                            color = CinemaTextTertiary,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("script_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CinemaSurface,
                        unfocusedContainerColor = CinemaSurface,
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = CinemaSurfaceBorder,
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                // Quick Inspiration Chips
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quick Scene Ideas:",
                    style = MaterialTheme.typography.labelSmall.copy(color = CinemaTextSecondary)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    samplePrompts.forEach { prompt ->
                        Surface(
                            color = CinemaSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaSurfaceBorder),
                            modifier = Modifier.clickable { onScriptChanged(prompt) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CinemaCyan, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = prompt.take(38) + "...",
                                    style = MaterialTheme.typography.labelSmall.copy(color = CinemaTextSecondary, fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }

            // STEP 3: Genre & Director Aesthetic
            item {
                SectionHeader(stepNumber = "3", title = "Film Genre & Aesthetics", subtitle = "Choose cinematic mood, directorial vision, and screen ratio.")

                Spacer(modifier = Modifier.height(10.dp))

                // Genres
                Text(
                    text = "Movie Genre",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CinemaTextPrimary)
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MovieGenre.values().forEach { genre ->
                        val isSelected = formState.genre == genre
                        Surface(
                            color = if (isSelected) genre.primaryColor.copy(alpha = 0.2f) else CinemaSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) genre.primaryColor else CinemaSurfaceBorder
                            ),
                            modifier = Modifier.clickable { onGenreSelected(genre) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = genre.primaryColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = genre.displayName,
                                    color = if (isSelected) genre.primaryColor else CinemaTextSecondary,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Director Style
                Text(
                    text = "Director's Vision",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CinemaTextPrimary)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DirectorStyle.values().forEach { director ->
                        val isSelected = formState.directorStyle == director
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDirectorSelected(director) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CinemaGold.copy(alpha = 0.12f) else CinemaSurface
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CinemaGold else CinemaSurfaceBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = director.displayName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) CinemaGold else CinemaTextPrimary
                                        )
                                    )
                                    Text(
                                        text = director.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = CinemaTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Aspect Ratio
                Text(
                    text = "Aspect Ratio Format",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = CinemaTextPrimary)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AspectRatioMode.values().forEach { mode ->
                        val isSelected = formState.aspectRatio == mode
                        Surface(
                            color = if (isSelected) CinemaCyan.copy(alpha = 0.15f) else CinemaSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CinemaCyan else CinemaSurfaceBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onAspectRatioSelected(mode) }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CinemaCyan else CinemaTextPrimary
                                    )
                                )
                                Text(
                                    text = if (mode == AspectRatioMode.ANAMORPHIC) "Ultra Cinema" else if (mode == AspectRatioMode.WIDESCREEN) "16:9 Standard" else "Vertical Reel",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = CinemaTextTertiary,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(stepNumber: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            color = CinemaGold,
            shape = CircleShape,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber,
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = CinemaTextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CinemaTextSecondary,
                    fontSize = 11.sp
                )
            )
        }
    }
}
