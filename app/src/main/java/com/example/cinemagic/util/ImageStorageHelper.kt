package com.example.cinemagic.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import com.example.cinemagic.domain.MovieGenre
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageHelper {

    suspend fun saveImageFromUri(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
            inputStream?.close()

            saveBitmapToFile(context, bitmap, "cast_")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap, prefix: String = "scene_"): String = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "cinemagic_media").apply { mkdirs() }
        val filename = "${prefix}${UUID.randomUUID()}.jpg"
        val file = File(dir, filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    }

    suspend fun loadBitmap(path: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Synthesizes a high-end cinematic visual frame when generating scenes:
     * Blends the cast portrait into an atmospheric cinematic environment
     * with volumetric lighting, anamorphic lens flares, vignette, and lower-third film slate.
     */
    suspend fun renderCinematicFrame(
        context: Context,
        castBitmap: Bitmap?,
        sceneNumber: Int,
        sceneTitle: String,
        genre: MovieGenre,
        actionSummary: String
    ): String = withContext(Dispatchers.IO) {
        val width = 1280
        val height = 720
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Atmosphere Gradient Background based on Genre
        val (bgTop, bgBottom, accentGlow) = when (genre) {
            MovieGenre.CYBERPUNK -> Triple(Color.rgb(10, 12, 28), Color.rgb(2, 4, 12), Color.rgb(0, 229, 255))
            MovieGenre.FILM_NOIR -> Triple(Color.rgb(25, 20, 18), Color.rgb(5, 4, 4), Color.rgb(255, 183, 77))
            MovieGenre.SCI_FI -> Triple(Color.rgb(12, 24, 40), Color.rgb(2, 6, 15), Color.rgb(128, 216, 255))
            MovieGenre.DARK_FANTASY -> Triple(Color.rgb(24, 10, 30), Color.rgb(8, 2, 10), Color.rgb(234, 128, 252))
            MovieGenre.ACTION_THRILLER -> Triple(Color.rgb(35, 12, 12), Color.rgb(10, 2, 2), Color.rgb(255, 82, 82))
            MovieGenre.POST_APOCALYPTIC -> Triple(Color.rgb(30, 22, 14), Color.rgb(10, 7, 4), Color.rgb(255, 209, 128))
        }

        val bgShader = LinearGradient(0f, 0f, 0f, height.toFloat(), bgTop, bgBottom, Shader.TileMode.CLAMP)
        paint.shader = bgShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // 2. Cinematic Volumetric Light Beam / Radial Glow
        val glowShader = RadialGradient(
            width * 0.7f, height * 0.35f,
            width * 0.55f,
            Color.argb(90, Color.red(accentGlow), Color.green(accentGlow), Color.blue(accentGlow)),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        paint.shader = glowShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // 3. Draw Cast Member Photo if available
        if (castBitmap != null) {
            val castDestWidth = (width * 0.42f).toInt()
            val castDestHeight = (height * 0.85f).toInt()
            val castLeft = (width * 0.52f).toInt()
            val castTop = (height * 0.12f).toInt()

            val srcRect = Rect(0, 0, castBitmap.width, castBitmap.height)
            val dstRect = Rect(castLeft, castTop, castLeft + castDestWidth, castTop + castDestHeight)

            paint.alpha = 230
            canvas.drawBitmap(castBitmap, srcRect, dstRect, paint)
            paint.alpha = 255

            // Feathering shadow gradient over cast edges for seamless cinematic blend
            val edgeGradient = LinearGradient(
                castLeft.toFloat(), 0f, (castLeft + castDestWidth * 0.3f), 0f,
                bgBottom, Color.TRANSPARENT, Shader.TileMode.CLAMP
            )
            paint.shader = edgeGradient
            canvas.drawRect(dstRect, paint)
            paint.shader = null

            val bottomGradient = LinearGradient(
                0f, (castTop + castDestHeight * 0.6f), 0f, (castTop + castDestHeight).toFloat(),
                Color.TRANSPARENT, bgBottom, Shader.TileMode.CLAMP
            )
            paint.shader = bottomGradient
            canvas.drawRect(dstRect, paint)
            paint.shader = null
        }

        // 4. Anamorphic Blue Flare / Horizontal Light Streak
        val streakY = height * 0.4f
        val streakPaint = Paint().apply {
            color = Color.argb(120, 100, 200, 255)
            strokeWidth = 3f
        }
        canvas.drawLine(0f, streakY, width.toFloat(), streakY, streakPaint)

        val streakGlow = LinearGradient(
            width * 0.2f, streakY, width * 0.8f, streakY,
            Color.TRANSPARENT, Color.argb(140, Color.red(accentGlow), Color.green(accentGlow), Color.blue(accentGlow)), Shader.TileMode.MIRROR
        )
        streakPaint.shader = streakGlow
        streakPaint.strokeWidth = 14f
        canvas.drawLine(width * 0.1f, streakY, width * 0.9f, streakY, streakPaint)

        // 5. Cinematic Vignette
        val vignette = RadialGradient(
            width * 0.5f, height * 0.5f,
            width * 0.65f,
            Color.TRANSPARENT,
            Color.argb(200, 0, 0, 0),
            Shader.TileMode.CLAMP
        )
        paint.shader = vignette
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // 6. Lower Third Cinematic Film Slate
        val slatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            setShadowLayer(8f, 2f, 2f, Color.BLACK)
        }

        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentGlow
            textSize = 20f
            isFakeBoldText = true
            letterSpacing = 0.15f
        }

        canvas.drawText("SCENE $sceneNumber  •  ${genre.displayName.uppercase()}", 48f, height - 76f, tagPaint)
        canvas.drawText(sceneTitle.uppercase(), 48f, height - 40f, slatePaint)

        // 7. Save file and return path
        saveBitmapToFile(context, bitmap, "scene_frame_")
    }

    /**
     * Creates a sample actor portrait bitmap if user chooses a preset actor
     */
    fun createSampleActorBitmap(actorName: String, genre: MovieGenre): Bitmap {
        val width = 480
        val height = 640
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        val topColor = when (genre) {
            MovieGenre.CYBERPUNK -> Color.rgb(20, 24, 45)
            MovieGenre.FILM_NOIR -> Color.rgb(35, 30, 28)
            MovieGenre.SCI_FI -> Color.rgb(15, 35, 55)
            MovieGenre.DARK_FANTASY -> Color.rgb(40, 20, 50)
            MovieGenre.ACTION_THRILLER -> Color.rgb(45, 18, 18)
            MovieGenre.POST_APOCALYPTIC -> Color.rgb(40, 32, 20)
        }
        val bgGrad = LinearGradient(0f, 0f, 0f, height.toFloat(), topColor, Color.BLACK, Shader.TileMode.CLAMP)
        paint.shader = bgGrad
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // Silhouette / Headshot shape with dramatic cinematic rim lighting
        val headRadius = width * 0.22f
        val headCenterX = width * 0.5f
        val headCenterY = height * 0.38f

        // Shoulders
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 32, 40)
        }
        val bodyRect = RectF(width * 0.15f, headCenterY + headRadius * 0.7f, width * 0.85f, height.toFloat())
        canvas.drawRoundRect(bodyRect, 80f, 80f, bodyPaint)

        // Head
        val skinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(195, 160, 135)
        }
        canvas.drawCircle(headCenterX, headCenterY, headRadius, skinPaint)

        // Hair / Helmet / Hood
        val hairPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(25, 25, 30)
        }
        val hairRect = RectF(headCenterX - headRadius, headCenterY - headRadius * 1.05f, headCenterX + headRadius, headCenterY - headRadius * 0.2f)
        canvas.drawRoundRect(hairRect, headRadius, headRadius, hairPaint)

        // Eyes with focused gaze
        val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(40, 30, 25) }
        canvas.drawCircle(headCenterX - headRadius * 0.35f, headCenterY - 10f, 10f, eyePaint)
        canvas.drawCircle(headCenterX + headRadius * 0.35f, headCenterY - 10f, 10f, eyePaint)

        // Dramatic rim light overlay
        val rimColor = when (genre) {
            MovieGenre.CYBERPUNK -> Color.argb(180, 0, 229, 255)
            MovieGenre.FILM_NOIR -> Color.argb(180, 255, 190, 80)
            MovieGenre.SCI_FI -> Color.argb(180, 120, 215, 255)
            MovieGenre.DARK_FANTASY -> Color.argb(180, 230, 130, 250)
            MovieGenre.ACTION_THRILLER -> Color.argb(180, 255, 80, 80)
            MovieGenre.POST_APOCALYPTIC -> Color.argb(180, 255, 210, 120)
        }
        val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = rimColor
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawCircle(headCenterX, headCenterY, headRadius, rimPaint)

        return bmp
    }
}
