package com.example.sightreading.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sightreading.model.KeyProficiency
import com.example.sightreading.model.KeySignature
import com.example.sightreading.ui.theme.WorldThemeRepository

data class WorldIslandPosition(
    val keySignature: KeySignature,
    val worldName: String,
    val xPx: Float,
    val yPx: Float,
    val proficiency: KeyProficiency,
    val iconSymbol: String
)

@Composable
fun MasterOverworldCanvas(
    keysList: List<KeySignature>,
    proficiencies: Map<KeySignature, KeyProficiency>,
    onSelectKey: (KeySignature) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1180.dp) // Generous height for 12 world islands
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(keysList, proficiencies) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val actualIslands = calculateIslandPositions(keysList, proficiencies, width, height)

                        val hitRadiusPx = 64.dp.toPx() // Generous touch hit radius
                        val hitIsland = actualIslands.find { island ->
                            val dx = tapOffset.x - island.xPx
                            val dy = tapOffset.y - island.yPx
                            (dx * dx + dy * dy) <= (hitRadiusPx * hitRadiusPx)
                        }
                        if (hitIsland != null) {
                            onSelectKey(hitIsland.keySignature)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Ocean Overworld Water Gradient Background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0077B6), Color(0xFF023E8A), Color(0xFF03045E))
                )
            )

            // Draw Decorative Sea Elements
            drawText(textMeasurer, "🌊", Offset(width * 0.08f, height * 0.12f), style = TextStyle(fontSize = 32.sp))
            drawText(textMeasurer, "⛵", Offset(width * 0.82f, height * 0.25f), style = TextStyle(fontSize = 42.sp))
            drawText(textMeasurer, "🧭", Offset(width * 0.80f, height * 0.05f), style = TextStyle(fontSize = 48.sp))
            drawText(textMeasurer, "🌊", Offset(width * 0.85f, height * 0.70f), style = TextStyle(fontSize = 32.sp))

            val actualIslands = calculateIslandPositions(keysList, proficiencies, width, height)

            // 2. Draw Connected Sea Trade Paths
            if (actualIslands.size >= 2) {
                val path = Path().apply {
                    moveTo(actualIslands[0].xPx, actualIslands[0].yPx)
                    for (i in 1 until actualIslands.size) {
                        val prev = actualIslands[i - 1]
                        val curr = actualIslands[i]
                        val midX = (prev.xPx + curr.xPx) / 2f
                        cubicTo(midX, prev.yPx, midX, curr.yPx, curr.xPx, curr.yPx)
                    }
                }

                // Golden Sea Route Path
                drawPath(
                    path = path,
                    color = Color(0xFFFFD54F),
                    style = Stroke(width = 16.dp.toPx())
                )

                drawPath(
                    path = path,
                    color = Color(0xFF1565C0),
                    style = Stroke(width = 6.dp.toPx())
                )
            }

            // 3. Draw World Islands & Landmarks
            actualIslands.forEach { island ->
                drawWorldIslandSprite(
                    island = island,
                    textMeasurer = textMeasurer
                )
            }
        }
    }
}

private fun calculateIslandPositions(
    keysList: List<KeySignature>,
    proficiencies: Map<KeySignature, KeyProficiency>,
    width: Float,
    height: Float
): List<WorldIslandPosition> {
    if (keysList.isEmpty()) return emptyList()

    val paddingX = width * 0.18f
    val availableWidth = width - (paddingX * 2f)

    val topMargin = height * 0.08f
    val bottomMargin = height * 0.08f
    val yStart = height - bottomMargin
    val yStep = (height - topMargin - bottomMargin) / maxOf(1, keysList.size - 1)

    return keysList.mapIndexed { idx, keySig ->
        val levelNum = idx + 1
        val xProgress = when (levelNum % 6) {
            1 -> 0.10f
            2 -> 0.35f
            3 -> 0.70f
            4 -> 0.90f
            5 -> 0.65f
            0 -> 0.25f
            else -> 0.5f
        }

        val x = paddingX + (availableWidth * xProgress)
        val y = yStart - (idx * yStep)

        val prof = proficiencies[keySig] ?: KeyProficiency(keySignature = keySig)
        val worldTheme = WorldThemeRepository.getThemeForKey(keySig)

        val iconSymbol = when {
            keySig.name.contains("CRYSTAL") -> "💎"
            keySig.name.contains("FOREST") || keySig.name.contains("RAINFOREST") -> "🌲"
            keySig.name.contains("DESERT") -> "🏜️"
            keySig.name.contains("AURORA") || keySig.name.contains("PEAKS") -> "🏔️"
            keySig.name.contains("VOLCANIC") -> "🌋"
            keySig.name.contains("CATHEDRAL") || keySig.name.contains("PALACE") -> "🏰"
            keySig.name.contains("DRAGON") -> "🐉"
            else -> "🏝️"
        }

        WorldIslandPosition(
            keySignature = keySig,
            worldName = worldTheme.worldName,
            xPx = x,
            yPx = y,
            proficiency = prof,
            iconSymbol = iconSymbol
        )
    }
}

private fun DrawScope.drawWorldIslandSprite(
    island: WorldIslandPosition,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val radius = 34.dp.toPx()

    // 1. Island Outer Base Circle
    drawCircle(
        color = Color(0xFF4CAF50), // Green Island Terrain
        center = Offset(island.xPx, island.yPx),
        radius = radius
    )

    drawCircle(
        color = Color(0xFFFFD54F), // Gold Coastline Border
        center = Offset(island.xPx, island.yPx),
        radius = radius,
        style = Stroke(width = 4.dp.toPx())
    )

    // 2. Island Icon Symbol
    drawText(
        textMeasurer = textMeasurer,
        text = island.iconSymbol,
        topLeft = Offset(island.xPx - 14.dp.toPx(), island.yPx - 16.dp.toPx()),
        style = TextStyle(fontSize = 22.sp)
    )

    // 3. World Name & Key Tag
    val tagText = island.keySignature.displayName
    val subTagText = "${island.proficiency.completedLessonsCount}/25 Levels • ${island.proficiency.proficiencyPercentage}%"

    val measuredText1 = textMeasurer.measure(tagText, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold))
    val measuredText2 = textMeasurer.measure(subTagText, style = TextStyle(fontSize = 9.sp))

    val tagWidth = maxOf(measuredText1.size.width, measuredText2.size.width) + 16f
    val tagHeight = measuredText1.size.height + measuredText2.size.height + 10f

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.85f),
        topLeft = Offset(island.xPx - tagWidth / 2f, island.yPx + radius + 4.dp.toPx()),
        size = Size(tagWidth, tagHeight),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )

    drawText(
        textMeasurer = textMeasurer,
        text = tagText,
        topLeft = Offset(island.xPx - (measuredText1.size.width / 2f), island.yPx + radius + 6.dp.toPx()),
        style = TextStyle(fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
    )

    drawText(
        textMeasurer = textMeasurer,
        text = subTagText,
        topLeft = Offset(island.xPx - (measuredText2.size.width / 2f), island.yPx + radius + measuredText1.size.height + 8.dp.toPx()),
        style = TextStyle(fontSize = 9.sp, color = Color(0xFFFFD54F))
    )
}
