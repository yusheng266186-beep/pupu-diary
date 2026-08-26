package com.pupu.diary.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pupu.diary.ui.theme.AppPalette
import com.pupu.diary.ui.theme.LocalAppPalette

enum class Tab(val label: String) {
    HOME("首页"), RECORD("记录"), STATS("统计"), MINE("我的")
}

/** 悬浮胶囊底部导航，选中图标弹跳 */
@Composable
fun BottomBar(current: Tab, onSelect: (Tab) -> Unit) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            shadowElevation = 14.dp,
            color = palette.card
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 6.dp)
            ) {
                Tab.entries.forEach { tab ->
                    val selected = tab == current
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1f else 0.92f,
                        animationSpec = spring(
                            dampingRatio = 0.5f,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "tabScale"
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelect(tab)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
                            NavIcon(tab, selected, palette)
                        }
                        Spacer(Modifier.width(3.dp))
                        Text(
                            tab.label,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) palette.primaryDeep else palette.textSub
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavIcon(tab: Tab, selected: Boolean, palette: AppPalette) {
    val color = if (selected) palette.primary else palette.textSub
    Canvas(Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(w * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (tab) {
            Tab.HOME -> {
                val roof = Path().apply {
                    moveTo(w * 0.08f, h * 0.5f)
                    lineTo(w * 0.5f, h * 0.12f)
                    lineTo(w * 0.92f, h * 0.5f)
                }
                drawPath(roof, color, style = stroke)
                val body = Path().apply {
                    moveTo(w * 0.2f, h * 0.48f)
                    lineTo(w * 0.8f, h * 0.48f)
                    lineTo(w * 0.8f, h * 0.86f)
                    lineTo(w * 0.2f, h * 0.86f)
                    close()
                }
                if (selected) {
                    drawPath(body, color, style = Fill)
                } else {
                    drawPath(body, color, style = stroke)
                }
            }
            Tab.RECORD -> {
                // 对勾方框（打卡记录）
                val box = Path().apply {
                    moveTo(w * 0.16f, h * 0.16f)
                    lineTo(w * 0.84f, h * 0.16f)
                    lineTo(w * 0.84f, h * 0.84f)
                    lineTo(w * 0.16f, h * 0.84f)
                    close()
                }
                drawPath(box, color, style = stroke)
                if (selected) {
                    val check = Path().apply {
                        moveTo(w * 0.3f, h * 0.51f)
                        lineTo(w * 0.45f, h * 0.66f)
                        lineTo(w * 0.72f, h * 0.35f)
                    }
                    drawPath(
                        check, color,
                        style = Stroke(w * 0.14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                } else {
                    drawCircle(color, radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.5f))
                }
            }
            Tab.STATS -> {
                listOf(0.38f, 0.62f, 0.88f).forEachIndexed { i, hh ->
                    val bw = w * 0.17f
                    val x = w * (0.12f + i * 0.28f)
                    drawRoundRect(
                        color,
                        topLeft = Offset(x, h * (0.9f - hh)),
                        size = Size(bw, h * hh),
                        cornerRadius = CornerRadius(bw / 2f, bw / 2f)
                    )
                }
            }
            Tab.MINE -> {
                drawCircle(
                    color, radius = w * 0.2f,
                    center = Offset(w * 0.5f, h * 0.32f),
                    style = if (selected) Fill else stroke
                )
                val body = Path().apply {
                    moveTo(w * 0.16f, h * 0.88f)
                    cubicTo(w * 0.16f, h * 0.6f, w * 0.84f, h * 0.6f, w * 0.84f, h * 0.88f)
                }
                drawPath(
                    body, color,
                    style = if (selected) {
                        Stroke(w * 0.16f, cap = StrokeCap.Round)
                    } else {
                        stroke
                    }
                )
            }
        }
    }
}
