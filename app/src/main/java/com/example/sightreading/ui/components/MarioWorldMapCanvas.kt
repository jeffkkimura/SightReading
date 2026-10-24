package com.example.sightreading.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sightreading.model.Lesson
import com.example.sightreading.ui.theme.WorldTheme

data class MarioNodePosition(
    val lesson: Lesson,
    val index: Int,
    val xPx: Float,
    val yPx: Float,
    val isCompleted: Boolean,
    val isUnlocked: Boolean,
    val isBossCastle: Boolean,
    val isFortress: Boolean
)

@Composable
fun MarioWorldMapCanvas(
    worldTheme: WorldTheme,
    lessons: List<Lesson>,
    completedLessonIds: Set<String>,
    masteredLessonIds: Set<String> = emptySet(),
    onSelectLesson: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()
    var selectedLessonId by remember { mutableStateOf<String?>(lessons.firstOrNull()?.id) }

    // Resolve Image Sprite Bitmap for the World if PNG asset exists
    val drawableResId = remember(worldTheme.spriteType) {
        WorldSpriteBackgrounds.getSpriteDrawableRes(context, worldTheme.spriteType)
    }

    val worldImageBitmap = remember(drawableResId) {
        if (drawableResId != null) {
            try {
                BitmapFactory.decodeResource(context.resources, drawableResId)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(lessons, completedLessonIds, masteredLessonIds) {
                    detectTapGestures { tapOffset ->
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val nodePositions = calculateMarioMapNodePositions(lessons, completedLessonIds, masteredLessonIds, width, height)

                        val hitRadiusPx = 52.dp.toPx()
                        val hitNode = nodePositions.find { node ->
                            val dx = tapOffset.x - node.xPx
                            val dy = tapOffset.y - node.yPx
                            (dx * dx + dy * dy) <= (hitRadiusPx * hitRadiusPx)
                        }

                        if (hitNode != null && hitNode.isUnlocked) {
                            selectedLessonId = hitNode.lesson.id
                            onSelectLesson(hitNode.lesson)
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw Custom High-Res WebP Image Background with 100% Aspect Ratio Preservation (No Stretching!)
            if (worldImageBitmap != null) {
                val scale = maxOf(width / worldImageBitmap.width.toFloat(), height / worldImageBitmap.height.toFloat())
                val scaledWidth = (worldImageBitmap.width * scale).toInt()
                val scaledHeight = (worldImageBitmap.height * scale).toInt()
                val offsetX = ((width - scaledWidth) / 2f).toInt()
                val offsetY = ((height - scaledHeight) / 2f).toInt()

                drawImage(
                    image = worldImageBitmap,
                    dstOffset = IntOffset(offsetX, offsetY),
                    dstSize = IntSize(scaledWidth, scaledHeight)
                )
            } else {
                WorldSpriteBackgrounds.drawWorldSpriteBackground(this, worldTheme, textMeasurer)
            }

            // 2. Compute Full Screen Spaced-Out Node Positions
            val nodePositions = calculateMarioMapNodePositions(lessons, completedLessonIds, masteredLessonIds, width, height)

            // 3. Draw Winding Paved Mario Path
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

                // Outer Yellow Paved Path Border
                drawPath(
                    path = path,
                    color = Color(0xFFFBC02D),
                    style = Stroke(width = 20.dp.toPx())
                )

                // Inner Dotted Black Path Line
                drawPath(
                    path = path,
                    color = Color(0xFF212121),
                    style = Stroke(width = 8.dp.toPx())
                )
            }

            // 4. Draw 25 Mario Stage Nodes
            nodePositions.forEach { node ->
                val isSelected = node.lesson.id == selectedLessonId
                drawMarioNodeSprite(
                    node = node,
                    isSelected = isSelected,
                    textMeasurer = textMeasurer
                )
            }
        }
    }
}

private fun calculateMarioMapNodePositions(
    lessons: List<Lesson>,
    completedLessonIds: Set<String>,
    masteredLessonIds: Set<String>,
    width: Float,
    height: Float
): List<MarioNodePosition> {
    if (lessons.isEmpty()) return emptyList()

    val paddingX = width * 0.16f
    val availableWidth = width - (paddingX * 2f)

    val topMargin = height * 0.18f
    val bottomMargin = height * 0.18f
    val yStart = height - bottomMargin
    val yStep = (height - topMargin - bottomMargin) / maxOf(1, lessons.size - 1)

    return lessons.mapIndexed { idx, lesson ->
        val levelNum = idx + 1

        val xProgress = when (levelNum % 8) {
            1 -> 0.10f
            2 -> 0.32f
            3 -> 0.68f
            4 -> 0.90f
            5 -> 0.85f
            6 -> 0.65f
            7 -> 0.35f
            0 -> 0.12f
            else -> 0.5f
        }

        val x = paddingX + (availableWidth * xProgress)
        val y = yStart - (idx * yStep)

        val isCompleted = completedLessonIds.contains(lesson.id)
        val isUnlocked = idx == 0 || masteredLessonIds.contains(lessons[idx - 1].id) || completedLessonIds.contains(lessons[idx - 1].id)
        val isFortress = levelNum in listOf(5, 10, 15, 20)
        val isBossCastle = levelNum == 25

        MarioNodePosition(
            lesson = lesson,
            index = levelNum,
            xPx = x,
            yPx = y,
            isCompleted = isCompleted,
            isUnlocked = isUnlocked,
            isBossCastle = isBossCastle,
            isFortress = isFortress
        )
    }
}

private fun DrawScope.drawMarioNodeSprite(
    node: MarioNodePosition,
    isSelected: Boolean,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val radius = if (node.isBossCastle) 34.dp.toPx() else if (node.isFortress) 28.dp.toPx() else 22.dp.toPx()

    // 1. Selection Highlight Ring
    if (isSelected) {
        drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = 0.6f),
            center = Offset(node.xPx, node.yPx),
            radius = radius * 1.8f
        )
    }

    // 2. Stage Pad Outer Border
    val padColor = when {
        node.isBossCastle -> Color(0xFFD50000)
        node.isFortress -> Color(0xFF37474F)
        node.isCompleted -> Color(0xFFFFD54F)
        node.isUnlocked -> Color(0xFF00E676)
        else -> Color(0xFF616161)
    }

    drawCircle(
        color = padColor,
        center = Offset(node.xPx, node.yPx),
        radius = radius
    )

    drawCircle(
        color = Color.White,
        center = Offset(node.xPx, node.yPx),
        radius = radius,
        style = Stroke(width = 3.5.dp.toPx())
    )

    // 3. Stage Symbol
    val symbol = when {
        node.isBossCastle -> "🏰"
        node.isFortress -> "🏰"
        node.isCompleted -> "⭐"
        node.isUnlocked -> "🎹"
        else -> "🔒"
    }

    drawText(
        textMeasurer = textMeasurer,
        text = symbol,
        topLeft = Offset(node.xPx - 10.dp.toPx(), node.yPx - 12.dp.toPx()),
        style = TextStyle(fontSize = 16.sp)
    )

    // 4. Mario Player Character Sprite on Active Node
    if (node.isUnlocked && !node.isCompleted) {
        drawText(
            textMeasurer = textMeasurer,
            text = "🏃‍♂️",
            topLeft = Offset(node.xPx - 12.dp.toPx(), node.yPx - radius - 26.dp.toPx()),
            style = TextStyle(fontSize = 26.sp)
        )
    }

    // 5. Level Label Tag
    val tagText = if (node.isBossCastle) "BOSS 25" else "ST ${node.index}"
    val measuredText = textMeasurer.measure(tagText, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold))

    val tagWidth = measuredText.size.width + 12f
    val tagHeight = measuredText.size.height + 6f

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.85f),
        topLeft = Offset(node.xPx - tagWidth / 2f, node.yPx + radius + 3.dp.toPx()),
        size = Size(tagWidth, tagHeight),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )

    drawText(
        textMeasurer = textMeasurer,
        text = tagText,
        topLeft = Offset(node.xPx - (measuredText.size.width / 2f), node.yPx + radius + 6.dp.toPx()),
        style = TextStyle(fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
    )
}
