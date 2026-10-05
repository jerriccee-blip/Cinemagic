package com.example.cinemagic.domain

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class MovieGenre(val displayName: String, val iconName: String, val defaultTone: String, val primaryColor: Color) {
    CYBERPUNK("Cyberpunk", "memory", "Gritty neon-drenched dystopia with synthetic beats", Color(0xFF00E5FF)),
    FILM_NOIR("Film Noir", "shield", "Moody shadows, smoke-filled alleys, and melancholy saxophone", Color(0xFFFFB74D)),
    SCI_FI("Sci-Fi Odyssey", "rocket_launch", "Vast cosmic wonder, alien monoliths, and orchestral strings", Color(0xFF80D8FF)),
    DARK_FANTASY("Dark Fantasy", "auto_fix_high", "Ancient ruins, arcane spells, and thunderous war drums", Color(0xFFEA80FC)),
    ACTION_THRILLER("Action Thriller", "flash_on", "High-octane car chases, ticking clocks, and relentless energy", Color(0xFFFF5252)),
    POST_APOCALYPTIC("Post-Apocalyptic", "terrain", "Wasteland survival, rusted scavengers, and acoustic sorrow", Color(0xFFFFD180))
}

enum class DirectorStyle(val displayName: String, val description: String) {
    NOLAN("Christopher Nolan", "Grand cinematic scope, practical realism, ticking tension"),
    VILLENEUVE("Denis Villeneuve", "Atmospheric minimalism, sweeping vistas, deep resonant soundscapes"),
    TARANTINO("Quentin Tarantino", "Snappy razor-sharp dialogue, dramatic zooms, vibrant color"),
    SCOTT("Ridley Scott", "Dense volumetric haze, industrial noir aesthetic, epic set designs"),
    FINCHER("David Fincher", "Methodical camera tracking, desaturated teal/amber palette, psychological edge")
}

enum class AspectRatioMode(val label: String, val ratio: Float, val description: String) {
    ANAMORPHIC("2.39:1 Cinema", 2.39f, "Ultra-widescreen blockbuster anamorphic"),
    WIDESCREEN("16:9 Standard", 1.777f, "Modern high-definition cinematic standard"),
    REEL("9:16 Vertical", 0.562f, "Cinematic vertical format for modern storytelling")
}

data class ScriptScene(
    val sceneNumber: Int,
    val title: String,
    val location: String, // e.g. "EXT. SHINJUKU ROOFTOP - NIGHT"
    val cameraShot: String, // e.g. "Slow Dolly in, Anamorphic 35mm"
    val actionDescription: String,
    val speaker: String,
    val dialogue: String,
    val soundEffects: String,
    val imagePrompt: String,
    val imagePath: String? = null,
    val durationSeconds: Int = 10
)

data class MovieProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val logline: String,
    val genre: MovieGenre,
    val directorStyle: DirectorStyle,
    val aspectRatio: AspectRatioMode = AspectRatioMode.ANAMORPHIC,
    val castPhotoPath: String? = null,
    val castCharacterName: String = "The Protagonist",
    val castCharacterRole: String = "Lead",
    val promptScript: String,
    val scenes: List<ScriptScene> = emptyList(),
    val soundtrackVibe: String = "Epic orchestral synth atmosphere",
    val createdAt: Long = System.currentTimeMillis()
)

data class PresetTemplate(
    val title: String,
    val genre: MovieGenre,
    val directorStyle: DirectorStyle,
    val characterName: String,
    val characterRole: String,
    val initialScript: String,
    val tag: String
)

val PRESET_TEMPLATES = listOf(
    PresetTemplate(
        title = "Ghost of Sector 9",
        genre = MovieGenre.CYBERPUNK,
        directorStyle = DirectorStyle.VILLENEUVE,
        characterName = "Ren Tanaka",
        characterRole = "Cybernetic Recon Detective",
        initialScript = "A rogue synthetic detective tracks a data broker into a rain-slicked underground noodle bar. An encoded memory chip holds the secret to who wiped the city's power grid.",
        tag = "Cyberpunk Neo-Noir"
    ),
    PresetTemplate(
        title = "The Obsidian Protocol",
        genre = MovieGenre.SCI_FI,
        directorStyle = DirectorStyle.NOLAN,
        characterName = "Commander Sarah Vance",
        characterRole = "Deep-Space Salvage Captain",
        initialScript = "Investigating a ghost vessel drifting near Saturn's rings, the salvage captain discovers the crew vanished 40 years ago—yet the bridge computer is counting down to planetary atmospheric entry.",
        tag = "Cosmic Mystery"
    ),
    PresetTemplate(
        title = "Shadows in the Fog",
        genre = MovieGenre.FILM_NOIR,
        directorStyle = DirectorStyle.SCOTT,
        characterName = "Jack Mercer",
        characterRole = "Disillusioned Private Eye",
        initialScript = "At 2 AM on a rain-drenched harbor dock, a private investigator confronts the only person who knows what was inside the locked safe before the warehouse fire.",
        tag = "Classic Noir"
    ),
    PresetTemplate(
        title = "Crown of Cinders",
        genre = MovieGenre.DARK_FANTASY,
        directorStyle = DirectorStyle.FINCHER,
        characterName = "Lord Kaelen",
        characterRole = "Exiled Spellblade Knight",
        initialScript = "Standing in the ruins of the dragon throne room, an exiled warrior draws a glowing blade as shadowy sentinels materialize from the molten pillars.",
        tag = "Epic Fantasy"
    )
)
