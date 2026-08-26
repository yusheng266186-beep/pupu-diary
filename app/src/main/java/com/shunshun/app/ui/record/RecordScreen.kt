package com.shunshun.app.ui.record

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shunshun.app.data.PoopRecord
import com.shunshun.app.ui.BlueMist
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
import com.shunshun.app.ui.SageLight
import com.shunshun.app.ui.Surface
import com.shunshun.app.ui.Yellow
import com.shunshun.app.ui.components.AppHaptic
import com.shunshun.app.ui.components.EmptyState
import com.shunshun.app.ui.components.HandDrawnDialog
import com.shunshun.app.ui.components.HistoryCard
import com.shunshun.app.ui.components.MascotMood
import com.shunshun.app.ui.components.MoodSelector
import com.shunshun.app.ui.components.PrimaryButton
import com.shunshun.app.ui.components.SectionCard
import com.shunshun.app.ui.components.SegmentedControl
import com.shunshun.app.ui.components.SoftnessSlider
import com.shunshun.app.ui.components.StoolIcon
import com.shunshun.app.ui.components.SuccessOverlay
import com.shunshun.app.ui.components.dayKey
import com.shunshun.app.ui.components.colorName
import com.shunshun.app.ui.components.appClickable
import com.shunshun.app.ui.components.formatDate
import com.shunshun.app.ui.components.rememberAppHaptics
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun RecordScreen(records: List<PoopRecord>, onSaved: (PoopRecord) -> Unit, onDelete: (PoopRecord) -> Unit, onUpdate: (PoopRecord) -> Unit) {
    var mode by rememberSaveable { mutableStateOf("记录一次") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Spacer(Modifier.height(21.dp))
            Text("记录便便", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
            Text("花几秒钟，听听身体的声音", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(12.dp))
            SegmentedControl(mode, listOf("记录一次", "历史记录")) { mode = it }
        }
        if (mode == "历史记录") item { HistoryList(records, onDelete, onUpdate) } else item { RecordForm(onSaved) }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun RecordForm(onSaved: (PoopRecord) -> Unit) {
    var stool by rememberSaveable { mutableIntStateOf(0) }
    var colorType by rememberSaveable { mutableIntStateOf(-1) }
    var mood by rememberSaveable { mutableIntStateOf(3) }
    var smoothness by rememberSaveable { mutableIntStateOf(72) }
    var duration by rememberSaveable { mutableIntStateOf(6) }
    var abdominalPain by rememberSaveable { mutableStateOf(false) }
    var straining by rememberSaveable { mutableStateOf(false) }
    var incomplete by rememberSaveable { mutableStateOf(false) }
    var note by rememberSaveable { mutableStateOf("") }
    var more by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf(false) }
    var saved by rememberSaveable { mutableStateOf(false) }
    var pending by remember { mutableStateOf<PoopRecord?>(null) }
    val haptics = rememberAppHaptics()

    LaunchedEffect(pending) {
        pending?.let {
            kotlinx.coroutines.delay(1_150)
            onSaved(it)
        }
    }
    val offset by animateFloatAsState(if (error) 5f else 0f, tween(90), label = "error-shake")
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard("便便形状", Modifier.offset(x = offset.dp)) {
            val names = listOf("硬球状", "偏硬香肠", "表面裂纹", "光滑柔软", "软块状", "糊状", "水样")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                (1..7).chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { type ->
                            val chosen = type == stool
                            val scale by animateFloatAsState(if (chosen) 1.04f else 1f, spring(dampingRatio = .65f), label = "type-$type")
                            Column(
                                Modifier.weight(1f).scale(scale).height(106.dp)
                                    .background(if (chosen) SageLight.copy(alpha = .45f) else Surface, RoundedCornerShape(18.dp))
                                    .border(1.dp, if (chosen) Sage else Line, RoundedCornerShape(18.dp))
                                    .appClickable {
                                        stool = type
                                        error = false
                                        haptics(AppHaptic.Selection)
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                StoolIcon(type, 43.dp)
                                Text("${type}型", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(names[type - 1], color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
        SectionCard("便便颜色（可选）") {
            ColorSelector(colorType.takeIf { it >= 0 }) { colorType = it ?: -1 }
            Text("只做生活日记，不用于健康判断", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 9.dp))
        }
        SectionCard("今天的感觉") { MoodSelector(mood) { mood = it } }
        SectionCard("顺畅度  $smoothness%") { SoftnessSlider(smoothness) { smoothness = it } }
        Row(Modifier.fillMaxWidth().appClickable { more = !more }.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("◌", color = SageDark, fontSize = 22.sp)
            Spacer(Modifier.width(8.dp))
            Text(if (more) "收起更多记录" else "更多记录", color = SageDark, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Text(if (more) "⌃" else "⌄", color = SageDark, fontSize = 20.sp)
        }
        AnimatedVisibility(more) {
            SectionCard("补充记录") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DurationRow(duration) { duration = it }
                    ToggleRow(Icons.Outlined.Info, "肚肚不舒服", abdominalPain) { abdominalPain = !abdominalPain }
                    ToggleRow(Icons.Outlined.Info, "今天有点费力", straining) { straining = !straining }
                    ToggleRow(Icons.Outlined.Info, "感觉没有排干净", incomplete) { incomplete = !incomplete }
                    NoteRow(note) { note = it }
                }
            }
        }
        PrimaryButton(if (saved) "✓ 记录成功" else "保存记录") {
            if (pending != null) return@PrimaryButton
            if (stool == 0) {
                error = true
                haptics(AppHaptic.Warning)
            } else {
                pending = PoopRecord(stoolType = stool, colorType = colorType.takeIf { it >= 0 }, mood = mood, smoothness = smoothness, durationMinutes = duration, abdominalPain = abdominalPain, straining = straining, incompleteFeeling = incomplete, note = note.trim())
                saved = true
                haptics(AppHaptic.Success)
            }
        }
        if (error) Text("先选择一下便便类型吧～", color = Peach, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        AnimatedVisibility(saved) { SuccessOverlay() }
    }
}

private data class ColorChoice(val index: Int, val label: String, val color: Color)

private val colorChoices = listOf(
    ColorChoice(-1, "不记录", Surface),
    ColorChoice(0, "深棕", Color(0xFF80523F)),
    ColorChoice(1, "棕色", Brown),
    ColorChoice(2, "黄褐", Color(0xFFC18B4A)),
    ColorChoice(3, "浅黄", Color(0xFFE4C678)),
    ColorChoice(4, "红棕", Color(0xFFB86F5A)),
    ColorChoice(5, "其他", Color(0xFFD6BCA5))
)

@Composable
private fun ColorSelector(selected: Int?, onSelect: (Int?) -> Unit) {
    val haptics = rememberAppHaptics()
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        colorChoices.forEach { choice ->
            val chosen = selected == choice.index || (choice.index == -1 && selected == null)
            Column(
                Modifier.width(62.dp).appClickable {
                    onSelect(choice.index.takeIf { it >= 0 })
                    haptics(AppHaptic.Selection)
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(43.dp)
                        .background(choice.color.copy(alpha = if (choice.index == -1) .34f else .62f), CircleShape)
                        .border(1.5.dp, if (chosen) SageDark else Line, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (choice.index == -1) Text("—", color = Muted, fontSize = 17.sp)
                    else Box(Modifier.size(18.dp).background(choice.color, CircleShape))
                }
                Text(choice.label, color = if (chosen) SageDark else Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
            }
        }
    }
}

@Composable
private fun DurationRow(value: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.AccessTime, null, tint = SageDark, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text("排便时长", color = Ink, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        StepperButton(Icons.Outlined.Remove, enabled = value > 1) { onChange((value - 1).coerceAtLeast(1)) }
        Text("$value 分钟", color = SageDark, fontSize = 13.sp, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
        StepperButton(Icons.Outlined.Add, enabled = value < 60) { onChange((value + 1).coerceAtMost(60)) }
    }
}

@Composable
private fun StepperButton(icon: androidx.compose.ui.graphics.vector.ImageVector, enabled: Boolean, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).appClickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) {
        Box(Modifier.size(34.dp).background(if (enabled) Meadow else Line.copy(alpha = .45f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (enabled) SageDark else Muted, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun ToggleRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, checked: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().appClickable(onClick = onClick).padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = SageDark, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, color = Ink, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(23.dp).background(if (checked) Sage else Color.Transparent, CircleShape).border(1.5.dp, if (checked) Sage else Line, CircleShape), contentAlignment = Alignment.Center) {
            if (checked) Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NoteRow(value: String, onChange: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Outlined.Edit, null, tint = SageDark, modifier = Modifier.size(20.dp).padding(top = 3.dp))
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = { onChange(it.take(120)) },
            modifier = Modifier.weight(1f).height(62.dp).background(Cream, RoundedCornerShape(14.dp)).border(1.dp, Line, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 13.sp, lineHeight = 19.sp),
            decorationBox = { field -> if (value.isEmpty()) Text("写下一点今天的感受（可选）", color = Muted, fontSize = 13.sp); field() }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HistoryList(records: List<PoopRecord>, onDelete: (PoopRecord) -> Unit, onUpdate: (PoopRecord) -> Unit) {
    val latest = records.firstOrNull()?.timestamp ?: System.currentTimeMillis()
    var selectedDay by rememberSaveable { mutableStateOf(if (records.isEmpty()) null else dayKey(latest)) }
    var detail by remember { mutableStateOf<PoopRecord?>(null) }
    var editing by remember { mutableStateOf<PoopRecord?>(null) }
    var confirmDelete by remember { mutableStateOf<PoopRecord?>(null) }
    val recordDayKeys = records.asSequence().map { dayKey(it.timestamp) }.toSet()
    val dateKeys = remember(recordDayKeys) {
        val base = Calendar.getInstance()
        val recentDays = (0..89).map { offset ->
            dayKey((base.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }.timeInMillis)
        }
        (recentDays + recordDayKeys).distinct().sortedDescending()
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DayChip("全部", null, selectedDay == null, records.any()) { selectedDay = null }
            dateKeys.forEach { key ->
                DayChip(SimpleDateFormat("E", Locale.CHINA).format(parseDay(key)), key, selectedDay == key, key in recordDayKeys) { selectedDay = key }
            }
        }
        Text("左右滑动查看更早日期", color = Muted.copy(alpha = .78f), fontSize = 10.sp, modifier = Modifier.padding(start = 3.dp))
        val filtered = selectedDay?.let { key -> records.filter { dayKey(it.timestamp) == key } } ?: records
        Text(if (selectedDay == null) "全部记录" else "${formatDayTitle(selectedDay!!)}的记录", color = Muted, fontSize = 13.sp)
        if (filtered.isEmpty()) EmptyState("这里还空空的～", "完成第一次记录后，它就会住进这里。", MascotMood.Sleepy)
        else filtered.forEach { record -> HistoryCard(record, onDelete) { detail = record } }
    }
    val selected = detail
    if (selected != null) {
        ModalBottomSheet(
            onDismissRequest = { detail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Cream,
            dragHandle = {
                Box(
                    Modifier.padding(top = 9.dp).size(width = 38.dp, height = 4.dp)
                        .background(SageLight.copy(alpha = .92f), RoundedCornerShape(4.dp))
                )
            }
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(72.dp).background(Meadow, CircleShape), contentAlignment = Alignment.Center) { StoolIcon(selected.stoolType, 48.dp) }
                    Spacer(Modifier.width(15.dp))
                    Column {
                        Text("记录详情", color = BrownDark, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                        Text(formatDate(selected.timestamp), color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                DetailLine("便便形状", "${selected.stoolType}型")
                selected.colorType?.let { DetailLine("颜色", colorName(it)) }
                DetailLine("今天的感觉", com.shunshun.app.ui.components.moodName(selected.mood))
                DetailLine("顺畅度", "${selected.smoothness}%")
                selected.durationMinutes?.let { DetailLine("排便时长", "$it 分钟") }
                if (selected.abdominalPain || selected.straining || selected.incompleteFeeling) {
                    val notes = buildList { if (selected.abdominalPain) add("肚肚不舒服"); if (selected.straining) add("今天有点费力"); if (selected.incompleteFeeling) add("感觉没有排干净") }
                    DetailLine("身体备注", notes.joinToString("、"))
                }
                if (selected.note.isNotBlank()) DetailLine("随手记", selected.note)
                PrimaryButton("编辑这条记录") { editing = selected; detail = null }
                Text("删除这条记录", color = Peach, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().appClickable { confirmDelete = selected; detail = null }.padding(vertical = 11.dp))
                Spacer(Modifier.height(14.dp))
            }
        }
    }
    editing?.let { record -> EditRecordDialog(record, onDismiss = { editing = null }) { updated -> onUpdate(updated); editing = null } }
    confirmDelete?.let { record ->
        HandDrawnDialog(
            title = "确定删除这条记录吗？",
            message = "删除后无法恢复。",
            confirmLabel = "删除",
            confirmColor = Peach,
            onDismiss = { confirmDelete = null },
            onConfirm = { onDelete(record); confirmDelete = null }
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, color = Muted, fontSize = 13.sp, modifier = Modifier.width(84.dp))
        Text(value, color = Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EditRecordDialog(record: PoopRecord, onDismiss: () -> Unit, onSave: (PoopRecord) -> Unit) {
    var stool by remember(record.id) { mutableIntStateOf(record.stoolType) }
    var colorType by remember(record.id) { mutableIntStateOf(record.colorType ?: -1) }
    var mood by remember(record.id) { mutableIntStateOf(record.mood) }
    var smoothness by remember(record.id) { mutableIntStateOf(record.smoothness) }
    var duration by remember(record.id) { mutableIntStateOf(record.durationMinutes ?: 6) }
    var abdominalPain by remember(record.id) { mutableStateOf(record.abdominalPain) }
    var straining by remember(record.id) { mutableStateOf(record.straining) }
    var incomplete by remember(record.id) { mutableStateOf(record.incompleteFeeling) }
    var note by remember(record.id) { mutableStateOf(record.note) }
    val haptics = rememberAppHaptics()
    HandDrawnDialog(
        title = "编辑记录",
        onDismiss = onDismiss,
        confirmLabel = "保存",
        onConfirm = { onSave(record.copy(stoolType = stool, colorType = colorType.takeIf { it >= 0 }, mood = mood, smoothness = smoothness, durationMinutes = duration, abdominalPain = abdominalPain, straining = straining, incompleteFeeling = incomplete, note = note.trim())) }
    ) {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("便便形状", color = Muted, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..7).forEach { type ->
                        Box(Modifier.size(50.dp).background(if (stool == type) SageLight else Surface, RoundedCornerShape(14.dp)).border(1.dp, if (stool == type) Sage else Line, RoundedCornerShape(14.dp)).appClickable { stool = type; haptics(AppHaptic.Selection) }, contentAlignment = Alignment.Center) { StoolIcon(type, 32.dp) }
                    }
                }
                Text("便便颜色", color = Muted, fontSize = 12.sp)
                ColorSelector(colorType.takeIf { it >= 0 }) { colorType = it ?: -1 }
                Text("今天的感觉", color = Muted, fontSize = 12.sp)
                MoodSelector(mood) { mood = it }
                Text("顺畅度  $smoothness%", color = Muted, fontSize = 12.sp)
                SoftnessSlider(smoothness) { smoothness = it }
                DurationRow(duration) { duration = it }
                ToggleRow(Icons.Outlined.Info, "肚肚不舒服", abdominalPain) { abdominalPain = !abdominalPain }
                ToggleRow(Icons.Outlined.Info, "今天有点费力", straining) { straining = !straining }
                ToggleRow(Icons.Outlined.Info, "感觉没有排干净", incomplete) { incomplete = !incomplete }
                NoteRow(note) { note = it }
            }
    }
}

@Composable
private fun DayChip(label: String, key: String?, selected: Boolean, hasData: Boolean, onClick: () -> Unit) {
    Column(Modifier.width(if (key == null) 45.dp else 38.dp).appClickable(onClick = onClick).padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(if (selected) 31.dp else 29.dp).background(if (selected) Sage else if (hasData) SageLight.copy(alpha = .55f) else Surface, CircleShape), contentAlignment = Alignment.Center) {
            Text(if (key == null) label.take(1) else SimpleDateFormat("d", Locale.CHINA).format(parseDay(key)), color = if (selected) Color.White else SageDark, fontSize = 11.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
        Text(if (key == null) "记录" else label, color = if (selected) SageDark else Muted, fontSize = 9.sp, modifier = Modifier.padding(top = 3.dp))
        Box(Modifier.padding(top = 2.dp).size(4.dp).background(if (hasData) Sage else Color.Transparent, CircleShape))
    }
}

private fun parseDay(key: String): java.util.Date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(key) ?: java.util.Date()
private fun formatDayTitle(key: String): String = SimpleDateFormat("MM月dd日", Locale.CHINA).format(parseDay(key))
