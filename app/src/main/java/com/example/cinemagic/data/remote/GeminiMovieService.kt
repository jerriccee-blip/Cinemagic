package com.example.cinemagic.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.cinemagic.domain.DirectorStyle
import com.example.cinemagic.domain.MovieGenre
import com.example.cinemagic.domain.ScriptScene
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class CharacterProfile(
    val characterName: String,
    val characterRole: String,
    val visualDescription: String,
    val suggestedGenre: MovieGenre
)

data class GeneratedMovieData(
    val title: String,
    val logline: String,
    val soundtrackVibe: String,
    val scenes: List<ScriptScene>
)

class GeminiMovieService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun analyzeCastPhoto(bitmap: Bitmap): CharacterProfile = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackCharacterProfile()
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            val prompt = """
                You are a Hollywood casting director and cinematographer. Analyze this actor portrait.
                Return ONLY valid JSON in this exact structure without markdown backticks:
                {
                  "characterName": "Full Name",
                  "characterRole": "Role title (e.g. Cyberpunk Hacker, Hardboiled Detective, Astral Pilot)",
                  "visualDescription": "Key facial features, eyes, expression, hair and lighting aesthetic",
                  "suggestedGenre": "CYBERPUNK"
                }
                Allowed suggestedGenre values: CYBERPUNK, FILM_NOIR, SCI_FI, DARK_FANTASY, ACTION_THRILLER, POST_APOCALYPTIC.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.7)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w("GeminiService", "Vision call failed: ${response.code} $responseBody")
                return@withContext fallbackCharacterProfile()
            }

            parseCharacterProfile(responseBody)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error analyzing photo", e)
            fallbackCharacterProfile()
        }
    }

    suspend fun generateMovie(
        briefScript: String,
        genre: MovieGenre,
        director: DirectorStyle,
        characterName: String,
        characterRole: String,
        visualDesc: String
    ): GeneratedMovieData = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackMovieData(briefScript, genre, director, characterName, characterRole)
        }

        try {
            val systemPrompt = """
                You are a world-class Hollywood screenwriter, executive producer, and visionary director in the style of ${director.displayName}.
                You are creating an unforgettable, high-drama, 4-scene cinematic short movie.
                Starring Lead Actor/Character: "$characterName" ($characterRole).
                Actor Visual Aesthetic: "$visualDesc".
                Film Genre: ${genre.displayName}.
                Director Aesthetic: ${director.description}.
                User Brief/Premise: "$briefScript".

                Generate a complete 4-scene narrative arc:
                - Scene 1: The Inciting Hook & World Setup.
                - Scene 2: The Rising Tension / Critical Clue or Discovery.
                - Scene 3: The Climax / High-Stakes Confrontation.
                - Scene 4: The Aftermath / Dramatic Revelation or Twist.

                Output ONLY valid JSON (no markdown backticks, no extra text) with this exact schema:
                {
                  "title": "Compelling Blockbuster Movie Title",
                  "logline": "Gripping one-sentence hook summary of the movie",
                  "soundtrackVibe": "Detailed score description (e.g. Low modular synthesizer drone, pulsating heartbeat tempo, and mournful cello)",
                  "scenes": [
                    {
                      "sceneNumber": 1,
                      "title": "Scene Name",
                      "location": "EXT. SLUG-LINE LOCATION - TIME",
                      "cameraShot": "Specific camera movement and lens (e.g. Slow Low-Angle Dolly In, 35mm anamorphic with blue flares)",
                      "actionDescription": "Vivid cinematic screenplay action describing $characterName in the environment",
                      "speaker": "$characterName",
                      "dialogue": "A striking, impactful line of dialogue spoken by $characterName",
                      "soundEffects": "[Heavy rain splashing on metal grating, far-off sirens pulsing]",
                      "imagePrompt": "Masterpiece photorealistic cinematic film still, starring $characterName ($visualDesc), $characterRole, in the scene location, cinematic lighting, volumetric atmosphere, 8k resolution, directed by ${director.displayName}",
                      "durationSeconds": 10
                    }
                  ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", systemPrompt) })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.8)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w("GeminiService", "Script generation failed: ${response.code} $responseBody")
                return@withContext fallbackMovieData(briefScript, genre, director, characterName, characterRole)
            }

            parseMovieResponse(responseBody, briefScript, genre, director, characterName, characterRole)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error generating movie", e)
            fallbackMovieData(briefScript, genre, director, characterName, characterRole)
        }
    }

    suspend fun generateSceneImage(
        prompt: String,
        aspectRatioStr: String = "16:9"
    ): Bitmap? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        try {
            val ratio = if (aspectRatioStr.contains("9:16")) "9:16" else "16:9"
            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", ratio)
                    })
                    put("responseModalities", JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    })
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w("GeminiService", "Image generation failed: ${response.code}")
                return@withContext null
            }

            val root = JSONObject(body)
            val candidates = root.optJSONArray("candidates") ?: return@withContext null
            val content = candidates.optJSONObject(0)?.optJSONObject("content") ?: return@withContext null
            val parts = content.optJSONArray("parts") ?: return@withContext null

            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                val inlineData = part.optJSONObject("inlineData")
                if (inlineData != null) {
                    val base64Data = inlineData.optString("data", "")
                    if (base64Data.isNotEmpty()) {
                        val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                        return@withContext BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e("GeminiService", "Error generating image", e)
            null
        }
    }

    private fun parseCharacterProfile(responseBody: String): CharacterProfile {
        val root = JSONObject(responseBody)
        val text = extractCandidateText(root)
        val cleanJson = cleanJsonString(text)
        val obj = JSONObject(cleanJson)

        val name = obj.optString("characterName", "The Protagonist")
        val role = obj.optString("characterRole", "Lead Actor")
        val desc = obj.optString("visualDescription", "Charismatic protagonist with focused gaze and cinematic presence.")
        val genreStr = obj.optString("suggestedGenre", "CYBERPUNK")
        val genre = try { MovieGenre.valueOf(genreStr) } catch (e: Exception) { MovieGenre.CYBERPUNK }

        return CharacterProfile(name, role, desc, genre)
    }

    private fun parseMovieResponse(
        responseBody: String,
        briefScript: String,
        genre: MovieGenre,
        director: DirectorStyle,
        characterName: String,
        characterRole: String
    ): GeneratedMovieData {
        val root = JSONObject(responseBody)
        val text = extractCandidateText(root)
        val cleanJson = cleanJsonString(text)
        val obj = JSONObject(cleanJson)

        val title = obj.optString("title", "Project ${genre.displayName}")
        val logline = obj.optString("logline", briefScript)
        val soundtrack = obj.optString("soundtrackVibe", "Dramatic orchestral score with ambient synth pulses")
        val scenesArray = obj.optJSONArray("scenes") ?: JSONArray()

        val scenes = mutableListOf<ScriptScene>()
        for (i in 0 until scenesArray.length()) {
            val sObj = scenesArray.optJSONObject(i) ?: continue
            val sceneNumber = sObj.optInt("sceneNumber", i + 1)
            val sceneTitle = sObj.optString("title", "Scene $sceneNumber")
            val location = sObj.optString("location", "INT. LOCATION - NIGHT")
            val camera = sObj.optString("cameraShot", "Medium close-up, anamorphic lens")
            val action = sObj.optString("actionDescription", "Dramatic scene unfolds.")
            val speaker = sObj.optString("speaker", characterName)
            val dialogue = sObj.optString("dialogue", "We don't have much time.")
            val sfx = sObj.optString("soundEffects", "[Low atmospheric rumble]")
            val imgPrompt = sObj.optString("imagePrompt", "Cinematic still of $characterName in $genre movie")
            val dur = sObj.optInt("durationSeconds", 10)

            scenes.add(
                ScriptScene(
                    sceneNumber = sceneNumber,
                    title = sceneTitle,
                    location = location,
                    cameraShot = camera,
                    actionDescription = action,
                    speaker = speaker,
                    dialogue = dialogue,
                    soundEffects = sfx,
                    imagePrompt = imgPrompt,
                    durationSeconds = dur
                )
            )
        }

        if (scenes.isEmpty()) {
            return fallbackMovieData(briefScript, genre, director, characterName, characterRole)
        }

        return GeneratedMovieData(title, logline, soundtrack, scenes)
    }

    private fun extractCandidateText(root: JSONObject): String {
        val candidates = root.optJSONArray("candidates") ?: return ""
        val content = candidates.optJSONObject(0)?.optJSONObject("content") ?: return ""
        val parts = content.optJSONArray("parts") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until parts.length()) {
            val part = parts.optJSONObject(i) ?: continue
            sb.append(part.optString("text", ""))
        }
        return sb.toString().trim()
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    private fun fallbackCharacterProfile(): CharacterProfile {
        return CharacterProfile(
            characterName = "Alex Cruz",
            characterRole = "Undercover Operative",
            visualDescription = "Sharp eyes, determined jawline, dark coat with high collar, moody atmospheric lighting",
            suggestedGenre = MovieGenre.CYBERPUNK
        )
    }

    private fun fallbackMovieData(
        prompt: String,
        genre: MovieGenre,
        director: DirectorStyle,
        characterName: String,
        characterRole: String
    ): GeneratedMovieData {
        val title = when (genre) {
            MovieGenre.CYBERPUNK -> "Neon Echoes"
            MovieGenre.FILM_NOIR -> "Midnight Rain"
            MovieGenre.SCI_FI -> "Event Horizon Protocol"
            MovieGenre.DARK_FANTASY -> "The Cinder Throne"
            MovieGenre.ACTION_THRILLER -> "Deadly Velocity"
            MovieGenre.POST_APOCALYPTIC -> "Dust & Ash"
        }

        val scenes = listOf(
            ScriptScene(
                sceneNumber = 1,
                title = "The Breach",
                location = "EXT. RAIN-SWEPT LOWER SECTOR - NIGHT",
                cameraShot = "Wide Crane Shot descending into a neon alleyway, 35mm anamorphic",
                actionDescription = "$characterName stands under flickering holographic ads, water dripping from their coat as they scan the deserted street.",
                speaker = characterName,
                dialogue = "The signal originated here. Someone wanted me to find this.",
                soundEffects = "[Heavy rainfall, distant drone engine hum, siren wail]",
                imagePrompt = "Cinematic frame of $characterName ($characterRole) standing in a neon-lit rain-soaked street, anamorphic lens flare, blue and amber tones, photorealistic film still",
                durationSeconds = 9
            ),
            ScriptScene(
                sceneNumber = 2,
                title = "The Encrypted Vault",
                location = "INT. ABANDONED RELAY STATION - CONTINUOUS",
                cameraShot = "Steadicam Tracking Shot following behind $characterName into the dark chamber",
                actionDescription = "$characterName pushes open the heavy reinforced door. In the center of the dust-mote air, an active terminal pulses with crimson glyphs.",
                speaker = characterName,
                dialogue = "Forty years dormant... and it just woke up on its own.",
                soundEffects = "[Hydraulic door hiss, electrical hum, high-frequency data chime]",
                imagePrompt = "Cinematic interior film still of $characterName in an abandoned futuristic station, glowing red terminal light on face, cinematic dust motes, directed by ${director.displayName}",
                durationSeconds = 10
            ),
            ScriptScene(
                sceneNumber = 3,
                title = "The Confrontation",
                location = "INT. RELAY STATION OBSERVATION DECK - MOMENTS LATER",
                cameraShot = "Extreme Low-Angle Two-Shot, fast whip pan as shadows shift",
                actionDescription = "A silhouette steps into the neon rim-light. $characterName draws their weapon without flinching, eyes locked on the intruder.",
                speaker = characterName,
                dialogue = "I knew you were listening. Tell me who gave the order.",
                soundEffects = "[Metallic click of a weapon chambering, thunder crack outside]",
                imagePrompt = "High tension cinematic showdown between $characterName and an encroaching shadow, stark contrast lighting, rain streaking down panoramic window glass",
                durationSeconds = 11
            ),
            ScriptScene(
                sceneNumber = 4,
                title = "The Revelation",
                location = "EXT. ROOFTOP SPRAY - DAWN",
                cameraShot = "Slow Dolly Out from $characterName's face to reveal the sprawling mega-city horizon",
                actionDescription = "First light breaks through the toxic fog. $characterName looks down at a glowing memory sphere in their palm, realizing the scale of the conspiracy.",
                speaker = characterName,
                dialogue = "This isn't the end of their plan. It's only day one.",
                soundEffects = "[Wind gust, ambient synth pad swell, rising brass chord]",
                imagePrompt = "Epic dawn cinematic shot of $characterName overlooking a breathtaking futuristic skyline at sunrise, golden lens flares, cinematic composition",
                durationSeconds = 12
            )
        )

        return GeneratedMovieData(
            title = title,
            logline = if (prompt.isNotBlank()) prompt else "An operative uncovers a conspiracy that threatens to tear the city apart.",
            soundtrackVibe = "Dark analog synthwave with orchestral strings and heavy bass drones",
            scenes = scenes
        )
    }
}
