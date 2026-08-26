package com.pupu.diary.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pupu.diary.ui.theme.LocalAppPalette

/** Bristol 1–7 型图标：2–5 型共用迷你漩涡造型 + 不同纹理，1/6/7 为硬块/糊/水样（对齐参考稿） */
@Composable
fun BristolIcon(type: Int, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val palette = LocalAppPalette.current
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension / 100f
        val c = palette.poop
        val hi = Color.White.copy(alpha = 0.4f)
        val dark = palette.poopDark.copy(alpha = 0.4f)
        when (type) {
            1 -> {
                // 硬块：分离的小球
                ball(this, 30f, 60f, 14f, s, c); ball(this, 52f, 68f, 12f, s, c)
                ball(this, 70f, 55f, 9.5f, s, c); ball(this, 47f, 41f, 8.5f, s, c)
                dot(this, 27f, 55f, 3f, s, hi); dot(this, 44f, 38f, 2.4f, s, hi)
            }
            2 -> {
                // 干硬块：漩涡 + 颗粒
                val body = miniSwirl(s)
                drawPath(body, c)
                dot(this, 40f, 50f, 1.9f, s, dark); dot(this, 58f, 48f, 1.9f, s, dark)
                dot(this, 46f, 31f, 1.7f, s, dark); dot(this, 60f, 66f, 1.7f, s, dark)
                dot(this, 43f, 18f, 2f, s, hi)
            }
            3 -> {
                // 褶皱块：漩涡 + 皱褶线
                val body = miniSwirl(s)
                drawPath(body, c)
                clipPath(body) {
                    drawLine(dark, Offset(30f * s, 68f * s), Offset(70f * s, 66f * s), 2.2f * s, StrokeCap.Round)
                    drawLine(dark, Offset(34f * s, 51f * s), Offset(66f * s, 49f * s), 2f * s, StrokeCap.Round)
                    drawLine(dark, Offset(40f * s, 33f * s), Offset(60f * s, 32f * s), 1.8f * s, StrokeCap.Round)
                }
                dot(this, 43f, 18f, 2f, s, hi)
            }
            4 -> {
                // 光滑块：干净漩涡 + 高光
                drawPath(miniSwirl(s), c)
                drawOvalRect(this, 42f, 30f, 3f, 6f, s, hi)
                drawOvalRect(this, 33f, 48f, 2.5f, 5f, s, hi.copy(alpha = 0.7f))
            }
            5 -> {
                // 软块：漩涡 + 侧边融化
                drawPath(miniSwirl(s), c)
                drawOval(c, topLeft = Offset(60f * s, 68f * s), size = Size(24f * s, 16f * s))
                drawRoundRect(
                    c, topLeft = Offset(70f * s, 74f * s), size = Size(7f * s, 14f * s),
                    cornerRadius = CornerRadius(3.5f * s, 3.5f * s)
                )
                dot(this, 43f, 18f, 2f, s, hi)
            }
            6 -> {
                // 糊型块：摊平 + 下滴
                drawOvalRect(this, 50f, 62f, 32f, 12f, s, c)
                drawRoundRect(c, topLeft = Offset(29f * s, 66f * s), size = Size(7f * s, 16f * s), cornerRadius = CornerRadius(3.5f * s, 3.5f * s))
                drawRoundRect(c, topLeft = Offset(47f * s, 68f * s), size = Size(7f * s, 12f * s), cornerRadius = CornerRadius(3.5f * s, 3.5f * s))
                drawRoundRect(c, topLeft = Offset(64f * s, 66f * s), size = Size(6f * s, 10f * s), cornerRadius = CornerRadius(3f * s, 3f * s))
                ball(this, 76f, 40f, 4.5f, s, c)
                dot(this, 42f, 58f, 2.4f, s, hi)
            }
            else -> {
                // 水样状：水滴 + 水洼
                val drop = Path().apply {
                    moveTo(50f * s, 14f * s)
                    cubicTo(58f * s, 28f * s, 66f * s, 34f * s, 66f * s, 45f * s)
                    cubicTo(66f * s, 58f * s, 34f * s, 58f * s, 34f * s, 45f * s)
                    cubicTo(34f * s, 34f * s, 42f * s, 28f * s, 50f * s, 14f * s)
                    close()
                }
                drawPath(drop, c)
                dot(this, 43f, 38f, 2.6f, s, hi)
                drawOvalRect(this, 50f, 80f, 26f, 7f, s, c.copy(alpha = 0.75f))
            }
        }
    }
}

/** 迷你三层漩涡（与主角同造型语言，100×100 单位） */
private fun miniSwirl(s: Float): Path = Path().apply {
    // 底层
    moveTo(18f * s, 71f * s)
    cubicTo(18f * s, 61.5f * s, 25f * s, 56.5f * s, 33f * s, 55.5f * s)
    cubicTo(38f * s, 54.8f * s, 44f * s, 54.5f * s, 50f * s, 54.5f * s)
    cubicTo(56f * s, 54.5f * s, 62f * s, 54.8f * s, 67f * s, 55.5f * s)
    cubicTo(75f * s, 56.5f * s, 82f * s, 61.5f * s, 82f * s, 71f * s)
    cubicTo(82f * s, 76.5f * s, 68f * s, 79.5f * s, 50f * s, 79.5f * s)
    cubicTo(32f * s, 79.5f * s, 18f * s, 76.5f * s, 18f * s, 71f * s)
    close()
    // 中层
    moveTo(28f * s, 53f * s)
    cubicTo(28f * s, 44f * s, 34f * s, 39.5f * s, 41f * s, 38.2f * s)
    cubicTo(44f * s, 37.7f * s, 47f * s, 37.5f * s, 50f * s, 37.5f * s)
    cubicTo(53f * s, 37.5f * s, 56f * s, 37.7f * s, 59f * s, 38.2f * s)
    cubicTo(66f * s, 39.5f * s, 72f * s, 44f * s, 72f * s, 53f * s)
    cubicTo(72f * s, 57.5f * s, 62f * s, 60f * s, 50f * s, 60f * s)
    cubicTo(38f * s, 60f * s, 28f * s, 57.5f * s, 28f * s, 53f * s)
    close()
    // 顶层
    moveTo(36f * s, 36.5f * s)
    cubicTo(36f * s, 28.5f * s, 40.5f * s, 24f * s, 46.5f * s, 23.2f * s)
    cubicTo(48f * s, 23f * s, 52f * s, 23f * s, 53.5f * s, 23.2f * s)
    cubicTo(59.5f * s, 24f * s, 64f * s, 28.5f * s, 64f * s, 36.5f * s)
    cubicTo(64f * s, 40f * s, 57.5f * s, 42.5f * s, 50f * s, 42.5f * s)
    cubicTo(42.5f * s, 42.5f * s, 36f * s, 40f * s, 36f * s, 36.5f * s)
    close()
    // 尖
    addOval(Rect(47f * s, 13.5f * s, 56f * s, 22.5f * s))
}

private fun ball(d: DrawScope, cx: Float, cy: Float, r: Float, s: Float, color: Color) {
    d.drawCircle(color, r * s, Offset(cx * s, cy * s))
}

private fun dot(d: DrawScope, cx: Float, cy: Float, r: Float, s: Float, color: Color) {
    d.drawCircle(color, r * s, Offset(cx * s, cy * s))
}

private fun drawOvalRect(
    d: DrawScope, cx: Float, cy: Float, rx: Float, ry: Float, s: Float, color: Color
) {
    d.drawOval(
        color,
        topLeft = Offset((cx - rx) * s, (cy - ry) * s),
        size = Size(rx * 2 * s, ry * 2 * s)
    )
}
