package com.shunshun.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.ColumnScope
import com.shunshun.app.data.PoopRecord
import com.shunshun.app.ui.Brown
import com.shunshun.app.ui.BrownDark
import com.shunshun.app.ui.Cream
import com.shunshun.app.ui.Ink
import com.shunshun.app.ui.Line
import com.shunshun.app.ui.Meadow
import com.shunshun.app.ui.Muted
import com.shunshun.app.ui.Peach
import com.shunshun.app.ui.Sage
import com.shunshun.app.ui.SageDark
import com.shunshun.app.ui.Surface

fun Modifier.appClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
}

@Composable
fun HandDrawnDialog(
    title: String,
    message: String? = null,
    confirmLabel: String = "确认",
    dismissLabel: String? = "取消",
    confirmColor: Color = SageDark,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp).background(Cream, RoundedCornerShape(28.dp))
                .border(1.dp, Line.copy(alpha = .9f), RoundedCornerShape(28.dp)).padding(21.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(33.dp).background(Meadow, CircleShape), contentAlignment = Alignment.Center) {
                    Text("✿", color = Peach, fontSize = 18.sp)
                }
                Spacer(Modifier.width(11.dp))
                Text(title, color = BrownDark, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
            if (!message.isNullOrBlank()) {
                Text(message, color = Muted, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 13.dp))
            }
            content()
            Spacer(Modifier.height(19.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (dismissLabel != null) {
                    Box(
                        Modifier.weight(1f).height(44.dp).background(Surface, RoundedCornerShape(16.dp))
                            .border(1.dp, Line, RoundedCornerShape(16.dp)).appClickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) { Text(dismissLabel, color = Muted, fontSize = 13.sp) }
                }
                Box(
                    Modifier.weight(1f).height(44.dp).background(confirmColor.copy(alpha = .16f), RoundedCornerShape(16.dp))
                        .border(1.dp, confirmColor.copy(alpha = .34f), RoundedCornerShape(16.dp)).appClickable(onClick = onConfirm),
                    contentAlignment = Alignment.Center
                ) { Text(confirmLabel, color = confirmColor, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
            }
        }
    }
}

@Composable
fun PrimaryButton(label: String, onClick: () -> Unit) {
    val source = remember { MutableInteractionSource() }; val pressed by source.collectIsPressedAsState()
    val haptics = rememberAppHaptics()
    Box(Modifier.fillMaxWidth().height(53.dp).scale(if (pressed) .96f else 1f).background(Sage, RoundedCornerShape(28.dp)).clickable(interactionSource = source, indication = null, onClick = { haptics(AppHaptic.Light); onClick() }), contentAlignment = Alignment.Center) { Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium) }
}

@Composable
fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) { Column(modifier.fillMaxWidth().background(Surface.copy(alpha = .78f), RoundedCornerShape(23.dp)).border(1.dp, Line.copy(alpha = .72f), RoundedCornerShape(23.dp)).padding(16.dp)) { Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink); Spacer(Modifier.height(11.dp)); content() } }

@Composable
fun SegmentedControl(selected: String, values: List<String>, onSelect: (String) -> Unit) { Row(Modifier.fillMaxWidth().background(Color(0xFFF1EFE6), RoundedCornerShape(15.dp)).padding(4.dp)) { values.forEach { value -> Box(Modifier.weight(1f).height(35.dp).background(if (selected == value) Surface else Color.Transparent, RoundedCornerShape(11.dp)).appClickable { onSelect(value) }, contentAlignment = Alignment.Center) { Text(value, color = if (selected == value) SageDark else Muted, fontSize = 13.sp, fontWeight = if (selected == value) FontWeight.Medium else FontWeight.Normal) } } } }

@Composable
fun SoftnessSlider(value: Int, onChange: (Int) -> Unit) {
    val haptics = rememberAppHaptics()
    var lastStep by remember { mutableIntStateOf(value / 20) }
    fun updateFromX(x: Float, width: Int) {
        val next = ((x / width.coerceAtLeast(1)) * 100).toInt().coerceIn(0, 100)
        val step = next / 20
        if (step != lastStep) { lastStep = step; haptics(AppHaptic.Light) }
        onChange(next)
    }
    Column(Modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().height(57.dp)
                .pointerInput(Unit) { detectTapGestures { point -> updateFromX(point.x, size.width) } }
                .pointerInput(Unit) { detectDragGestures { change, _ -> updateFromX(change.position.x, size.width) } }
        ) {
            Canvas(Modifier.fillMaxWidth().height(57.dp).padding(vertical = 20.dp)) {
                val y = size.height / 2f
                drawRoundRect(Color(0xFFE9E4D8), topLeft = Offset(0f, y - 4f), size = Size(size.width, 8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f))
                drawRoundRect(Sage, topLeft = Offset(0f, y - 4f), size = Size(size.width * value / 100f, 8f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f))
                val x = size.width * value / 100f
                drawCircle(Surface, 13f, Offset(x, y)); drawCircle(SageDark, 13f, Offset(x, y), style = Stroke(2.2f)); drawCircle(Brown, 4.5f, Offset(x, y))
                drawCircle(BrownDark.copy(alpha = .9f), 1.8f, Offset(x - 4f, y - 1f)); drawCircle(BrownDark.copy(alpha = .9f), 1.8f, Offset(x + 4f, y - 1f))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("费劲", color = Muted, fontSize = 12.sp); Text("顺畅", color = Muted, fontSize = 12.sp) }
    }
}

@Composable
fun MoodSelector(selected: Int, onSelect: (Int) -> Unit) { val haptics = rememberAppHaptics(); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { (0..4).forEach { MoodFace(it, selected == it) { haptics(AppHaptic.Selection); onSelect(it) } } } }

@Composable
fun HistoryCard(record: PoopRecord, onDelete: (PoopRecord) -> Unit, onClick: () -> Unit = {}) { var confirm by remember { mutableStateOf(false) }; Row(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(19.dp)).border(1.dp, Line, RoundedCornerShape(19.dp)).appClickable(onClick = onClick).padding(13.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(51.dp).background(Meadow, CircleShape), contentAlignment = Alignment.Center) { StoolIcon(record.stoolType, 34.dp) }; Spacer(Modifier.width(11.dp)); Column(Modifier.weight(1f)) { Text(formatDate(record.timestamp), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium); Text(buildString { append(record.stoolType).append("型") ; record.colorType?.let { append(" · ").append(colorName(it)) }; append(" · ").append(record.smoothness).append("% 顺畅") }, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp)); Text(moodName(record.mood), color = SageDark, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp)) }; Box(Modifier.size(48.dp).appClickable { confirm = true }, contentAlignment = Alignment.Center) { Icon(Icons.Outlined.DeleteOutline, "删除记录", tint = Muted) } }; if (confirm) HandDrawnDialog(title = "确定删除这条记录吗？", message = "删除后无法恢复。", confirmLabel = "删除", confirmColor = Peach, onDismiss = { confirm = false }, onConfirm = { onDelete(record); confirm = false }) }

@Composable
fun EmptyState(title: String, subtitle: String, mood: MascotMood) { Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) { PoopMascot(76.dp, mood); Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp)); Text(subtitle, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp)) } }

@Composable
fun SuccessOverlay() { Box(Modifier.fillMaxWidth().background(Cream.copy(alpha = .96f), RoundedCornerShape(21.dp)).padding(vertical = 17.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { PoopMascot(70.dp, MascotMood.Excited); Text("记录成功！", color = SageDark, fontSize = 17.sp, fontWeight = FontWeight.Medium); Text("今天也有认真照顾身体～", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) } } }

@Composable
fun OptionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = SageDark, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(10.dp)); Text(title, color = Muted, fontSize = 13.sp); Spacer(Modifier.weight(1f)); Text(value, color = Ink, fontSize = 13.sp) } }

@Composable
fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color = SageDark, onClick: () -> Unit = {}) { Row(Modifier.fillMaxWidth().appClickable(onClick = onClick).padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(21.dp)) }; Spacer(Modifier.width(13.dp)); Text(label, color = Ink, fontSize = 14.sp); Spacer(Modifier.weight(1f)); Text("›", color = Muted, fontSize = 23.sp) } }

fun formatTime(timestamp: Long): String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.CHINA).format(java.util.Date(timestamp))
fun formatDate(timestamp: Long): String = java.text.SimpleDateFormat("MM月dd日  HH:mm", java.util.Locale.CHINA).format(java.util.Date(timestamp))
fun dayKey(timestamp: Long): String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA).format(java.util.Date(timestamp))
fun moodName(mood: Int): String = listOf("很难受", "不舒服", "一般", "舒服", "超舒服").getOrElse(mood) { "一般" }
fun colorName(colorType: Int): String = listOf("深棕", "棕色", "黄褐", "浅黄", "红棕", "其他").getOrElse(colorType) { "未记录颜色" }
fun streak(records: List<PoopRecord>): Int {
    val days = records.map { dayKey(it.timestamp) }.toSet()
    val calendar = java.util.Calendar.getInstance()
    // A record from yesterday still belongs to an active streak when today is not over yet.
    if (!days.contains(dayKey(calendar.timeInMillis))) calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
    if (!days.contains(dayKey(calendar.timeInMillis))) return 0
    var count = 0
    while (days.contains(dayKey(calendar.timeInMillis))) {
        count++
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
    }
    return count
}
