package com.example.sightreading.ui.components

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.sightreading.ui.theme.SpriteType
import com.example.sightreading.ui.theme.WorldTheme

object WorldSpriteBackgrounds {

    /**
     * Maps each World SpriteType to its corresponding drawable resource image.
     * Checks `sunlitredwood.png`, `crystalmeadow.png`, `goldendesertoasis.png`,
     * and `auroracelestialpeaks.png`.
     */
    fun getSpriteDrawableRes(context: Context, spriteType: SpriteType): Int? {
        val namesToTry = when (spriteType) {
            SpriteType.CRYSTAL_MEADOW -> listOf("crystalmeadow", "bg_crystal_meadow")
            SpriteType.SUNLIT_FOREST -> listOf("sunlitredwood", "sunlitredwoodforest", "bg_sunlit_forest")
            SpriteType.DESERT_DUNES -> listOf("goldendesertoasis", "bg_desert_dunes")
            SpriteType.AURORA_PEAKS -> listOf("auroracelestialpeaks", "bg_aurora_peaks")
            SpriteType.ELECTRIC_CITADEL -> listOf("electricskycitadel", "bg_electric_citadel")
            SpriteType.SAPPHIRE_PALACE -> listOf("royalsapphirepalace", "bg_sapphire_palace")
            SpriteType.CORAL_REEF -> listOf("bioluminescentcoralreef", "bg_coral_reef")
            SpriteType.TWILIGHT_HAVEN -> listOf("twilightlavenderhaven", "bg_twilight_haven")
            SpriteType.SUNSET_VALLEY -> listOf("coralsunsetvalley", "bg_sunset_valley")
            SpriteType.COSMIC_NEBULA -> listOf("bg_cosmic_nebula")
            SpriteType.EMERALD_RAINFOREST -> listOf("emerald", "bg_emerald_rainforest")
            SpriteType.SPRING_BLOSSOM -> listOf("springblossomvillage", "bg_spring_blossom")
            SpriteType.MIDNIGHT_NEBULA -> listOf("bg_midnight_nebula")
            SpriteType.OBSIDIAN_CAVERN -> listOf("bg_obsidian_cavern")
            SpriteType.STORMY_CITADEL -> listOf("bg_stormy_citadel")
            SpriteType.FROZEN_GLACIER -> listOf("bg_frozen_glacier")
            SpriteType.CRIMSON_MOON -> listOf("bg_crimson_moon")
            SpriteType.NETHER_TWILIGHT -> listOf("bg_nether_twilight")
            SpriteType.VOID_ABYSS -> listOf("bg_void_abyss")
            SpriteType.DEEP_OCEAN -> listOf("bg_deep_ocean")
            SpriteType.VOLCANIC_INFERNO -> listOf("bg_volcanic_inferno")
            SpriteType.GOTHIC_CATHEDRAL -> listOf("bg_gothic_cathedral")
            SpriteType.HAUNTED_FOREST -> listOf("bg_haunted_forest")
            SpriteType.DRAGON_PEAK -> listOf("bg_dragon_peak")
        }

        for (name in namesToTry) {
            val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (resId != 0) return resId
        }

        return null
    }

    fun drawWorldSpriteBackground(
        drawScope: DrawScope,
        worldTheme: WorldTheme,
        textMeasurer: androidx.compose.ui.text.TextMeasurer
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height

        // 1. Sky & Terrain Gradient Base
        drawScope.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(worldTheme.skyTopColor, worldTheme.skyBottomColor, worldTheme.terrainColor)
            )
        )

        // 2. Specific World Procedural Sprite Artwork
        when (worldTheme.spriteType) {
            SpriteType.CRYSTAL_MEADOW -> drawCrystalMeadow(drawScope, width, height, textMeasurer)
            SpriteType.SUNLIT_FOREST -> drawSunlitForest(drawScope, width, height, textMeasurer)
            SpriteType.DESERT_DUNES -> drawDesertDunes(drawScope, width, height, textMeasurer)
            SpriteType.AURORA_PEAKS -> drawAuroraPeaks(drawScope, width, height, textMeasurer)
            SpriteType.ELECTRIC_CITADEL -> drawElectricCitadel(drawScope, width, height, textMeasurer)
            SpriteType.SAPPHIRE_PALACE -> drawSapphirePalace(drawScope, width, height, textMeasurer)
            SpriteType.CORAL_REEF -> drawCoralReef(drawScope, width, height, textMeasurer)
            SpriteType.TWILIGHT_HAVEN -> drawTwilightHaven(drawScope, width, height, textMeasurer)
            SpriteType.SUNSET_VALLEY -> drawSunsetValley(drawScope, width, height, textMeasurer)
            SpriteType.COSMIC_NEBULA, SpriteType.MIDNIGHT_NEBULA -> drawCosmicNebula(drawScope, width, height, textMeasurer)
            SpriteType.EMERALD_RAINFOREST -> drawRainforest(drawScope, width, height, textMeasurer)
            SpriteType.SPRING_BLOSSOM -> drawSpringBlossom(drawScope, width, height, textMeasurer)
            SpriteType.OBSIDIAN_CAVERN -> drawObsidianCavern(drawScope, width, height, textMeasurer)
            SpriteType.STORMY_CITADEL -> drawStormyCitadel(drawScope, width, height, textMeasurer)
            SpriteType.FROZEN_GLACIER -> drawFrozenGlacier(drawScope, width, height, textMeasurer)
            SpriteType.CRIMSON_MOON -> drawCrimsonMoon(drawScope, width, height, textMeasurer)
            SpriteType.NETHER_TWILIGHT -> drawNetherTwilight(drawScope, width, height, textMeasurer)
            SpriteType.VOID_ABYSS -> drawVoidAbyss(drawScope, width, height, textMeasurer)
            SpriteType.DEEP_OCEAN -> drawDeepOcean(drawScope, width, height, textMeasurer)
            SpriteType.VOLCANIC_INFERNO -> drawVolcanicInferno(drawScope, width, height, textMeasurer)
            SpriteType.GOTHIC_CATHEDRAL -> drawGothicCathedral(drawScope, width, height, textMeasurer)
            SpriteType.HAUNTED_FOREST -> drawHauntedForest(drawScope, width, height, textMeasurer)
            SpriteType.DRAGON_PEAK -> drawDragonPeak(drawScope, width, height, textMeasurer)
        }
    }

    private fun drawCrystalMeadow(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF29B6F6), Color(0xFF80DEEA), Color(0xFFE0F7FA))
            )
        )

        val rainbowColors = listOf(
            Color(0xFFFF5252).copy(alpha = 0.65f),
            Color(0xFFFF9800).copy(alpha = 0.65f),
            Color(0xFFFFEB3B).copy(alpha = 0.65f),
            Color(0xFF4CAF50).copy(alpha = 0.65f),
            Color(0xFF00E5FF).copy(alpha = 0.65f),
            Color(0xFF7C4DFF).copy(alpha = 0.65f)
        )

        val rainbowCenter = Offset(w * 0.90f, h * 0.45f)
        var currentRadius = w * 0.65f
        rainbowColors.forEach { color ->
            ds.drawCircle(
                color = color,
                center = rainbowCenter,
                radius = currentRadius,
                style = Stroke(width = 12f)
            )
            currentRadius -= 12f
        }

        var secRadius = w * 0.85f
        rainbowColors.forEach { color ->
            ds.drawCircle(
                color = color.copy(alpha = 0.35f),
                center = Offset(w * 1.1f, h * 0.50f),
                radius = secRadius,
                style = Stroke(width = 10f)
            )
            secRadius -= 10f
        }

        ds.drawText(tm, "☁️", Offset(w * 0.05f, h * 0.05f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "☁️", Offset(w * 0.78f, h * 0.08f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "✨", Offset(w * 0.50f, h * 0.12f), style = TextStyle(fontSize = 32.sp))
        ds.drawText(tm, "✨", Offset(w * 0.68f, h * 0.06f), style = TextStyle(fontSize = 36.sp))
        ds.drawText(tm, "🌟", Offset(w * 0.30f, h * 0.05f), style = TextStyle(fontSize = 42.sp))

        val hillPath1 = Path().apply {
            moveTo(0f, h * 0.55f)
            quadraticTo(w * 0.35f, h * 0.42f, w * 0.65f, h * 0.52f)
            quadraticTo(w * 0.85f, h * 0.58f, w, h * 0.48f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        ds.drawPath(hillPath1, Color(0xFF81C784))

        val hillPath2 = Path().apply {
            moveTo(0f, h * 0.68f)
            quadraticTo(w * 0.25f, h * 0.58f, w * 0.55f, h * 0.65f)
            quadraticTo(w * 0.80f, h * 0.72f, w, h * 0.62f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        ds.drawPath(hillPath2, Color(0xFF66BB6A))

        val hillPath3 = Path().apply {
            moveTo(0f, h * 0.82f)
            quadraticTo(w * 0.40f, h * 0.72f, w * 0.75f, h * 0.80f)
            lineTo(w, h * 0.74f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        ds.drawPath(hillPath3, Color(0xFF43A047))

        ds.drawText(tm, "🌸", Offset(w * 0.15f, h * 0.75f), style = TextStyle(fontSize = 28.sp))
        ds.drawText(tm, "🌸", Offset(w * 0.45f, h * 0.85f), style = TextStyle(fontSize = 28.sp))
        ds.drawText(tm, "🌸", Offset(w * 0.78f, h * 0.82f), style = TextStyle(fontSize = 28.sp))

        fun drawCrystalSpire(xPx: Float, yPx: Float, wPx: Float, hPx: Float, mainColor: Color, sideColor: Color) {
            val mainSpire = Path().apply {
                moveTo(xPx + wPx / 2f, yPx)
                lineTo(xPx + wPx, yPx + hPx * 0.3f)
                lineTo(xPx + wPx * 0.85f, yPx + hPx)
                lineTo(xPx + wPx * 0.15f, yPx + hPx)
                lineTo(xPx, yPx + hPx * 0.3f)
                close()
            }
            ds.drawPath(mainSpire, mainColor)
            ds.drawPath(mainSpire, Color.White.copy(alpha = 0.4f), style = Stroke(width = 3f))

            val sideShard = Path().apply {
                moveTo(xPx - wPx * 0.3f, yPx + hPx * 0.4f)
                lineTo(xPx + wPx * 0.2f, yPx + hPx * 0.5f)
                lineTo(xPx + wPx * 0.1f, yPx + hPx)
                lineTo(xPx - wPx * 0.4f, yPx + hPx)
                close()
            }
            ds.drawPath(sideShard, sideColor)
            ds.drawPath(sideShard, Color.White.copy(alpha = 0.4f), style = Stroke(width = 2f))
        }

        drawCrystalSpire(w * 0.08f, h * 0.28f, w * 0.14f, h * 0.35f, Color(0xFF00E5FF), Color(0xFFE040FB))
        drawCrystalSpire(w * 0.76f, h * 0.35f, w * 0.12f, h * 0.30f, Color(0xFF00E676), Color(0xFF1DE9B6))
        drawCrystalSpire(w * 0.02f, h * 0.68f, w * 0.12f, h * 0.28f, Color(0xFFFF4081), Color(0xFF7C4DFF))
        drawCrystalSpire(w * 0.85f, h * 0.70f, w * 0.12f, h * 0.26f, Color(0xFF00E5FF), Color(0xFF00E676))
    }

    private fun drawSunlitForest(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "☀️", Offset(w * 0.45f, h * 0.02f), style = TextStyle(fontSize = 52.sp))
        ds.drawText(tm, "🌲", Offset(w * 0.05f, h * 0.20f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "🌲", Offset(w * 0.82f, h * 0.25f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "🌲", Offset(w * 0.15f, h * 0.55f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "🍄", Offset(w * 0.85f, h * 0.78f), style = TextStyle(fontSize = 36.sp))
    }

    private fun drawDesertDunes(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🏜️", Offset(w * 0.70f, h * 0.22f), style = TextStyle(fontSize = 58.sp))
        ds.drawText(tm, "🌴", Offset(w * 0.08f, h * 0.65f), style = TextStyle(fontSize = 42.sp))
        ds.drawText(tm, "🐪", Offset(w * 0.80f, h * 0.75f), style = TextStyle(fontSize = 38.sp))
        ds.drawText(tm, "☀️", Offset(w * 0.12f, h * 0.04f), style = TextStyle(fontSize = 48.sp))
    }

    private fun drawAuroraPeaks(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🌌", Offset(w * 0.35f, h * 0.02f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "🏔️", Offset(w * 0.02f, h * 0.15f), style = TextStyle(fontSize = 62.sp))
        ds.drawText(tm, "🏔️", Offset(w * 0.70f, h * 0.18f), style = TextStyle(fontSize = 62.sp))
        ds.drawText(tm, "❄️", Offset(w * 0.82f, h * 0.65f), style = TextStyle(fontSize = 36.sp))
    }

    private fun drawElectricCitadel(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "⚡", Offset(w * 0.10f, h * 0.08f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "☁️", Offset(w * 0.75f, h * 0.12f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "🏰", Offset(w * 0.40f, h * 0.20f), style = TextStyle(fontSize = 58.sp))
    }

    private fun drawSapphirePalace(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🏛️", Offset(w * 0.08f, h * 0.15f), style = TextStyle(fontSize = 52.sp))
        ds.drawText(tm, "⛲", Offset(w * 0.80f, h * 0.68f), style = TextStyle(fontSize = 42.sp))
        ds.drawText(tm, "💎", Offset(w * 0.82f, h * 0.12f), style = TextStyle(fontSize = 38.sp))
    }

    private fun drawCoralReef(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🪸", Offset(w * 0.08f, h * 0.65f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "🐠", Offset(w * 0.75f, h * 0.25f), style = TextStyle(fontSize = 42.sp))
        ds.drawText(tm, "🫧", Offset(w * 0.20f, h * 0.15f), style = TextStyle(fontSize = 36.sp))
    }

    private fun drawTwilightHaven(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🌙", Offset(w * 0.80f, h * 0.05f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "🌆", Offset(w * 0.08f, h * 0.20f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "🪻", Offset(w * 0.82f, h * 0.70f), style = TextStyle(fontSize = 38.sp))
    }

    private fun drawSunsetValley(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🌅", Offset(w * 0.38f, h * 0.04f), style = TextStyle(fontSize = 58.sp))
        ds.drawText(tm, "🌵", Offset(w * 0.08f, h * 0.68f), style = TextStyle(fontSize = 42.sp))
    }

    private fun drawCosmicNebula(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🪐", Offset(w * 0.10f, h * 0.08f), style = TextStyle(fontSize = 52.sp))
        ds.drawText(tm, "⭐", Offset(w * 0.85f, h * 0.12f), style = TextStyle(fontSize = 36.sp))
        ds.drawText(tm, "✨", Offset(w * 0.48f, h * 0.02f), style = TextStyle(fontSize = 36.sp))
        ds.drawText(tm, "🌟", Offset(w * 0.80f, h * 0.70f), style = TextStyle(fontSize = 36.sp))
    }

    private fun drawRainforest(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🌴", Offset(w * 0.05f, h * 0.18f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "🌴", Offset(w * 0.82f, h * 0.22f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "🦜", Offset(w * 0.15f, h * 0.60f), style = TextStyle(fontSize = 38.sp))
    }

    private fun drawSpringBlossom(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🌸", Offset(w * 0.10f, h * 0.12f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "🌸", Offset(w * 0.80f, h * 0.18f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "🦋", Offset(w * 0.45f, h * 0.08f), style = TextStyle(fontSize = 38.sp))
    }

    private fun drawObsidianCavern(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🦇", Offset(w * 0.15f, h * 0.08f), style = TextStyle(fontSize = 38.sp))
        ds.drawText(tm, "🔮", Offset(w * 0.82f, h * 0.20f), style = TextStyle(fontSize = 42.sp))
        ds.drawText(tm, "💎", Offset(w * 0.08f, h * 0.68f), style = TextStyle(fontSize = 38.sp))
    }

    private fun drawStormyCitadel(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🌩️", Offset(w * 0.35f, h * 0.04f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "🏰", Offset(w * 0.78f, h * 0.18f), style = TextStyle(fontSize = 52.sp))
    }

    private fun drawFrozenGlacier(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🧊", Offset(w * 0.10f, h * 0.15f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "🧊", Offset(w * 0.82f, h * 0.22f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "🐧", Offset(w * 0.85f, h * 0.75f), style = TextStyle(fontSize = 38.sp))
    }

    private fun drawCrimsonMoon(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🔴", Offset(w * 0.80f, h * 0.04f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "⛩️", Offset(w * 0.10f, h * 0.20f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "🏮", Offset(w * 0.85f, h * 0.68f), style = TextStyle(fontSize = 38.sp))
    }

    private fun drawNetherTwilight(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🔮", Offset(w * 0.12f, h * 0.12f), style = TextStyle(fontSize = 42.sp))
        ds.drawText(tm, "✨", Offset(w * 0.80f, h * 0.18f), style = TextStyle(fontSize = 38.sp))
    }

    private fun drawVoidAbyss(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🕳️", Offset(w * 0.40f, h * 0.04f), style = TextStyle(fontSize = 54.sp))
        ds.drawText(tm, "💥", Offset(w * 0.82f, h * 0.20f), style = TextStyle(fontSize = 38.sp))
    }

    private fun drawDeepOcean(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🪼", Offset(w * 0.12f, h * 0.18f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "🦈", Offset(w * 0.75f, h * 0.28f), style = TextStyle(fontSize = 42.sp))
    }

    private fun drawVolcanicInferno(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🌋", Offset(w * 0.35f, h * 0.02f), style = TextStyle(fontSize = 62.sp))
        ds.drawText(tm, "🔥", Offset(w * 0.08f, h * 0.68f), style = TextStyle(fontSize = 42.sp))
    }

    private fun drawGothicCathedral(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "🏰", Offset(w * 0.75f, h * 0.08f), style = TextStyle(fontSize = 58.sp))
        ds.drawText(tm, "🌕", Offset(w * 0.10f, h * 0.05f), style = TextStyle(fontSize = 48.sp))
    }

    private fun drawHauntedForest(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawText(tm, "👻", Offset(w * 0.12f, h * 0.15f), style = TextStyle(fontSize = 48.sp))
        ds.drawText(tm, "🎃", Offset(w * 0.82f, h * 0.70f), style = TextStyle(fontSize = 42.sp))
    }

    private fun drawDragonPeak(ds: DrawScope, w: Float, h: Float, tm: androidx.compose.ui.text.TextMeasurer) {
        ds.drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF20242B), Color(0xFF3B424E), Color(0xFF7A8494))
            )
        )

        val bgPeak1 = Path().apply {
            moveTo(w * 0.05f, h)
            lineTo(w * 0.18f, h * 0.12f)
            lineTo(w * 0.28f, h * 0.35f)
            lineTo(w * 0.35f, h)
            close()
        }
        ds.drawPath(bgPeak1, Color(0xFF3D4452))

        val bgPeak2 = Path().apply {
            moveTo(w * 0.55f, h)
            lineTo(w * 0.62f, h * 0.18f)
            lineTo(w * 0.72f, h * 0.45f)
            lineTo(w * 0.92f, h * 0.15f)
            lineTo(w, h)
            close()
        }
        ds.drawPath(bgPeak2, Color(0xFF383F4C))

        val midPeak1 = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.30f)
            lineTo(w * 0.08f, h * 0.22f)
            lineTo(w * 0.22f, h * 0.08f)
            lineTo(w * 0.28f, h * 0.28f)
            lineTo(w * 0.42f, h)
            close()
        }
        ds.drawPath(midPeak1, Color(0xFF282D37))

        val midPeak2 = Path().apply {
            moveTo(w * 0.48f, h)
            lineTo(w * 0.58f, h * 0.50f)
            lineTo(w * 0.64f, h * 0.25f)
            lineTo(w * 0.75f, h * 0.42f)
            lineTo(w * 0.88f, h)
            close()
        }
        ds.drawPath(midPeak2, Color(0xFF242831))

        val fgPeak = Path().apply {
            moveTo(w * 0.15f, h)
            lineTo(w * 0.28f, h * 0.65f)
            lineTo(w * 0.36f, h * 0.52f)
            lineTo(w * 0.45f, h * 0.72f)
            lineTo(w * 0.55f, h)
            close()
        }
        ds.drawPath(fgPeak, Color(0xFF16181F))

        val fogPath = Path().apply {
            moveTo(0f, h * 0.78f)
            quadraticTo(w * 0.25f, h * 0.72f, w * 0.50f, h * 0.80f)
            quadraticTo(w * 0.75f, h * 0.88f, w, h * 0.75f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        ds.drawPath(fogPath, Color(0xFFB5BDCA).copy(alpha = 0.50f))

        fun drawBanner(xPx: Float, yPx: Float, widthPx: Float, heightPx: Float) {
            ds.drawLine(
                color = Color(0xFF101010),
                start = Offset(xPx - 8f, yPx),
                end = Offset(xPx + widthPx + 8f, yPx),
                strokeWidth = 5f
            )

            val bannerPath = Path().apply {
                moveTo(xPx, yPx)
                lineTo(xPx + widthPx, yPx)
                lineTo(xPx + widthPx, yPx + heightPx)
                lineTo(xPx + widthPx / 2f, yPx + heightPx * 0.82f)
                lineTo(xPx, yPx + heightPx)
                close()
            }
            ds.drawPath(bannerPath, Color(0xFF990000))
            ds.drawPath(bannerPath, Color.Black.copy(alpha = 0.25f), style = Stroke(width = 2f))

            ds.drawText(
                textMeasurer = tm,
                text = "🐉",
                topLeft = Offset(xPx + widthPx * 0.15f, yPx + heightPx * 0.20f),
                style = TextStyle(fontSize = (widthPx * 0.6f).sp)
            )
        }

        drawBanner(w * 0.05f, h * 0.28f, w * 0.12f, h * 0.28f)
        drawBanner(w * 0.26f, h * 0.16f, w * 0.09f, h * 0.22f)
        drawBanner(w * 0.52f, h * 0.38f, w * 0.08f, h * 0.18f)
        drawBanner(w * 0.70f, h * 0.42f, w * 0.08f, h * 0.18f)
        drawBanner(w * 0.82f, h * 0.30f, w * 0.13f, h * 0.30f)
    }
}
