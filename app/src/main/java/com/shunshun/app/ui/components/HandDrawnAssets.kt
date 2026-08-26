package com.shunshun.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.material3.Text
import com.shunshun.app.navigation.AppTab
import com.shunshun.app.ui.BlueMist
import com.shunshun.app.ui.Brown
import com.shunshun.app.ui.BrownDark
import com.shunshun.app.ui.Meadow
import com.shunshun.app.ui.Muted
import com.shunshun.app.ui.Sage
import com.shunshun.app.ui.SageDark
import com.shunshun.app.ui.SageLight
import com.shunshun.app.ui.Yellow

enum class MascotMood { Default, Happy, Excited, Sleepy, Sad, Water }

@Composable
fun PoopMascot(size: Dp, mood: MascotMood = MascotMood.Default) {
    var blinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(3_000L, 6_000L))
            blinking = true
            delay(120L)
            blinking = false
        }
    }
    val eyeScale by animateFloatAsState(if (blinking) .16f else 1f, tween(90), label = "mascot-blink")
    Canvas(Modifier.size(size)) {
        val w = size.toPx(); val h = size.toPx()
        val body = Path().apply { moveTo(w * .16f, h * .78f); cubicTo(w * .10f, h * .62f, w * .17f, h * .48f, w * .29f, h * .43f); cubicTo(w * .30f, h * .25f, w * .42f, h * .34f, w * .50f, h * .16f); cubicTo(w * .59f, h * .02f, w * .64f, h * .27f, w * .72f, h * .40f); cubicTo(w * .88f, h * .48f, w * .93f, h * .68f, w * .83f, h * .80f); cubicTo(w * .67f, h * .98f, w * .31f, h * .98f, w * .16f, h * .78f); close() }
        drawPath(body, Brown)
        drawLine(BrownDark.copy(alpha = .38f), Offset(w * .21f, h * .77f), Offset(w * .34f, h * .87f), 2f, StrokeCap.Round)
        drawLine(BrownDark.copy(alpha = .38f), Offset(w * .79f, h * .77f), Offset(w * .66f, h * .87f), 2f, StrokeCap.Round)
        val eyeY = if (mood == MascotMood.Sleepy) .62f else .60f
        if (mood == MascotMood.Sleepy) { drawLine(BrownDark, Offset(w * .32f, h * eyeY), Offset(w * .41f, h * eyeY), 3f, StrokeCap.Round); drawLine(BrownDark, Offset(w * .59f, h * eyeY), Offset(w * .68f, h * eyeY), 3f, StrokeCap.Round) } else { val radius = w * .034f; drawOval(BrownDark, topLeft = Offset(w * .38f - radius, h * eyeY - radius * eyeScale), size = Size(radius * 2f, radius * 2f * eyeScale)); drawOval(BrownDark, topLeft = Offset(w * .62f - radius, h * eyeY - radius * eyeScale), size = Size(radius * 2f, radius * 2f * eyeScale)) }
        drawCircle(Color(0xFFE7A6A0), w * .06f, Offset(w * .27f, h * .69f)); drawCircle(Color(0xFFE7A6A0), w * .06f, Offset(w * .73f, h * .69f))
        if (mood == MascotMood.Sad) drawArc(BrownDark, 200f, 140f, false, topLeft = Offset(w * .40f, h * .64f), size = Size(w * .20f, h * .14f), style = Stroke(3f)) else drawArc(BrownDark, 15f, 150f, false, topLeft = Offset(w * .40f, h * .61f), size = Size(w * .20f, h * .15f), style = Stroke(3f))
        if (mood == MascotMood.Water) { drawLine(BlueMist, Offset(w * .84f, h * .22f), Offset(w * .95f, h * .06f), 4f, StrokeCap.Round); drawCircle(BlueMist, 4f, Offset(w * .96f, h * .22f)) }
    }
}

@Composable
fun StoolIcon(type: Int, size: Dp = 48.dp) {
    Canvas(Modifier.size(size)) {
        val w = size.toPx(); val h = size.toPx(); val fill = Color(0xFFF6EBDD); val outline = BrownDark.copy(alpha = .82f); val detail = BrownDark.copy(alpha = .62f); val stroke = Stroke(width = 2.2f, cap = StrokeCap.Round)
        when (type) {
            1 -> { drawCircle(fill, w * .16f, Offset(w * .34f, h * .55f)); drawCircle(fill, w * .16f, Offset(w * .66f, h * .55f)); drawCircle(fill, w * .13f, Offset(w * .50f, h * .28f)); drawCircle(outline, w * .16f, Offset(w * .34f, h * .55f), style = stroke); drawCircle(outline, w * .16f, Offset(w * .66f, h * .55f), style = stroke); drawCircle(outline, w * .13f, Offset(w * .50f, h * .28f), style = stroke) }
            2 -> { drawRoundRect(fill, topLeft = Offset(w * .16f, h * .30f), size = Size(w * .68f, h * .34f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .17f)); drawRoundRect(outline, topLeft = Offset(w * .16f, h * .30f), size = Size(w * .68f, h * .34f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .17f), style = stroke); drawLine(detail, Offset(w * .28f, h * .39f), Offset(w * .69f, h * .53f), 2f, StrokeCap.Round) }
            3 -> { drawOval(fill, topLeft = Offset(w * .17f, h * .28f), size = Size(w * .66f, h * .39f)); drawOval(outline, topLeft = Offset(w * .17f, h * .28f), size = Size(w * .66f, h * .39f), style = stroke); drawLine(detail, Offset(w * .34f, h * .34f), Offset(w * .28f, h * .49f), 2f, StrokeCap.Round); drawLine(detail, Offset(w * .54f, h * .30f), Offset(w * .48f, h * .48f), 2f, StrokeCap.Round); drawLine(detail, Offset(w * .72f, h * .38f), Offset(w * .66f, h * .55f), 2f, StrokeCap.Round) }
            4 -> { val p = Path().apply { moveTo(w * .20f, h * .64f); cubicTo(w * .10f, h * .50f, w * .18f, h * .34f, w * .34f, h * .37f); cubicTo(w * .37f, h * .19f, w * .61f, h * .18f, w * .65f, h * .35f); cubicTo(w * .86f, h * .29f, w * .91f, h * .54f, w * .77f, h * .65f); cubicTo(w * .60f, h * .77f, w * .34f, h * .76f, w * .20f, h * .64f); close() }; drawPath(p, fill); drawPath(p, outline, style = stroke) }
            5 -> { drawCircle(fill, w * .18f, Offset(w * .30f, h * .52f)); drawCircle(fill, w * .20f, Offset(w * .50f, h * .45f)); drawCircle(fill, w * .17f, Offset(w * .70f, h * .53f)); drawOval(fill, topLeft = Offset(w * .24f, h * .54f), size = Size(w * .52f, h * .24f)); drawCircle(outline, w * .18f, Offset(w * .30f, h * .52f), style = stroke); drawCircle(outline, w * .20f, Offset(w * .50f, h * .45f), style = stroke); drawCircle(outline, w * .17f, Offset(w * .70f, h * .53f), style = stroke) }
            6 -> { val p = Path().apply { moveTo(w * .18f, h * .38f); quadraticTo(w * .28f, h * .23f, w * .39f, h * .39f); quadraticTo(w * .50f, h * .57f, w * .61f, h * .36f); quadraticTo(w * .73f, h * .20f, w * .84f, h * .42f); quadraticTo(w * .79f, h * .69f, w * .52f, h * .72f); quadraticTo(w * .23f, h * .74f, w * .18f, h * .38f); close() }; drawPath(p, fill); drawPath(p, outline, style = stroke); drawCircle(detail, 2.5f, Offset(w * .34f, h * .50f)); drawCircle(detail, 2.5f, Offset(w * .61f, h * .54f)) }
            else -> { drawCircle(fill, w * .20f, Offset(w * .34f, h * .38f)); drawCircle(fill, w * .20f, Offset(w * .66f, h * .38f)); drawCircle(fill, w * .17f, Offset(w * .50f, h * .64f)); drawCircle(outline, w * .20f, Offset(w * .34f, h * .38f), style = stroke); drawCircle(outline, w * .20f, Offset(w * .66f, h * .38f), style = stroke); drawCircle(outline, w * .17f, Offset(w * .50f, h * .64f), style = stroke); drawCircle(fill, w * .08f, Offset(w * .21f, h * .78f)); drawCircle(fill, w * .08f, Offset(w * .79f, h * .78f)) }
        }
    }
}

@Composable
fun MoodFace(index: Int, selected: Boolean, onClick: () -> Unit) {
    val faceColors = listOf(Color(0xFFB8D6B8), Color(0xFFF0B8AE), Color(0xFFB8CCD9), Color(0xFFF1B58B), Color(0xFFF2D48A))
    val faceScale by animateFloatAsState(if (selected) 1.08f else 1f, androidx.compose.animation.core.spring(dampingRatio = .7f), label = "mood-$index")
    Column(Modifier.width(58.dp).scale(faceScale).appClickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).background(faceColors[index].copy(alpha = if (selected) 1f else .45f), CircleShape).border(if (selected) 2.dp else 0.dp, if (selected) SageDark else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(9.dp)) {
                val ink = BrownDark.copy(alpha = .78f)
                val line = 1.8f
                val left = Offset(size.width * .28f, size.height * .38f)
                val right = Offset(size.width * .72f, size.height * .38f)
                drawCircle(ink, 2.1f, left)
                drawCircle(ink, 2.1f, right)
                drawCircle(Color.White.copy(alpha = .42f), 1.1f, Offset(left.x - .6f, left.y - .7f))
                drawCircle(Color.White.copy(alpha = .42f), 1.1f, Offset(right.x - .6f, right.y - .7f))
                when (index) {
                    0 -> {
                        drawLine(ink, Offset(left.x - 3f, left.y - 5f), Offset(left.x + 3f, left.y - 3f), line, StrokeCap.Round)
                        drawLine(ink, Offset(right.x - 3f, right.y - 3f), Offset(right.x + 3f, right.y - 5f), line, StrokeCap.Round)
                        drawArc(ink, 205f, 130f, false, topLeft = Offset(size.width * .30f, size.height * .47f), size = Size(size.width * .40f, size.height * .29f), style = Stroke(line, cap = StrokeCap.Round))
                    }
                    1 -> {
                        drawLine(ink, Offset(left.x - 3f, left.y + 1f), Offset(left.x + 3f, left.y + 2f), line, StrokeCap.Round)
                        drawLine(ink, Offset(right.x - 3f, right.y + 2f), Offset(right.x + 3f, right.y + 1f), line, StrokeCap.Round)
                        drawArc(ink, 195f, 130f, false, topLeft = Offset(size.width * .32f, size.height * .49f), size = Size(size.width * .36f, size.height * .24f), style = Stroke(line, cap = StrokeCap.Round))
                    }
                    2 -> {
                        drawLine(ink, Offset(size.width * .35f, size.height * .63f), Offset(size.width * .65f, size.height * .63f), line, StrokeCap.Round)
                        drawCircle(ink.copy(alpha = .35f), 1.1f, Offset(size.width * .50f, size.height * .50f))
                    }
                    3 -> {
                        drawArc(ink, 200f, 140f, false, topLeft = Offset(left.x - 4f, left.y - 2f), size = Size(8f, 6f), style = Stroke(line, cap = StrokeCap.Round))
                        drawArc(ink, 200f, 140f, false, topLeft = Offset(right.x - 4f, right.y - 2f), size = Size(8f, 6f), style = Stroke(line, cap = StrokeCap.Round))
                        drawArc(ink, 20f, 140f, false, topLeft = Offset(size.width * .28f, size.height * .46f), size = Size(size.width * .44f, size.height * .30f), style = Stroke(line, cap = StrokeCap.Round))
                    }
                    else -> {
                        drawCircle(ink, 1.4f, Offset(left.x, left.y - 1f)); drawCircle(ink, 1.4f, Offset(right.x, right.y - 1f))
                        drawArc(ink, 20f, 140f, false, topLeft = Offset(size.width * .27f, size.height * .43f), size = Size(size.width * .46f, size.height * .34f), style = Stroke(line + .3f, cap = StrokeCap.Round))
                    }
                }
                drawCircle(Color(0xFFE7A6A0).copy(alpha = .50f), 3.2f, Offset(size.width * .18f, size.height * .57f))
                drawCircle(Color(0xFFE7A6A0).copy(alpha = .50f), 3.2f, Offset(size.width * .82f, size.height * .57f))
            }
        }
        Text(listOf("很难受", "不舒服", "一般", "舒服", "超舒服")[index], color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
fun NavGlyph(tab: AppTab, tint: Color) {
    Canvas(Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = size.minDimension * .095f
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        when (tab) {
            AppTab.HOME -> {
                val house = Path().apply {
                    moveTo(w * .10f, h * .44f); lineTo(w * .50f, h * .10f); lineTo(w * .90f, h * .44f)
                    moveTo(w * .21f, h * .39f); lineTo(w * .21f, h * .88f); lineTo(w * .79f, h * .88f); lineTo(w * .79f, h * .39f)
                }
                drawPath(house, tint, style = stroke)
                drawRoundRect(tint, topLeft = Offset(w * .42f, h * .61f), size = Size(w * .16f, h * .27f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .035f), style = stroke)
            }
            AppTab.RECORD -> {
                drawRoundRect(tint, topLeft = Offset(w * .18f, h * .15f), size = Size(w * .64f, h * .72f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .10f), style = stroke)
                drawRoundRect(tint, topLeft = Offset(w * .34f, h * .08f), size = Size(w * .32f, h * .18f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .06f), style = stroke)
                drawLine(tint, Offset(w * .30f, h * .42f), Offset(w * .70f, h * .42f), strokeWidth, StrokeCap.Round)
                drawLine(tint, Offset(w * .30f, h * .61f), Offset(w * .60f, h * .61f), strokeWidth, StrokeCap.Round)
                drawLine(tint, Offset(w * .30f, h * .75f), Offset(w * .53f, h * .75f), strokeWidth, StrokeCap.Round)
            }
            AppTab.STATS -> {
                drawLine(tint, Offset(w * .14f, h * .86f), Offset(w * .88f, h * .86f), strokeWidth, StrokeCap.Round)
                drawRoundRect(tint, topLeft = Offset(w * .23f, h * .55f), size = Size(w * .13f, h * .31f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .05f))
                drawRoundRect(tint, topLeft = Offset(w * .44f, h * .36f), size = Size(w * .13f, h * .50f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .05f))
                drawRoundRect(tint, topLeft = Offset(w * .65f, h * .18f), size = Size(w * .13f, h * .68f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .05f))
            }
            AppTab.PROFILE -> {
                drawCircle(tint, w * .16f, Offset(w * .50f, h * .27f), style = stroke)
                drawArc(tint, 200f, 140f, false, topLeft = Offset(w * .16f, h * .47f), size = Size(w * .68f, h * .40f), style = stroke)
            }
        }
    }
}
