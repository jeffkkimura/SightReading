package com.example.sightreading.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sightreading.model.Lesson
import com.example.sightreading.ui.theme.SpriteType
import com.example.sightreading.ui.theme.WorldTheme

data class MapNodePosition(
    val lesson: Lesson,
    val xPx: Float,
    val yPx: Float,
    val isCompleted: Boolean,
    val isUnlocked: Boolean
)

@Composable
fun WorldSpriteCanvas(
    worldTheme: WorldTheme,
    lessons: List<Lesson>,
    completedLessonIds: Set<String>,
    onSelectLesson: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw World Sky & Terrain Gradient Background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(worldTheme.skyTopColor, worldTheme.skyBottomColor, worldTheme.terrainColor)
                )
            )

            // 2. Draw Specific World Environmental Sprites
            drawWorldEnvironmentalSprites(worldTheme.spriteType, worldTheme, width, height, textMeasurer)

            // 3. Compute Winding Level Node Positions
            val nodePositions = calculateMapNodePositions(lessons, completedLessonIds, width, height)

            // 4. Draw Winding Path Trail
            if (nodePositions.size >= 2) {
                val path = Path().apply {
                    moveTo(nodePositions[0].xPx, nodePositions[0].yPx)
                    for (i in 1 until nodePositions.size) {
                        val prev = nodePositions[i - 1]
                        val curr = nodePositions[i]
                        val midX = (prev.xPx + curr.xPx) / 2f
                        cubicTo(midX, prev.yPx, midX, curr.yPx, curr.xPx, curr.yPx)
                    }
                }

                drawPath(
                    path = path,
                    color = worldTheme.pathColor,
                    style = Stroke(width = 16.dp.toPx())
                )

                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.6f),
                    style = Stroke(width = 6.dp.toPx())
                )
            }

            // 5. Draw Interactive Level Nodes & Sprites
            nodePositions.forEachIndexed { idx, node ->
                drawMapNodeSprite(
                    node = node,
                    index = idx + 1,
                    worldTheme = worldTheme,
                    textMeasurer = textMeasurer
                )
            }
        }

        // Invisible Click Overlay for Level Nodes
        val nodePositions = remember(lessons, completedLessonIds) {
            calculateMapNodePositions(lessons, completedLessonIds, 1080f, 1000f)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(lessons, completedLessonIds) {
                    detectTapGestures { tapOffset ->
                        val hitNode = nodePositions.find { node ->
                            val dx = tapOffset.x - node.xPx
                            val dy = tapOffset.y - node.yPx
                            (dx * dx + dy * dy) <= (40.dp.toPx() * 40.dp.toPx())
                        }
                        if (hitNode != null && hitNode.isUnlocked) {
                            onSelectLesson(hitNode.lesson)
                        }
                    }
                }
        ) {
            // Overlay rendered above
        }
    }
}

private fun calculateMapNodePositions(
    lessons: List<Lesson>,
    completedLessonIds: Set<String>,
    width: Float,
    height: Float
): List<MapNodePosition> {
    if (lessons.isEmpty()) return emptyList()

    val paddingX = width * 0.18f
    val availableWidth = width - (paddingX * 2f)

    val yStart = height * 0.80f
    val yStep = (height * 0.65f) / maxOf(1, lessons.size - 1)

    return lessons.mapIndexed { idx, lesson ->
        // S-curve winding path pattern
        val xProgress = if (idx % 2 == 0) 0.15f + (idx * 0.2f) % 0.7f else 0.85f - (idx * 0.2f) % 0.7f
        val x = paddingX + (availableWidth * xProgress)
        val y = yStart - (idx * yStep)

        val isCompleted = completedLessonIds.contains(lesson.id)
        val isUnlocked = idx == 0 || completedLessonIds.contains(lessons[idx - 1].id)

        MapNodePosition(
            lesson = lesson,
            xPx = x,
            yPx = y,
            isCompleted = isCompleted,
            isUnlocked = isUnlocked
        )
    }
}

private fun DrawScope.drawWorldEnvironmentalSprites(
    spriteType: SpriteType,
    theme: WorldTheme,
    width: Float,
    height: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    when (spriteType) {
        SpriteType.CRYSTAL_MEADOW -> {
            // Draw Crystal Spires
            drawCircle(Color.White.copy(alpha = 0.2f), center = Offset(width * 0.85f, height * 0.25f), radius = 45.dp.toPx())
            drawText(textMeasurer, "💎", Offset(width * 0.10f, height * 0.15f), style = TextStyle(fontSize = 36.sp))
            drawText(textMeasurer, "✨", Offset(width * 0.80f, height * 0.10f), style = TextStyle(fontSize = 28.sp))
            drawText(textMeasurer, "🌿", Offset(width * 0.05f, height * 0.70f), style = TextStyle(fontSize = 32.sp))
            drawText(textMeasurer, "🌸", Offset(width * 0.88f, height * 0.65f), style = TextStyle(fontSize = 32.sp))
        }
        SpriteType.SUNLIT_FOREST -> {
            drawText(textMeasurer, "🌲", Offset(width * 0.08f, height * 0.12f), style = TextStyle(fontSize = 42.sp))
            drawText(textMeasurer, "🌲", Offset(width * 0.82f, height * 0.18f), style = TextStyle(fontSize = 42.sp))
            drawText(textMeasurer, "☀️", Offset(width * 0.48f, height * 0.05f), style = TextStyle(fontSize = 48.sp))
            drawText(textMeasurer, "🍄", Offset(width * 0.12f, height * 0.75f), style = TextStyle(fontSize = 30.sp))
        }
        SpriteType.DESERT_DUNES -> {
            drawText(textMeasurer, "🏜️", Offset(width * 0.82f, height * 0.15f), style = TextStyle(fontSize = 48.sp))
            drawText(textMeasurer, "🌴", Offset(width * 0.10f, height * 0.68f), style = TextStyle(fontSize = 38.sp))
            drawText(textMeasurer, "☀️", Offset(width * 0.15f, height * 0.05f), style = TextStyle(fontSize = 42.sp))
        }
        SpriteType.AURORA_PEAKS -> {
            drawText(textMeasurer, "🏔️", Offset(width * 0.05f, height * 0.10f), style = TextStyle(fontSize = 54.sp))
            drawText(textMeasurer, "🏔️", Offset(width * 0.75f, height * 0.12f), style = TextStyle(fontSize = 54.sp))
            drawText(textMeasurer, "🌌", Offset(width * 0.40f, height * 0.02f), style = TextStyle(fontSize = 42.sp))
        }
        SpriteType.COSMIC_NEBULA, SpriteType.MIDNIGHT_NEBULA -> {
            drawText(textMeasurer, "🪐", Offset(width * 0.10f, height * 0.08f), style = TextStyle(fontSize = 44.sp))
            drawText(textMeasurer, "⭐", Offset(width * 0.85f, height * 0.12f), style = TextStyle(fontSize = 32.sp))
            drawText(textMeasurer, "✨", Offset(width * 0.50f, height * 0.05f), style = TextStyle(fontSize = 30.sp))
            drawText(textMeasurer, "🌟", Offset(width * 0.78f, height * 0.70f), style = TextStyle(fontSize = 32.sp))
        }
        else -> {
            // Default High Quality Stars & Flora Sprites
            drawText(textMeasurer, "🏰", Offset(width * 0.78f, height * 0.08f), style = TextStyle(fontSize = 48.sp))
            drawText(textMeasurer, "✨", Offset(width * 0.12f, height * 0.12f), style = TextStyle(fontSize = 32.sp))
            drawText(textMeasurer, "🌟", Offset(width * 0.88f, height * 0.70f), style = TextStyle(fontSize = 32.sp))
        }
    }
}

private fun DrawScope.drawMapNodeSprite(
    node: MapNodePosition,
    index: Int,
    worldTheme: WorldTheme,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val radius = 26.dp.toPx()

    // Outer Glow / Ring for active node
    if (node.isUnlocked && !node.isCompleted) {
        drawCircle(
            color = worldTheme.nodeActiveColor.copy(alpha = 0.4f),
            center = Offset(node.xPx, node.yPx),
            radius = radius * 1.5f
        )
    }

    // Node Core Circle
    val nodeBgColor = when {
        node.isCompleted -> worldTheme.nodeCompletedColor
        node.isUnlocked -> worldTheme.nodeActiveColor
        else -> Color.Gray
    }

    drawCircle(
        color = nodeBgColor,
        center = Offset(node.xPx, node.yPx),
        radius = radius
    )

    drawCircle(
        color = Color.White,
        center = Offset(node.xPx, node.yPx),
        radius = radius,
        style = Stroke(width = 3.dp.toPx())
    )

    // Node Symbol Sprite
    val symbol = when {
        node.isCompleted -> "⭐"
        node.isUnlocked -> "🎹"
        else -> "🔒"
    }

    drawText(
        textMeasurer = textMeasurer,
        text = symbol,
        topLeft = Offset(node.xPx - 11.dp.toPx(), node.yPx - 13.dp.toPx()),
        style = TextStyle(fontSize = 18.sp)
    )

    // Level Title Tag
    val tagText = "Level $index"
    val measuredText = textMeasurer.measure(tagText, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold))
    val tagWidth = measuredText.size.width + 16f
    val tagHeight = measuredText.size.height + 8f

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.75f),
        topLeft = Offset(node.xPx - tagWidth / 2f, node.yPx + radius + 4.dp.toPx()),
        size = Size(tagWidth, tagHeight),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )

    drawText(
        textMeasurer = textMeasurer,
        text = tagText,
        topLeft = Offset(node.xPx - (measuredText.size.width / 2f), node.yPx + radius + 8.dp.toPx()),
        style = TextStyle(fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
    )
}
