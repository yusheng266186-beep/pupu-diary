package com.shunshun.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.shunshun.app.data.AppDatabase
import com.shunshun.app.data.PoopRecord
import com.shunshun.app.ui.Brown
import com.shunshun.app.ui.BrownDark
import com.shunshun.app.ui.Cream
import com.shunshun.app.ui.Ink
import com.shunshun.app.ui.Line
import com.shunshun.app.ui.Muted
import com.shunshun.app.ui.Peach
import com.shunshun.app.ui.Sage
import com.shunshun.app.ui.SageDark
import com.shunshun.app.ui.SageLight
import com.shunshun.app.ui.ShunShunTheme
import com.shunshun.app.ui.Surface
import com.shunshun.app.ui.Yellow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ShunShunTheme { ShunShunApp() } }
    }
}

class PoopViewModel(private val dao: com.shunshun.app.data.PoopDao) : ViewModel() {
    val records = dao.observeAll().stateIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO), SharingStarted.WhileSubscribed(5_000), emptyList())
    fun add(record: PoopRecord) = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { dao.insert(record) }
    fun addAll(records: List<PoopRecord>) = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { dao.insertAll(records) }
    fun delete(record: PoopRecord) = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { dao.delete(record) }
    fun clear() = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { dao.clearAll() }
}

class PoopViewModelFactory(private val dao: com.shunshun.app.data.PoopDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = PoopViewModel(dao) as T
}

private enum class Tab(val title: String) { HOME("首页"), RECORD("记录"), STATS("统计"), PROFILE("我的") }

@Composable
fun ShunShunApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val vm: PoopViewModel = viewModel(factory = PoopViewModelFactory(db.poopDao()))
    val records by vm.records.collectAsState()
    var tab by rememberSaveable { mutableStateOf(Tab.HOME.name) }
    val current = Tab.valueOf(tab)
    Scaffold(
        containerColor = Cream,
        snackbarHost = { SnackbarHost(SnackbarHostState()) },
        bottomBar = { BottomBar(current) { tab = it.name } }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (current) {
                Tab.HOME -> HomeScreen(records.size) { tab = Tab.RECORD.name }
                Tab.RECORD -> RecordScreen { record -> vm.add(record); tab = Tab.HOME.name }
                Tab.STATS -> StatsScreen(records)
                Tab.PROFILE -> ProfileScreen(records, vm)
            }
        }
    }
}

@Composable
private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Surface).padding(horizontal = 14.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        Tab.values().forEach { item ->
            val active = item == selected
            val icon = when (item) { Tab.HOME -> Icons.Outlined.Home; Tab.RECORD -> Icons.Outlined.NoteAlt; Tab.STATS -> Icons.Outlined.InsertChart; Tab.PROFILE -> Icons.Outlined.PersonOutline }
            Column(Modifier.width(72.dp).clickable { onSelect(item) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(42.dp).background(if (active) SageLight.copy(alpha = .55f) else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, item.title, tint = if (active) SageDark else Muted) }
                Text(item.title, fontSize = 11.sp, color = if (active) SageDark else Muted, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun HomeScreen(todayCount: Int, onRecord: () -> Unit) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when (hour) { in 5..11 -> "早上好呀～"; in 12..13 -> "中午好呀～"; in 14..18 -> "下午好呀～"; else -> "晚上好呀～" }
    val transition = rememberInfiniteTransition(label = "breath")
    val bob by transition.animateFloat(0.98f, 1.02f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "bob")
    var bubble by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Spacer(Modifier.height(24.dp))
                Text(greeting, Modifier.fillMaxWidth(), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("今天记录了吗？", Modifier.fillMaxWidth().padding(top = 6.dp), fontSize = 16.sp, color = Muted)
                Text("记录 $todayCount 次", Modifier.fillMaxWidth().padding(top = 4.dp), fontSize = 13.sp, color = SageDark)
                Spacer(Modifier.height(16.dp))
                Box(Modifier.fillMaxWidth().height(410.dp).background(Color(0xFFEFF3E2), RoundedCornerShape(32.dp)), contentAlignment = Alignment.BottomCenter) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(Yellow, radius = 36f, center = Offset(size.width - 72f, 76f))
                        drawCircle(Color.White.copy(alpha = .8f), 32f, Offset(76f, 128f)); drawCircle(Color.White.copy(alpha = .8f), 24f, Offset(108f, 128f))
                        drawOval(Color(0xFFDDE7C6), topLeft = Offset(0f, size.height * .52f), size = androidx.compose.ui.geometry.Size(size.width, size.height * .48f))
                        repeat(10) { i -> val x = (i * 73f) % size.width; drawLine(SageDark.copy(alpha = .5f), Offset(x, size.height - 26f), Offset(x + 4f, size.height - 44f), 3f, StrokeCap.Round) }
                    }
                    Text("顺顺，今天也要轻松一点", Modifier.align(Alignment.TopCenter).padding(top = 18.dp), color = SageDark, fontSize = 13.sp)
                    Box(Modifier.padding(bottom = 42.dp).scale(bob).clickable { bubble = true }) { PoopCharacter(118.dp) }
                    if (bubble) Text("记得喝水呀！", Modifier.align(Alignment.Center).padding(bottom = 210.dp).background(Surface, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 8.dp), color = BrownDark, fontSize = 12.sp)
                }
                Spacer(Modifier.height(20.dp))
                Button(onClick = onRecord, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Sage)) { Text("记录便便", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun PoopCharacter(size: androidx.compose.ui.unit.Dp) {
    Canvas(Modifier.size(size)) {
        val w = size.toPx(); val h = size.toPx()
        val p = androidx.compose.ui.graphics.Path().apply { moveTo(w*.18f,h*.78f); cubicTo(w*.1f,h*.55f,w*.22f,h*.62f,w*.29f,h*.45f); cubicTo(w*.36f,h*.28f,w*.43f,h*.5f,w*.5f,h*.3f); cubicTo(w*.58f,h*.12f,w*.67f,h*.38f,w*.76f,h*.46f); cubicTo(w*.88f,h*.56f,w*.93f,h*.77f,w*.8f,h*.83f); cubicTo(w*.6f,h*.93f,w*.32f,h*.94f,w*.18f,h*.78f); close() }
        drawPath(p, Brown); drawCircle(BrownDark, w*.035f, Offset(w*.39f,h*.62f)); drawCircle(BrownDark, w*.035f, Offset(w*.63f,h*.62f))
        drawArc(BrownDark, 15f, 150f, false, topLeft = Offset(w*.4f, h*.63f), size = androidx.compose.ui.geometry.Size(w*.22f, h*.15f), style = androidx.compose.ui.graphics.drawscope.Stroke(w*.025f))
        drawCircle(Color(0xFFE8A6A0), w*.055f, Offset(w*.31f,h*.68f)); drawCircle(Color(0xFFE8A6A0), w*.055f, Offset(w*.69f,h*.68f))
    }
}

@Composable
private fun RecordScreen(onSaved: (PoopRecord) -> Unit) {
    var stool by rememberSaveable { mutableIntStateOf(0) }; var mood by rememberSaveable { mutableIntStateOf(3) }; var smooth by rememberSaveable { mutableIntStateOf(72) }; var more by rememberSaveable { mutableStateOf(false) }; var saved by rememberSaveable { mutableStateOf(false) }
    val names = listOf("硬球状", "偏硬香肠", "表面裂纹", "光滑柔软", "软块状", "糊状", "水样")
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Spacer(Modifier.height(22.dp)); Text("记录便便", fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("花几秒钟，听听身体的声音", color = Muted, fontSize = 14.sp) }
        item { SectionCard("便便形状") { Column { (1..7).chunked(3).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { type -> val chosen = stool == type; Box(Modifier.weight(1f).padding(vertical = 5.dp).height(92.dp).border(1.dp, if (chosen) Sage else Line, RoundedCornerShape(18.dp)).background(if (chosen) SageLight.copy(alpha = .45f) else Surface, RoundedCornerShape(18.dp)).clickable { stool = type }, contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { PoopCharacter(37.dp); Text("${type}型", fontSize = 12.sp, color = if (chosen) SageDark else Ink); Text(names[type - 1], fontSize = 10.sp, color = Muted) } } }; repeat(3 - row.size) { Spacer(Modifier.weight(1f)) } } } } } }
        item { SectionCard("今天的感觉") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { listOf("很难受", "不舒服", "一般", "舒服", "超舒服").forEachIndexed { index, label -> val chosen = mood == index; Column(Modifier.clickable { mood = index }.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(42.dp).background(if (chosen) SageLight else Color(0xFFF0EDE2), CircleShape), contentAlignment = Alignment.Center) { Text(listOf("·︵·", "︶︵︶", "－ －", "•ᴗ•", "^ᴗ^")[index], color = BrownDark, fontSize = 12.sp) }; Text(label, fontSize = 10.sp, color = Muted) } } } } }
        item { SectionCard("顺畅度  $smooth%") { Column { Slider(value = smooth.toFloat(), onValueChange = { smooth = it.toInt() }, valueRange = 0f..100f, colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Sage, activeTrackColor = Sage)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("费劲", fontSize = 12.sp, color = Muted); Text("顺畅", fontSize = 12.sp, color = Muted) } } } }
        item { TextButton(onClick = { more = !more }) { Icon(Icons.Outlined.WaterDrop, null, tint = SageDark); Spacer(Modifier.width(6.dp)); Text(if (more) "收起更多记录" else "更多记录", color = SageDark) } }
        item { AnimatedVisibility(more) { SectionCard("补充信息") { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("排便时长：6 分钟", color = Muted, fontSize = 14.sp); Text("今天肚肚没有不舒服", color = Muted, fontSize = 14.sp); Text("备注：", color = Muted, fontSize = 14.sp) } } } }
        item { Button(onClick = { if (stool > 0) { saved = true; onSaved(PoopRecord(stoolType = stool, mood = mood, smoothness = smooth, durationMinutes = 6)) } }, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = if (saved) SageDark else Sage)) { Text(if (saved) "✓ 记录成功" else "保存记录", fontSize = 17.sp, fontWeight = FontWeight.Bold) } }
        item { if (stool == 0) Text("先选择一下便便类型吧～", color = Peach, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
        item { Spacer(Modifier.height(26.dp)) }
    }
}

@Composable private fun SectionCard(title: String, content: @Composable () -> Unit) { Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(24.dp)).border(1.dp, Line, RoundedCornerShape(24.dp)).padding(16.dp)) { Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)); content() } }

@Composable
private fun StatsScreen(records: List<PoopRecord>) {
    val average = if (records.isEmpty()) 0 else records.map { it.smoothness }.average().toInt()
    LazyColumn(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { item { Spacer(Modifier.height(18.dp)); Text("统计分析", fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("把每天的小变化，变成看得见的趋势", color = Muted, fontSize = 14.sp) }; item { SectionCard("最近状态") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Stat("记录总数", "${records.size}"); Stat("平均顺畅度", "$average%"); Stat("连续记录", if (records.isEmpty()) "0 天" else "1 天") } } }; item { SectionCard("近 7 次记录") { if (records.isEmpty()) Text("再记录几次，我就能帮你发现规律啦。", color = Muted) else { Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) { records.take(7).reversed().forEach { r -> Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.width(24.dp).height((r.smoothness * 1.1f).dp).background(Sage, RoundedCornerShape(8.dp))); Text("${r.stoolType}", fontSize = 11.sp, color = Muted) } } } } } } }
}

@Composable private fun Stat(label: String, value: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = SageDark); Text(label, fontSize = 11.sp, color = Muted) } }

@Composable
private fun ProfileScreen(records: List<PoopRecord>, vm: PoopViewModel) {
    var confirm by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    fun encode(): String = JSONArray().apply { records.forEach { r -> put(JSONObject().apply { put("timestamp", r.timestamp); put("stoolType", r.stoolType); put("mood", r.mood); put("smoothness", r.smoothness); put("durationMinutes", r.durationMinutes ?: JSONObject.NULL); put("abdominalPain", r.abdominalPain); put("straining", r.straining); put("incompleteFeeling", r.incompleteFeeling); put("note", r.note) }) } }.toString()
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? -> uri?.let { selected -> scope.launch { context.contentResolver.openOutputStream(selected)?.use { it.write(encode().toByteArray()) } } } }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> uri?.let { selected -> scope.launch { context.contentResolver.openInputStream(selected)?.bufferedReader()?.use { reader -> val array = JSONArray(reader.readText()); val imported = buildList { for (i in 0 until array.length()) { val o = array.getJSONObject(i); add(PoopRecord(stoolType = o.getInt("stoolType"), mood = o.getInt("mood"), smoothness = o.getInt("smoothness"), timestamp = o.getLong("timestamp"), durationMinutes = if (o.isNull("durationMinutes")) null else o.getInt("durationMinutes"), abdominalPain = o.optBoolean("abdominalPain"), straining = o.optBoolean("straining"), incompleteFeeling = o.optBoolean("incompleteFeeling"), note = o.optString("note"))) } }; vm.addAll(imported) } } } }
    LazyColumn(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { item { Spacer(Modifier.height(18.dp)); Text("我的", fontSize = 26.sp, fontWeight = FontWeight.Bold); SectionCard("顺顺用户") { Row(verticalAlignment = Alignment.CenterVertically) { PoopCharacter(70.dp); Spacer(Modifier.width(14.dp)); Column { Text("小顺顺", fontSize = 18.sp, fontWeight = FontWeight.Bold); Text("本地生活记录 · ${records.size} 条", color = Muted, fontSize = 13.sp) } } } }; item { SectionCard("数据管理") { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { ActionRow(Icons.Outlined.FileDownload, "导出本地备份") { exportLauncher.launch("顺顺备份.json") }; ActionRow(Icons.Outlined.FileUpload, "恢复本地备份") { importLauncher.launch(arrayOf("application/json", "text/plain")) }; ActionRow(Icons.Outlined.DeleteOutline, "清除全部记录", Peach) { confirm = true } } } }; item { Text("顺顺是一款个人生活记录工具，不构成医疗诊断或治疗建议。", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)) } }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("清除全部记录？") }, text = { Text("删除后无法恢复，请确认已不再需要这些本地数据。") }, confirmButton = { TextButton(onClick = { vm.clear(); confirm = false }) { Text("确认清除", color = Peach) } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("取消") } })
}

@Composable private fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: androidx.compose.ui.graphics.Color = SageDark, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = tint); Spacer(Modifier.width(12.dp)); Text(label, color = Ink, fontSize = 15.sp); Spacer(Modifier.weight(1f)); Text("›", color = Muted, fontSize = 22.sp) } }
