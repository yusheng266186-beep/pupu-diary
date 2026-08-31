package com.pupu.diary.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pupu.diary.ui.theme.LocalAppPalette

/** 柱状图：入场逐根错峰生长，柱顶显示数值 */
@Composable
fun BarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val textMeasurer = rememberTextMeasurer()
    val anim = remember { Animatable(0f) }
    val reduceMotion = com.pupu.diary.ui.theme.LocalReduceMotion.current
    LaunchedEffect(values, reduceMotion) {
        anim.snapTo(0f)
        if (reduceMotion) anim.snapTo(1f)
        else anim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    Canvas(modifier.fillMaxWidth().height(190.dp)) {
        val n = values.size
        if (n == 0) return@Canvas
        val maxV = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val labelH = 30f
        val chartH = size.height - labelH
        val leftPad = 30f
        val gap = size.width * 0.025f
        val barW = (size.width - leftPad - gap * (n + 1)) / n
        val labelStyle = TextStyle(fontSize = 11.sp, color = palette.textSub)
        val axisStyle = TextStyle(fontSize = 8.sp, color = palette.textSub.copy(alpha = 0.7f))
        val valueStyle = TextStyle(fontSize = 10.sp, color = palette.primaryDeep, fontWeight = FontWeight.Bold)

        // Y 轴刻度线
        listOf(1f, 2f / 3f, 1f / 3f).forEach { f ->
            val gy = chartH - (chartH - 20f) * f
            drawLine(
                palette.textSub.copy(alpha = 0.12f),
                Offset(leftPad, gy), Offset(size.width, gy),
                strokeWidth = 1.5f
            )
            val t = textMeasurer.measure("${(maxV * f).toInt()}", axisStyle)
            drawText(t, topLeft = Offset(leftPad - t.size.width - 6f, gy - t.size.height / 2f))
        }
        drawLine(
            palette.textSub.copy(alpha = 0.25f),
            Offset(leftPad, chartH), Offset(size.width, chartH),
            strokeWidth = 2f
        )
        values.forEachIndexed { i, v ->
            val raw = ((anim.value * (n + 3f) - i) / 3f).coerceIn(0f, 1f)
            val eased = FastOutSlowInEasing.transform(raw)
            val h = (v / maxV) * (chartH - 20f) * eased
            val x = leftPad + gap + i * (barW + gap)
            if (v > 0f) {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(palette.primary, palette.primary.copy(alpha = 0.5f)),
                        startY = chartH - h,
                        endY = chartH
                    ),
                    topLeft = Offset(x, chartH - h),
                    size = Size(barW, h.coerceAtLeast(5f * eased)),
                    cornerRadius = CornerRadius(barW * 0.3f, barW * 0.3f)
                )
            } else {
                drawCircle(
                    palette.textSub.copy(alpha = 0.3f),
                    radius = 3f,
                    center = Offset(x + barW / 2f, chartH - 6f)
                )
            }
            val tr = textMeasurer.measure(labels[i], labelStyle)
            drawText(tr, topLeft = Offset(x + barW / 2f - tr.size.width / 2f, chartH + 8f))
            if (raw >= 1f && v > 0f) {
                val vt = textMeasurer.measure("${v.toInt()}", valueStyle)
                drawText(
                    vt,
                    topLeft = Offset(x + barW / 2f - vt.size.width / 2f, chartH - h - vt.size.height - 6f)
                )
            }
        }
    }
}

/** 环形图：扫掠入场，段间留缝 */
@Composable
fun DonutChart(
    segments: List<Pair<Color, Float>>,
    modifier: Modifier = Modifier
) {
    val anim = remember { Animatable(0f) }
    val reduceMotion = com.pupu.diary.ui.theme.LocalReduceMotion.current
    LaunchedEffect(segments, reduceMotion) {
        anim.snapTo(0f)
        if (reduceMotion) anim.snapTo(1f)
        else anim.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }
    Canvas(modifier.size(150.dp)) {
        val total = segments.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
        val stroke = 24.dp.toPx()
        val inset = stroke / 2f + 2.dp.toPx()
        val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
        val topLeft = Offset(inset, inset)
        var start = -90f
        segments.forEach { (color, value) ->
            val sweep = value / total * 360f * anim.value
            if (sweep > 0.5f) {
                drawArc(
                    color, start, sweep - 2.5f, false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Butt)
                )
            }
            start += sweep
        }
    }
}
