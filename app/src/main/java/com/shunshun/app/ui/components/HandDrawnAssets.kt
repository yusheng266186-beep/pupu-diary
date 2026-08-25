package com.shunshun.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Canvas(Modifier.size(size)) {
        val w = size.toPx(); val h = size.toPx()
        val body = Path().apply { moveTo(w * .16f, h * .78f); cubicTo(w * .10f, h * .62f, w * .17f, h * .48f, w * .29f, h * .43f); cubicTo(w * .30f, h * .25f, w * .42f, h * .34f, w * .50f, h * .16f); cubicTo(w * .59f, h * .02f, w * .64f, h * .27f, w * .72f, h * .40f); cubicTo(w * .88f, h * .48f, w * .93f, h * .68f, w * .83f, h * .80f); cubicTo(w * .67f, h * .98f, w * .31f, h * .98f, w * .16f, h * .78f); close() }
        drawPath(body, Brown)
        drawLine(BrownDark.copy(alpha = .38f), Offset(w * .21f, h * .77f), Offset(w * .34f, h * .87f), 2f, StrokeCap.Round)
        drawLine(BrownDark.copy(alpha = .38f), Offset(w * .79f, h * .77f), Offset(w * .66f, h * .87f), 2f, StrokeCap.Round)
        val eyeY = if (mood == MascotMood.Sleepy) .62f else .60f
        if (mood == MascotMood.Sleepy) { drawLine(BrownDark, Offset(w * .32f, h * eyeY), Offset(w * .41f, h * eyeY), 3f, StrokeCap.Round); drawLine(BrownDark, Offset(w * .59f, h * eyeY), Offset(w * .68f, h * eyeY), 3f, StrokeCap.Round) } else { drawCircle(BrownDark, w * .034f, Offset(w * .38f, h * eyeY)); drawCircle(BrownDark, w * .034f, Offset(w * .62f, h * eyeY)) }
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
    Column(Modifier.width(58.dp).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).background(faceColors[index].copy(alpha = if (selected) 1f else .45f), CircleShape).border(if (selected) 2.dp else 0.dp, if (selected) SageDark else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) { Canvas(Modifier.fillMaxSize().padding(12.dp)) { val ink = BrownDark.copy(alpha = .75f); drawCircle(ink, 2.2f, Offset(size.width * .30f, size.height * .38f)); drawCircle(ink, 2.2f, Offset(size.width * .70f, size.height * .38f)); if (index < 2) drawArc(ink, 20f, 140f, false, topLeft = Offset(size.width * .28f, size.height * .48f), size = Size(size.width * .44f, size.height * .28f), style = Stroke(2f)) else drawArc(ink, 200f, 140f, false, topLeft = Offset(size.width * .28f, size.height * .38f), size = Size(size.width * .44f, size.height * .28f), style = Stroke(2f)) } }
        Text(listOf("很难受", "不舒服", "一般", "舒服", "超舒服")[index], color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
fun NavGlyph(tab: AppTab, tint: Color) {
    Canvas(Modifier.size(22.dp)) {
        val stroke = Stroke(width = 2.2f, cap = StrokeCap.Round)
        when (tab) {
            AppTab.HOME -> { val house = Path().apply { moveTo(2f, 10f); lineTo(size.width / 2f, 3f); lineTo(size.width - 2f, 10f); moveTo(4f, 9f); lineTo(4f, size.height - 2f); lineTo(size.width - 4f, size.height - 2f); lineTo(size.width - 4f, 9f) }; drawPath(house, tint, style = stroke); drawLine(tint, Offset(size.width / 2f, size.height - 2f), Offset(size.width / 2f, size.height - 8f), 2.2f, StrokeCap.Round) }
            AppTab.RECORD -> { drawRoundRect(tint, topLeft = Offset(4f, 3f), size = Size(size.width - 8f, size.height - 5f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f), style = stroke); drawLine(tint, Offset(8f, 8f), Offset(size.width - 7f, 8f), 2f, StrokeCap.Round); drawLine(tint, Offset(8f, 13f), Offset(size.width - 7f, 13f), 2f, StrokeCap.Round) }
            AppTab.STATS -> { drawLine(tint, Offset(3f, size.height - 3f), Offset(size.width - 3f, size.height - 3f), 2.2f, StrokeCap.Round); drawRoundRect(tint, topLeft = Offset(5f, 10f), size = Size(3f, 7f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f)); drawRoundRect(tint, topLeft = Offset(10f, 6f), size = Size(3f, 11f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f)); drawRoundRect(tint, topLeft = Offset(15f, 3f), size = Size(3f, 14f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f)) }
            AppTab.PROFILE -> { drawCircle(tint, 5f, Offset(size.width / 2f, 6f), style = stroke); drawArc(tint, 200f, 140f, false, topLeft = Offset(4f, 10f), size = Size(size.width - 8f, 11f), style = stroke) }
        }
    }
}
