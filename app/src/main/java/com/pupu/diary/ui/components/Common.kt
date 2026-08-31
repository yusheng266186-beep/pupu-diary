package com.pupu.diary.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.pupu.diary.ui.theme.LocalAppPalette

/** 治愈风软卡片 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    color: Color? = null,
    content: @Composable () -> Unit
) {
    val palette = LocalAppPalette.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(corner),
        color = color ?: palette.card,
        shadowElevation = 3.dp
    ) {
        content()
    }
}

/** App 风格统一弹窗：奶油卡片 + 圆角 + 主题色按钮（替代 Material 默认样式） */
@Composable
fun PupuDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: (@Composable () -> Unit)? = null,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    confirmColor: Color? = null,
    dismissText: String? = null,
    onDismissClick: (() -> Unit)? = null
) {
    val palette = LocalAppPalette.current
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = palette.card,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textMain
            )
        },
        text = content?.let { c -> { c() } },
        confirmButton = {
            if (confirmText != null) {
                androidx.compose.material3.TextButton(onClick = { onConfirm?.invoke() }) {
                    Text(
                        confirmText,
                        fontWeight = FontWeight.SemiBold,
                        color = confirmColor ?: palette.primary
                    )
                }
            }
        },
        dismissButton = {
            if (dismissText != null) {
                androidx.compose.material3.TextButton(
                    onClick = { (onDismissClick ?: onDismissRequest).invoke() }
                ) {
                    Text(dismissText, color = palette.textSub)
                }
            }
        }
    )
}

/** 糖果按压主按钮：绿色按钮面坐在深色底边上，按下时面真的沉下去，物理感十足 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 56.dp
) {
    val palette = LocalAppPalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressY by animateDpAsState(
        targetValue = if (pressed) 4.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 700f),
        label = "pressY"
    )
    val haptic = LocalHapticFeedback.current
    val faceHeight = height - 4.dp

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer { alpha = if (enabled) 1f else 0.45f }
            .shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(50))
    ) {
        // 深色底边
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(faceHeight)
                .clip(RoundedCornerShape(50))
                .background(lerp(palette.primary, palette.primaryDeep, 0.5f))
        )
        // 按压面
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(faceHeight)
                .graphicsLayer { translationY = pressY.toPx() }
                .clip(RoundedCornerShape(50))
                .background(palette.primary)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}

/** 治愈风空状态：角色 + 标题 + 副标题 */
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    characterSize: Dp = 62.dp
) {
    val palette = LocalAppPalette.current
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PoopCharacter(size = characterSize, mood = 1)
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.textMain
        )
        Spacer(Modifier.height(3.dp))
        Text(
            subtitle,
            fontSize = 12.sp,
            color = palette.textSub,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/** 区块标题（参考稿样式：纯文字半粗体） */
@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    Text(
        title,
        modifier = modifier.padding(vertical = 4.dp),
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = palette.textMain
    )
}

/** 返回式页头（标题居中） */
@Composable
fun BackHeader(title: String, onBack: () -> Unit) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(palette.card)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(18.dp)) {
                    val w = this.size.width
                    val h = this.size.height
                    val p = Path().apply {
                        moveTo(w * 0.62f, h * 0.18f)
                        lineTo(w * 0.28f, h * 0.5f)
                        lineTo(w * 0.62f, h * 0.82f)
                    }
                    drawPath(
                        p, palette.textMain,
                        style = Stroke(width = w * 0.14f, cap = StrokeCap.Round)
                    )
                }
            }
        }
        Text(
            title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textMain,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/** 圆形小加号按钮（打卡用） */
@Composable
fun PlusButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.85f else 1f,
        spring(dampingRatio = 0.4f, stiffness = 600f),
        label = "plusScale"
    )
    Box(
        modifier = modifier
            .size(34.dp)
            .semantics { contentDescription = "打卡 +1" }
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(palette.primary)
            .clickable(interactionSource = interaction, indication = null) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(14.dp)) {
            val w = size.width
            val h = size.height
            drawLine(
                Color.White, Offset(w / 2f, h * 0.1f), Offset(w / 2f, h * 0.9f),
                strokeWidth = w * 0.18f, cap = StrokeCap.Round
            )
            drawLine(
                Color.White, Offset(w * 0.1f, h / 2f), Offset(w * 0.9f, h / 2f),
                strokeWidth = w * 0.18f, cap = StrokeCap.Round
            )
        }
    }
}

/** 删除撤销条：底部浮层。弹簧入场、快速退出（慢进快出的不对称时机），5 秒后自动消失 */
@Composable
fun UndoBar(
    visible: Boolean,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = spring(dampingRatio = 0.65f, stiffness = 380f),
            initialOffsetY = { it }
        ) + fadeIn(tween(120)),
        exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(140)),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            shadowElevation = 12.dp,
            color = palette.card
        ) {
            Row(
                Modifier.padding(start = 18.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("已删除一条记录", fontSize = 13.sp, color = palette.textSub)
                Text(
                    "撤销",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primaryDeep,
                    modifier = Modifier
                        .semantics { contentDescription = "撤销刚才的删除" }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onUndo
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}
