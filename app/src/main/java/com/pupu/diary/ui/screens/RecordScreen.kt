package com.pupu.diary.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pupu.diary.data.Bristol
import com.pupu.diary.data.PoopColors
import com.pupu.diary.data.PoopRecord
import com.pupu.diary.ui.AppState
import com.pupu.diary.ui.components.BristolIcon
import com.pupu.diary.ui.components.BackHeader
import com.pupu.diary.ui.components.ConfettiBurst
import com.pupu.diary.ui.components.MoodFace
import com.pupu.diary.ui.components.PoopCharacter
import com.pupu.diary.ui.components.PrimaryButton
import com.pupu.diary.ui.components.PupuDialog
import com.pupu.diary.ui.components.SectionTitle
import com.pupu.diary.ui.components.SoftCard
import com.pupu.diary.ui.theme.LocalAppPalette
import com.pupu.diary.util.formatFullCn
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

@Composable
fun RecordScreen(state: AppState, onDone: () -> Unit, edit: PoopRecord? = null) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    var bristol by remember { mutableStateOf<Int?>(edit?.bristol) }
    var mood by remember { mutableIntStateOf(edit?.mood ?: 1) }
    var smooth by remember { mutableFloatStateOf(edit?.smoothness?.toFloat() ?: 70f) }
    var note by remember { mutableStateOf(edit?.note ?: "") }
    var duration by remember { mutableIntStateOf(edit?.durationMin ?: 0) }
    var colorType by remember { mutableStateOf(edit?.colorType) }
    var pain by remember { mutableStateOf(edit?.abdominalPain ?: false) }
    var straining by remember { mutableStateOf(edit?.straining ?: false) }
    var incomplete by remember { mutableStateOf(edit?.incomplete ?: false) }
    var timeMs by remember { mutableLongStateOf(edit?.time ?: System.currentTimeMillis()) }
    var useNow by remember { mutableStateOf(edit == null) }
    var showTimePick by remember { mutableStateOf(false) }
    var moreOpen by remember {
        mutableStateOf(
            edit != null && (
                edit.note.isNotBlank() || edit.durationMin > 0 ||
                    edit.colorType != null || edit.abdominalPain ||
                    edit.straining || edit.incomplete
                )
        )
    }
    var saveTrigger by remember { mutableIntStateOf(0) }
    var shakeTrigger by remember { mutableIntStateOf(0) }
    var triedSave by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    // 未选形状时点保存：表单左右抖动提醒（按 dp 换算，保证不同密度下幅度一致）
    val density = LocalDensity.current
    val shake = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            val unit = with(density) { 10.dp.toPx() }
            listOf(-1f, 1f, -0.65f, 0.65f, -0.3f, 0f).forEach { f ->
                shake.animateTo(f * unit, tween(55))
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            BackHeader(if (edit != null) "编辑记录" else "记录便便", onBack = onDone)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionTitle("便形状")
                Spacer(Modifier.height(10.dp))
                Column(
                    Modifier.offset { IntOffset(shake.value.roundToInt(), 0) }
                ) {
                    Bristol.labels.indices.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth()) {
                            row.forEach { i ->
                                BristolCard(
                                    type = i + 1,
                                    selected = bristol == i + 1,
                                    modifier = Modifier.weight(1f),
                                    onClick = { bristol = i + 1 }
                                )
                                if (i % 3 != 2) Spacer(Modifier.width(10.dp))
                            }
                            repeat(3 - row.size) {
                                Spacer(Modifier.weight(1f))
                                Spacer(Modifier.width(10.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                AnimatedVisibility(visible = bristol != null) {
                    Text(
                        Bristol.hint(bristol ?: 4),
                        fontSize = 12.sp,
                        color = palette.primaryDeep,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                SectionTitle("心情")
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (0..4).forEach { m ->
                        MoodFace(mood = m, selected = mood == m, onClick = { mood = m })
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("顺滑度")
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${smooth.toInt()}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryDeep
                    )
                }
                SmoothSlider(value = smooth, onChange = { smooth = it })
                Row(Modifier.fillMaxWidth()) {
                    Text("粗糙", fontSize = 11.sp, color = palette.textSub)
                    Spacer(Modifier.weight(1f))
                    Text("顺滑", fontSize = 11.sp, color = palette.textSub)
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("记录时间")
                    Spacer(Modifier.weight(1f))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(palette.primarySoft)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showTimePick = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (useNow) "现在" else formatFullCn(timeMs),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.primaryDeep
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))

                // 更多记录：排便时长 + 备注（默认收起）
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { moreOpen = !moreOpen }
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("更多记录")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (moreOpen) "收起" else "颜色 · 状态 · 时长 · 备注",
                        fontSize = 12.sp,
                        color = palette.primaryDeep
                    )
                    Spacer(Modifier.weight(1f))
                    MoreChevron(open = moreOpen)
                }
                AnimatedVisibility(visible = moreOpen) {
                    Column {
                        Text(
                            "排便时长（可选，再点一下取消）",
                            fontSize = 11.sp,
                            color = palette.textSub,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        // 窄屏放不下 5 个芯片时横向滑动
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            listOf(1, 3, 5, 10, 15).forEachIndexed { idx, min ->
                                DurationChip(
                                    text = "$min 分钟",
                                    selected = duration == min,
                                    modifier = if (idx == 0) Modifier else Modifier.padding(start = 8.dp),
                                    onClick = { duration = if (duration == min) 0 else min }
                                )
                            }
                        }
                        // 颜色日记（可选，来自「便便晴天」的同源色板）
                        Spacer(Modifier.height(16.dp))
                        SectionTitle("颜色日记")
                        Text(
                            "今天是什么颜色？（可选）",
                            fontSize = 11.sp,
                            color = palette.textSub,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PoopColors.labels.indices.forEach { idx ->
                                ColorDot(
                                    type = idx,
                                    selected = colorType == idx,
                                    modifier = if (idx == 0) Modifier else Modifier.padding(start = 10.dp),
                                    onClick = {
                                        colorType = if (colorType == idx) null else idx
                                    }
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            DurationChip(
                                text = "不记录",
                                selected = colorType == null,
                                onClick = { colorType = null }
                            )
                        }
                        // 身体状态（可选三项）
                        Spacer(Modifier.height(16.dp))
                        SectionTitle("身体状态")
                        Text(
                            "有感觉到就点亮，没有就跳过（可选）",
                            fontSize = 11.sp,
                            color = palette.textSub,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            listOf(
                                Triple("肚肚不舒服", pain) { pain = !pain },
                                Triple("有点费力", straining) { straining = !straining },
                                Triple("没排净", incomplete) { incomplete = !incomplete }
                            ).forEachIndexed { idx, (label, value, toggle) ->
                                DurationChip(
                                    text = label,
                                    selected = value,
                                    modifier = if (idx == 0) Modifier else Modifier.padding(start = 8.dp),
                                    onClick = toggle
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        SectionTitle("备注")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = note,
                            onValueChange = { if (it.length <= 50) note = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("今天吃了什么特别的东西吗？（可选）", fontSize = 13.sp, color = palette.textSub)
                            },
                            shape = RoundedCornerShape(18.dp),
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.primary,
                                unfocusedBorderColor = palette.cardAlt,
                                focusedContainerColor = palette.card,
                                unfocusedContainerColor = palette.card,
                                cursorColor = palette.primary,
                                focusedTextColor = palette.textMain,
                                unfocusedTextColor = palette.textMain
                            )
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                AnimatedVisibility(visible = bristol == null && triedSave) {
                    Text(
                        "先选择一下便便形状吧～",
                        fontSize = 12.sp,
                        color = palette.textSub,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            PrimaryButton(
                text = if (edit != null) "保存修改" else "保存记录",
                enabled = !saving,
                onClick = {
                    val b = bristol
                    if (b == null) {
                        triedSave = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        shakeTrigger++
                    } else {
                        saving = true
                        val t = if (useNow) System.currentTimeMillis() else timeMs
                        val e = edit
                        if (e != null) {
                            state.updateRecord(
                                e.copy(
                                    bristol = b,
                                    mood = mood,
                                    smoothness = smooth.toInt(),
                                    note = note.trim(),
                                    time = t,
                                    durationMin = duration,
                                    colorType = colorType,
                                    abdominalPain = pain,
                                    straining = straining,
                                    incomplete = incomplete
                                )
                            )
                        } else {
                            state.addRecord(
                                b, mood, smooth.toInt(), note.trim(), t, duration,
                                colorType, pain, straining, incomplete
                            )
                        }
                        saved = true
                        saveTrigger++
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
        }
        ConfettiBurst(trigger = saveTrigger, onFinished = { onDone() })
        AnimatedVisibility(
            visible = saved,
            enter = fadeIn(tween(160)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = 380f)
            ),
            exit = fadeOut(tween(120))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                SoftCard(corner = 26.dp) {
                    Column(
                        Modifier.padding(horizontal = 30.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PoopCharacter(size = 74.dp, celebrate = true, breathing = false)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (edit != null) "修改已保存！" else "记录成功！",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.primaryDeep
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("今天也有认真照顾身体～", fontSize = 12.sp, color = palette.textSub)
                    }
                }
            }
        }
    }

    if (showTimePick) {
        val zone = ZoneId.systemDefault()
        fun at(h: Int, m: Int, minusDays: Long = 0): Long =
            LocalDate.now().minusDays(minusDays).atTime(h, m)
                .atZone(zone).toInstant().toEpochMilli()
                .coerceAtMost(System.currentTimeMillis())

        PupuDialog(
            onDismissRequest = { showTimePick = false },
            title = "什么时候的记录？",
            content = {
                Column {
                    Row {
                        TimeChip("现在") { useNow = true; showTimePick = false }
                        Spacer(Modifier.width(8.dp))
                        TimeChip("1 小时前") {
                            timeMs = System.currentTimeMillis() - 3_600_000L
                            useNow = false; showTimePick = false
                        }
                        Spacer(Modifier.width(8.dp))
                        TimeChip("3 小时前") {
                            timeMs = System.currentTimeMillis() - 3 * 3_600_000L
                            useNow = false; showTimePick = false
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        TimeChip("今天 08:00") { timeMs = at(8, 0); useNow = false; showTimePick = false }
                        Spacer(Modifier.width(8.dp))
                        TimeChip("昨天 20:00") { timeMs = at(20, 0, 1); useNow = false; showTimePick = false }
                    }
                }
            },
            confirmText = "关闭"
        )
    }
}

@Composable
private fun MoreChevron(open: Boolean) {
    val palette = LocalAppPalette.current
    val rotation by animateFloatAsState(
        targetValue = if (open) 90f else 0f,
        animationSpec = tween(220),
        label = "moreChevron"
    )
    Canvas(
        Modifier
            .size(14.dp)
            .graphicsLayer { rotationZ = rotation }
    ) {
        val p = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 0.3f, size.height * 0.2f)
            lineTo(size.width * 0.7f, size.height * 0.5f)
            lineTo(size.width * 0.3f, size.height * 0.8f)
        }
        drawPath(
            p, palette.textSub,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                size.width * 0.18f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

@Composable
private fun DurationChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) palette.primarySoft else palette.card)
            .then(
                if (selected) {
                    Modifier.border(1.5.dp, palette.primary, RoundedCornerShape(50))
                } else {
                    Modifier.border(1.dp, palette.cardAlt, RoundedCornerShape(50))
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) palette.primaryDeep else palette.textSub
        )
    }
}

/** 颜色日记的圆点：外圈浅底 + 选中时主色描边 */
@Composable
private fun ColorDot(
    type: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    Column(
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val scale by animateFloatAsState(
            targetValue = if (selected) 1.15f else 1f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = 420f),
            label = "cScale"
        )
        Box(
            Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .size(34.dp)
                .clip(CircleShape)
                .background(palette.card)
                .border(
                    if (selected) 2.dp else 1.dp,
                    if (selected) palette.primary else palette.cardAlt,
                    CircleShape
                )
                .padding(5.dp)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(PoopColors.color(type))
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            PoopColors.label(type),
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) palette.primaryDeep else palette.textSub
        )
    }
}

@Composable
private fun TimeChip(text: String, onClick: () -> Unit) {
    val palette = LocalAppPalette.current
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.primarySoft)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryDeep)
    }
}

@Composable
private fun BristolCard(
    type: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 420f),
        label = "bScale"
    )
    Column(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) palette.primarySoft else palette.card)
            .then(
                if (selected) {
                    Modifier.border(2.dp, palette.primary, RoundedCornerShape(18.dp))
                } else {
                    Modifier.border(1.dp, palette.cardAlt, RoundedCornerShape(18.dp))
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BristolIcon(type = type, size = 40.dp)
        Spacer(Modifier.height(6.dp))
        Text(
            "$type ${Bristol.label(type)}",
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) palette.primaryDeep else palette.textMain
        )
    }
}

/** 顺滑度滑杆：渐变轨道 + 便便拇指（拖动时挤压变形，每跨一档轻触感） */
@Composable
private fun SmoothSlider(value: Float, onChange: (Float) -> Unit) {
    val palette = LocalAppPalette.current
    val haptic = LocalHapticFeedback.current
    var lastTick by remember { mutableIntStateOf(-1) }
    fun emit(v: Float) {
        onChange(v)
        val step = (v / 12.5f).toInt()
        if (step != lastTick) {
            lastTick = step
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    var widthPx by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val squishY by animateFloatAsState(
        targetValue = if (dragging) 0.78f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "sy"
    )
    val squishX by animateFloatAsState(
        targetValue = if (dragging) 1.18f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "sx"
    )
    val density = LocalDensity.current
    val thumbSize = 42.dp
    val frac = (value / 100f).coerceIn(0f, 1f)
    val targetX = with(density) {
        ((widthPx - thumbSize.toPx()).coerceAtLeast(0f) * frac).toDp()
    }
    val thumbX by animateDpAsState(
        targetValue = targetX,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "tx"
    )

    Box(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .onSizeChanged { widthPx = it.width.toFloat() }
            .pointerInput(Unit) {
                detectTapGestures { off ->
                    emit((off.x / widthPx).coerceIn(0f, 1f) * 100f)
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = { dragging = false },
                    onDragCancel = { dragging = false }
                ) { change, _ ->
                    change.consume()
                    emit((change.position.x / widthPx).coerceIn(0f, 1f) * 100f)
                }
            }
    ) {
        Canvas(
            Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(12.dp)
        ) {
            drawRoundRect(
                palette.cardAlt,
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            drawRoundRect(
                Brush.horizontalGradient(listOf(palette.poopLight, palette.primary)),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbX)
                .graphicsLayer {
                    scaleX = squishX
                    scaleY = squishY
                }
        ) {
            PoopCharacter(size = thumbSize, breathing = false)
        }
    }
}
