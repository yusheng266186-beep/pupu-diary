package com.shunshun.app.ui.record

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shunshun.app.data.PoopRecord
import com.shunshun.app.ui.BrownDark
import com.shunshun.app.ui.Cream
import com.shunshun.app.ui.Ink
import com.shunshun.app.ui.Line
import com.shunshun.app.ui.Muted
import com.shunshun.app.ui.Peach
import com.shunshun.app.ui.Sage
import com.shunshun.app.ui.SageDark
import com.shunshun.app.ui.SageLight
import com.shunshun.app.ui.Surface
import com.shunshun.app.ui.components.EmptyState
import com.shunshun.app.ui.components.HistoryCard
import com.shunshun.app.ui.components.MascotMood
import com.shunshun.app.ui.components.MoodSelector
import com.shunshun.app.ui.components.OptionRow
import com.shunshun.app.ui.components.PrimaryButton
import com.shunshun.app.ui.components.SectionCard
import com.shunshun.app.ui.components.SegmentedControl
import com.shunshun.app.ui.components.SoftnessSlider
import com.shunshun.app.ui.components.StoolIcon
import com.shunshun.app.ui.components.SuccessOverlay

@Composable
fun RecordScreen(records: List<PoopRecord>, onSaved: (PoopRecord) -> Unit, onDelete: (PoopRecord) -> Unit) {
    var mode by rememberSaveable { mutableStateOf("记录一次") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Spacer(Modifier.height(21.dp)); androidx.compose.material3.Text("记录便便", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold); androidx.compose.material3.Text("花几秒钟，听听身体的声音", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)); Spacer(Modifier.height(12.dp)); SegmentedControl(mode, listOf("记录一次", "历史记录")) { mode = it } }
        if (mode == "历史记录") item { HistoryList(records, onDelete) } else item { RecordForm(onSaved) }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun RecordForm(onSaved: (PoopRecord) -> Unit) {
    var stool by rememberSaveable { mutableIntStateOf(0) }; var mood by rememberSaveable { mutableIntStateOf(3) }; var smoothness by rememberSaveable { mutableIntStateOf(72) }; var more by rememberSaveable { mutableStateOf(false) }; var error by rememberSaveable { mutableStateOf(false) }; var saved by rememberSaveable { mutableStateOf(false) }; var pending by remember { mutableStateOf<PoopRecord?>(null) }
    LaunchedEffect(pending) { pending?.let { kotlinx.coroutines.delay(1_100); onSaved(it) } }
    val offset by animateFloatAsState(if (error) 5f else 0f, tween(90), label = "error-shake")
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard("便便形状", Modifier.offset(x = offset.dp)) {
            val names = listOf("硬球状", "偏硬香肠", "表面裂纹", "光滑柔软", "软块状", "糊状", "水样")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { (1..7).chunked(3).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { type -> val chosen = type == stool; val scale by animateFloatAsState(if (chosen) 1.04f else 1f, spring(dampingRatio = .65f), label = "type-$type"); Column(Modifier.weight(1f).scale(scale).height(106.dp).background(if (chosen) SageLight.copy(alpha = .45f) else Surface, RoundedCornerShape(18.dp)).border(1.dp, if (chosen) Sage else Line, RoundedCornerShape(18.dp)).clickable { stool = type; error = false }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { StoolIcon(type, 43.dp); androidx.compose.material3.Text("${type}型", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium); androidx.compose.material3.Text(names[type - 1], color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp)) } }; repeat(3 - row.size) { Spacer(Modifier.weight(1f)) } } } }
        }
        SectionCard("今天的感觉") { MoodSelector(mood) { mood = it } }
        SectionCard("顺畅度  $smoothness%") { SoftnessSlider(smoothness) { smoothness = it } }
        Row(Modifier.fillMaxWidth().clickable { more = !more }.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { androidx.compose.material3.Text("◌", color = SageDark, fontSize = 22.sp); Spacer(Modifier.width(8.dp)); androidx.compose.material3.Text(if (more) "收起更多记录" else "更多记录", color = SageDark, fontSize = 14.sp); Spacer(Modifier.weight(1f)); androidx.compose.material3.Text(if (more) "⌃" else "⌄", color = SageDark, fontSize = 20.sp) }
        AnimatedVisibility(more) { SectionCard("补充记录") { Column(verticalArrangement = Arrangement.spacedBy(13.dp)) { OptionRow(androidx.compose.material.icons.Icons.Outlined.AccessTime, "排便时长", "6 分钟"); OptionRow(androidx.compose.material.icons.Icons.Outlined.Info, "肚肚状态", "没有不舒服"); OptionRow(androidx.compose.material.icons.Icons.Outlined.Edit, "备注", "添加一点文字") } } }
        PrimaryButton(if (saved) "✓ 记录成功" else "保存记录") { if (stool == 0) error = true else { pending = PoopRecord(stoolType = stool, mood = mood, smoothness = smoothness, durationMinutes = 6); saved = true } }
        if (error) androidx.compose.material3.Text("先选择一下便便类型吧～", color = Peach, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        AnimatedVisibility(saved) { SuccessOverlay() }
    }
}

@Composable
private fun HistoryList(records: List<PoopRecord>, onDelete: (PoopRecord) -> Unit) { if (records.isEmpty()) EmptyState("这里还空空的～", "完成第一次记录后，它就会住进这里。", MascotMood.Sleepy) else Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { androidx.compose.material3.Text("最近记录", color = Muted, fontSize = 13.sp); records.forEach { HistoryCard(it, onDelete) } } }
