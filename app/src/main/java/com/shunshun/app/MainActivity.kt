package com.shunshun.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shunshun.app.data.AppDatabase
import com.shunshun.app.data.PoopDao
import com.shunshun.app.data.PoopRecord
import com.shunshun.app.ui.BlueMist
import com.shunshun.app.ui.Brown
import com.shunshun.app.ui.BrownDark
import com.shunshun.app.ui.Cream
import com.shunshun.app.ui.Ink
import com.shunshun.app.ui.Line
import com.shunshun.app.ui.Lilac
import com.shunshun.app.ui.Meadow
import com.shunshun.app.ui.Muted
import com.shunshun.app.ui.Peach
import com.shunshun.app.ui.Sage
import com.shunshun.app.ui.SageDark
import com.shunshun.app.ui.SageLight
import com.shunshun.app.ui.ShunShunTheme
import com.shunshun.app.ui.Surface
import com.shunshun.app.ui.Yellow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ShunShunTheme { ShunShunApp() } }
    }
}

class PoopViewModel(private val dao: PoopDao) : ViewModel() {
    val records: StateFlow<List<PoopRecord>> = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun add(record: PoopRecord) = viewModelScope.launch(Dispatchers.IO) { dao.insert(record) }
    fun addAll(items: List<PoopRecord>) = viewModelScope.launch(Dispatchers.IO) { dao.insertAll(items) }
    fun delete(record: PoopRecord) = viewModelScope.launch(Dispatchers.IO) { dao.delete(record) }
    fun clear() = viewModelScope.launch(Dispatchers.IO) { dao.clearAll() }
}

class PoopViewModelFactory(private val dao: PoopDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = PoopViewModel(dao) as T
}

private enum class AppTab(val title: String) { HOME("首页"), RECORD("记录"), STATS("统计"), PROFILE("我的") }

@Composable
private fun ShunShunApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.get(context) }
    val vm: PoopViewModel = viewModel(factory = PoopViewModelFactory(database.poopDao()))
    val records by vm.records.collectAsState()
    var tabName by rememberSaveable { mutableStateOf(AppTab.HOME.name) }
    val tab = AppTab.valueOf(tabName)
    val snack = remember { SnackbarHostState() }
    Scaffold(containerColor = Cream, snackbarHost = { SnackbarHost(snack) }, bottomBar = { BottomNavigation(tab) { tabName = it.name } }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(targetState = tab, label = "page", transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) }) { page ->
                when (page) {
                    AppTab.HOME -> HomeScreen(records) { tabName = AppTab.RECORD.name }
                    AppTab.RECORD -> RecordScreen(records, onSaved = { record -> vm.add(record); tabName = AppTab.HOME.name }, onDelete = vm::delete)
                    AppTab.STATS -> StatisticsScreen(records)
                    AppTab.PROFILE -> ProfileScreen(records, vm)
                }
            }
        }
    }
}

@Composable
private fun BottomNavigation(selected: AppTab, onSelect: (AppTab) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Surface).border(1.dp, Line.copy(alpha = .45f)).padding(horizontal = 18.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        AppTab.values().forEach { item ->
            val active = item == selected
            Column(Modifier.width(62.dp).clickable { onSelect(item) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(40.dp).background(if (active) SageLight.copy(alpha = .65f) else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) { NavGlyph(item, if (active) SageDark else Muted) }
                Text(item.title, color = if (active) SageDark else Muted, fontSize = 10.sp, fontWeight = if (active) FontWeight.Medium else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun NavGlyph(tab: AppTab, tint: Color) {
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

@Composable
private fun HomeScreen(records: List<PoopRecord>, onRecord: () -> Unit) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) { in 5..11 -> "早上好呀～"; in 12..13 -> "中午好呀～"; in 14..18 -> "下午好呀～"; else -> "晚上好呀～" }
    var bubble by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "garden")
    val bob by transition.animateFloat(.985f, 1.015f, infiniteRepeatable(tween(2_200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "character")
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Spacer(Modifier.height(23.dp))
            Text(greeting, color = Ink, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Text("今天记录了吗？", color = Muted, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(top = 5.dp))
            Text("记录 ${records.size} 次", color = SageDark, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
            Spacer(Modifier.height(17.dp))
            Box(Modifier.fillMaxWidth().height(370.dp).background(Meadow.copy(alpha = .58f), RoundedCornerShape(31.dp)), contentAlignment = Alignment.BottomCenter) {
                GardenIllustration()
                Box(Modifier.padding(bottom = 43.dp).scale(bob).clickable { bubble = !bubble }) { PoopMascot(112.dp, MascotMood.Happy) }
                AnimatedVisibility(visible = bubble, modifier = Modifier.align(Alignment.Center).offset(y = (-72).dp)) { Text("今天也要顺顺利利～", color = BrownDark, fontSize = 12.sp, modifier = Modifier.background(Surface, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 9.dp)) }
                Text("顺顺，今天也要轻松一点", color = SageDark, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 21.dp))
            }
            Spacer(Modifier.height(17.dp)); PrimaryButton("记录便便", onRecord); Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(18.dp)).border(1.dp, Line, RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = SageDark, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(10.dp)); Text(if (records.isEmpty()) "完成第一次记录，就能在这里看到自己的小规律" else "最近一次：${formatTime(records.first().timestamp)} · ${records.first().stoolType}型", color = Muted, fontSize = 12.sp); Spacer(Modifier.weight(1f)); Icon(Icons.Outlined.LocalFlorist, null, tint = Peach, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GardenIllustration() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        drawCircle(Yellow.copy(alpha = .95f), 29f, Offset(w - 45f, 46f))
        drawCircle(Color.White.copy(alpha = .58f), 21f, Offset(45f, 65f)); drawCircle(Color.White.copy(alpha = .58f), 17f, Offset(66f, 65f)); drawCircle(Color.White.copy(alpha = .58f), 13f, Offset(81f, 65f)); drawLine(SageDark.copy(alpha = .16f), Offset(34f, 72f), Offset(90f, 72f), 1.6f, StrokeCap.Round)
        drawOval(Color(0xFFD9E7BE), topLeft = Offset(-w * .08f, h * .53f), size = Size(w * 1.16f, h * .57f)); drawOval(Color(0xFFC7DCA5), topLeft = Offset(-w * .14f, h * .74f), size = Size(w * 1.28f, h * .42f))
        drawPlant(24f, h - 8f, -1f); drawPlant(w - 25f, h - 12f, 1f); drawPlant(50f, h - 21f, -1f); drawPlant(w - 53f, h - 25f, 1f)
        for (i in 0..15) { val x = (i * 43f) % w; drawLine(SageDark.copy(alpha = .38f), Offset(x, h - 16f), Offset(x + if (i % 2 == 0) 3f else -2f, h - 27f), 1.6f, StrokeCap.Round) }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlant(x: Float, y: Float, direction: Float) {
    drawLine(SageDark.copy(alpha = .7f), Offset(x, y), Offset(x + direction * 18f, y - 58f), 2.5f, StrokeCap.Round); drawOval(Sage.copy(alpha = .72f), topLeft = Offset(x + direction * 12f, y - 52f), size = Size(18f, 8f)); drawOval(SageLight, topLeft = Offset(x + direction * 2f, y - 36f), size = Size(20f, 9f))
}

private enum class MascotMood { Default, Happy, Excited, Sleepy, Sad, Water }

@Composable
private fun PoopMascot(size: Dp, mood: MascotMood = MascotMood.Default) {
    Canvas(Modifier.size(size)) {
        val w = size.toPx(); val h = size.toPx(); val path = Path().apply { moveTo(w * .16f, h * .78f); cubicTo(w * .10f, h * .62f, w * .17f, h * .48f, w * .29f, h * .43f); cubicTo(w * .30f, h * .25f, w * .42f, h * .34f, w * .50f, h * .16f); cubicTo(w * .59f, h * .02f, w * .64f, h * .27f, w * .72f, h * .40f); cubicTo(w * .88f, h * .48f, w * .93f, h * .68f, w * .83f, h * .80f); cubicTo(w * .67f, h * .98f, w * .31f, h * .98f, w * .16f, h * .78f); close() }
        drawPath(path, Brown); drawLine(BrownDark.copy(alpha = .38f), Offset(w * .21f, h * .77f), Offset(w * .34f, h * .87f), 2f, StrokeCap.Round); drawLine(BrownDark.copy(alpha = .38f), Offset(w * .79f, h * .77f), Offset(w * .66f, h * .87f), 2f, StrokeCap.Round)
        val eyeY = if (mood == MascotMood.Sleepy) .62f else .60f
        if (mood == MascotMood.Sleepy) { drawLine(BrownDark, Offset(w * .32f, h * eyeY), Offset(w * .41f, h * eyeY), 3f, StrokeCap.Round); drawLine(BrownDark, Offset(w * .59f, h * eyeY), Offset(w * .68f, h * eyeY), 3f, StrokeCap.Round) } else { drawCircle(BrownDark, w * .034f, Offset(w * .38f, h * eyeY)); drawCircle(BrownDark, w * .034f, Offset(w * .62f, h * eyeY)) }
        drawCircle(Color(0xFFE7A6A0), w * .06f, Offset(w * .27f, h * .69f)); drawCircle(Color(0xFFE7A6A0), w * .06f, Offset(w * .73f, h * .69f))
        if (mood == MascotMood.Sad) drawArc(BrownDark, 200f, 140f, false, topLeft = Offset(w * .40f, h * .64f), size = Size(w * .20f, h * .14f), style = Stroke(3f)) else drawArc(BrownDark, 15f, 150f, false, topLeft = Offset(w * .40f, h * .61f), size = Size(w * .20f, h * .15f), style = Stroke(3f))
        if (mood == MascotMood.Water) { drawLine(BlueMist, Offset(w * .84f, h * .22f), Offset(w * .95f, h * .06f), 4f, StrokeCap.Round); drawCircle(BlueMist, 4f, Offset(w * .96f, h * .22f)) }
    }
}

@Composable
private fun StoolIcon(type: Int, size: Dp = 48.dp) {
    Canvas(Modifier.size(size)) {
        val w = size.toPx(); val h = size.toPx(); val fill = Brown.copy(alpha = .22f); val detail = BrownDark.copy(alpha = .72f)
        when (type) {
            1 -> { drawCircle(fill, w * .16f, Offset(w * .34f, h * .55f)); drawCircle(fill, w * .16f, Offset(w * .66f, h * .55f)); drawCircle(fill, w * .13f, Offset(w * .50f, h * .28f)); drawLine(detail, Offset(w * .31f, h * .69f), Offset(w * .69f, h * .69f), 2f, StrokeCap.Round) }
            2 -> { drawRoundRect(fill, topLeft = Offset(w * .16f, h * .30f), size = Size(w * .68f, h * .34f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * .17f)); drawLine(detail, Offset(w * .27f, h * .39f), Offset(w * .70f, h * .54f), 2f, StrokeCap.Round) }
            3 -> { drawOval(fill, topLeft = Offset(w * .17f, h * .28f), size = Size(w * .66f, h * .39f)); drawLine(detail, Offset(w * .34f, h * .34f), Offset(w * .28f, h * .49f), 2f, StrokeCap.Round); drawLine(detail, Offset(w * .54f, h * .30f), Offset(w * .48f, h * .48f), 2f, StrokeCap.Round); drawLine(detail, Offset(w * .72f, h * .38f), Offset(w * .66f, h * .55f), 2f, StrokeCap.Round) }
            4 -> { val p = Path().apply { moveTo(w * .20f, h * .64f); cubicTo(w * .10f, h * .50f, w * .18f, h * .34f, w * .34f, h * .37f); cubicTo(w * .37f, h * .19f, w * .61f, h * .18f, w * .65f, h * .35f); cubicTo(w * .86f, h * .29f, w * .91f, h * .54f, w * .77f, h * .65f); cubicTo(w * .60f, h * .77f, w * .34f, h * .76f, w * .20f, h * .64f); close() }; drawPath(p, fill) }
            5 -> { drawCircle(fill, w * .18f, Offset(w * .30f, h * .52f)); drawCircle(fill, w * .20f, Offset(w * .50f, h * .45f)); drawCircle(fill, w * .17f, Offset(w * .70f, h * .53f)); drawOval(fill, topLeft = Offset(w * .24f, h * .54f), size = Size(w * .52f, h * .24f)) }
            6 -> { val p = Path().apply { moveTo(w * .18f, h * .38f); quadraticTo(w * .28f, h * .23f, w * .39f, h * .39f); quadraticTo(w * .50f, h * .57f, w * .61f, h * .36f); quadraticTo(w * .73f, h * .20f, w * .84f, h * .42f); quadraticTo(w * .79f, h * .69f, w * .52f, h * .72f); quadraticTo(w * .23f, h * .74f, w * .18f, h * .38f); close() }; drawPath(p, fill); drawCircle(detail, 2.5f, Offset(w * .34f, h * .50f)); drawCircle(detail, 2.5f, Offset(w * .61f, h * .54f)) }
            else -> { drawCircle(fill, w * .20f, Offset(w * .34f, h * .38f)); drawCircle(fill, w * .20f, Offset(w * .66f, h * .38f)); drawCircle(fill, w * .17f, Offset(w * .50f, h * .64f)); drawCircle(fill.copy(alpha = .75f), w * .08f, Offset(w * .21f, h * .78f)); drawCircle(fill.copy(alpha = .75f), w * .08f, Offset(w * .79f, h * .78f)) }
        }
    }
}

@Composable
private fun MoodFace(index: Int, selected: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.08f else 1f, spring(dampingRatio = .6f), label = "mood")
    Column(Modifier.width(58.dp).scale(scale).clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(48.dp).background(if (selected) SageLight else Color(0xFFF1EEE4), CircleShape), contentAlignment = Alignment.Center) { Canvas(Modifier.fillMaxSize().padding(12.dp)) { drawCircle(BrownDark.copy(alpha = .7f), 2.2f, Offset(size.width * .30f, size.height * .38f)); drawCircle(BrownDark.copy(alpha = .7f), 2.2f, Offset(size.width * .70f, size.height * .38f)); if (index < 2) drawArc(BrownDark, 20f, 140f, false, topLeft = Offset(size.width * .28f, size.height * .48f), size = Size(size.width * .44f, size.height * .28f), style = Stroke(2f)) else drawArc(BrownDark, 200f, 140f, false, topLeft = Offset(size.width * .28f, size.height * .38f), size = Size(size.width * .44f, size.height * .28f), style = Stroke(2f)) } }
        Text(listOf("很难受", "不舒服", "一般", "舒服", "超舒服")[index], color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }; val pressed by source.collectIsPressedAsState()
    Button(onClick = onClick, interactionSource = source, modifier = Modifier.fillMaxWidth().height(53.dp).scale(if (pressed) .97f else 1f), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = Sage), elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)) { Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium) }
}

@Composable
private fun SectionCard(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) { Column(modifier.fillMaxWidth().background(Surface.copy(alpha = .76f), RoundedCornerShape(23.dp)).border(1.dp, Line.copy(alpha = .78f), RoundedCornerShape(23.dp)).padding(16.dp)) { Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink); Spacer(Modifier.height(11.dp)); content() } }

@Composable
private fun SegmentedControl(selected: String, values: List<String>, onSelect: (String) -> Unit) { Row(Modifier.fillMaxWidth().background(Color(0xFFF1EFE6), RoundedCornerShape(16.dp)).padding(4.dp)) { values.forEach { value -> Box(Modifier.weight(1f).height(36.dp).background(if (selected == value) Surface else Color.Transparent, RoundedCornerShape(12.dp)).clickable { onSelect(value) }, contentAlignment = Alignment.Center) { Text(value, color = if (selected == value) SageDark else Muted, fontSize = 13.sp, fontWeight = if (selected == value) FontWeight.SemiBold else FontWeight.Normal) } } } }

@Composable
private fun RecordScreen(records: List<PoopRecord>, onSaved: (PoopRecord) -> Unit, onDelete: (PoopRecord) -> Unit) {
    var mode by rememberSaveable { mutableStateOf("记录一次") }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { item { Spacer(Modifier.height(21.dp)); Text("记录便便", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold); Text("花几秒钟，听听身体的声音", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)); Spacer(Modifier.height(12.dp)); SegmentedControl(mode, listOf("记录一次", "历史记录")) { mode = it } }; if (mode == "历史记录") item { HistoryList(records, onDelete) } else item { RecordForm(onSaved) }; item { Spacer(Modifier.height(24.dp)) } }
}

@Composable
private fun RecordForm(onSaved: (PoopRecord) -> Unit) {
    var stool by rememberSaveable { mutableIntStateOf(0) }; var mood by rememberSaveable { mutableIntStateOf(3) }; var smoothness by rememberSaveable { mutableIntStateOf(72) }; var more by rememberSaveable { mutableStateOf(false) }; var error by rememberSaveable { mutableStateOf(false) }; var saved by rememberSaveable { mutableStateOf(false) }; var pending by remember { mutableStateOf<PoopRecord?>(null) }
    LaunchedEffect(pending) { pending?.let { delay(1_100); onSaved(it) } }
    val offset by animateFloatAsState(if (error) 5f else 0f, tween(90), label = "errorShake")
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard("便便形状", Modifier.offset(x = offset.dp)) { val names = listOf("硬球状", "偏硬香肠", "表面裂纹", "光滑柔软", "软块状", "糊状", "水样"); Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { (1..7).chunked(3).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { row.forEach { type -> val chosen = type == stool; val scale by animateFloatAsState(if (chosen) 1.04f else 1f, spring(dampingRatio = .65f), label = "stool$type"); Column(Modifier.weight(1f).scale(scale).height(106.dp).background(if (chosen) SageLight.copy(alpha = .48f) else Surface, RoundedCornerShape(18.dp)).border(1.dp, if (chosen) Sage else Line, RoundedCornerShape(18.dp)).clickable { stool = type; error = false }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { StoolIcon(type, 43.dp); Text("${type}型", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.SemiBold); Text(names[type - 1], color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp)) } }; repeat(3 - row.size) { Spacer(Modifier.weight(1f)) } } } } }
        SectionCard("今天的感觉") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { (0..4).forEach { MoodFace(it, mood == it) { mood = it } } } }
        SectionCard("顺畅度  $smoothness%") { SoftnessSlider(smoothness) { smoothness = it } }
        Row(Modifier.fillMaxWidth().clickable { more = !more }.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.WaterDrop, null, tint = SageDark, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(8.dp)); Text(if (more) "收起更多记录" else "更多记录", color = SageDark, fontSize = 14.sp); Spacer(Modifier.weight(1f)); Text(if (more) "⌃" else "⌄", color = SageDark, fontSize = 20.sp) }
        AnimatedVisibility(more) { SectionCard("补充记录") { Column(verticalArrangement = Arrangement.spacedBy(13.dp)) { OptionRow(Icons.Outlined.AccessTime, "排便时长", "6 分钟"); OptionRow(Icons.Outlined.Info, "肚肚状态", "没有不舒服"); OptionRow(Icons.Outlined.Edit, "备注", "添加一点文字") } } }
        PrimaryButton(if (saved) "✓ 记录成功" else "保存记录") { if (stool == 0) error = true else { val record = PoopRecord(stoolType = stool, mood = mood, smoothness = smoothness, durationMinutes = 6); pending = record; saved = true } }
        if (error) Text("先选择一下便便类型吧～", color = Peach, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        AnimatedVisibility(saved) { SuccessOverlay() }
    }
}

@Composable
private fun SuccessOverlay() { Box(Modifier.fillMaxWidth().background(Cream.copy(alpha = .96f), RoundedCornerShape(24.dp)).padding(vertical = 18.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { PoopMascot(70.dp, MascotMood.Excited); Text("记录成功！", color = SageDark, fontSize = 17.sp, fontWeight = FontWeight.Bold); Text("今天也有认真照顾身体～", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) } } }

@Composable
private fun SoftnessSlider(value: Int, onChange: (Int) -> Unit) { Column(Modifier.fillMaxWidth()) { Box(Modifier.fillMaxWidth().height(60.dp).pointerInput(Unit) { detectDragGestures { change, _ -> onChange(((change.position.x / size.width) * 100).toInt().coerceIn(0, 100)) } }) { Canvas(Modifier.fillMaxSize().padding(top = 19.dp, bottom = 19.dp)) { val y = size.height / 2; drawRoundRect(Color(0xFFE7E0D0), topLeft = Offset(0f, y - 5f), size = Size(size.width, 10f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f)); drawRoundRect(Sage, topLeft = Offset(0f, y - 5f), size = Size(size.width * value / 100f, 10f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f)); val x = size.width * value / 100f; drawCircle(Surface, 13f, Offset(x, y)); drawCircle(SageDark, 13f, Offset(x, y), style = Stroke(2.5f)); drawCircle(Brown, 5f, Offset(x, y)) } }; Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("费劲", color = Muted, fontSize = 12.sp); Text("顺畅", color = Muted, fontSize = 12.sp) } } }

@Composable
private fun HistoryList(records: List<PoopRecord>, onDelete: (PoopRecord) -> Unit) { if (records.isEmpty()) EmptyState("这里还空空的～", "完成第一次记录后，它就会住进这里。", MascotMood.Sleepy) else Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("最近记录", color = Muted, fontSize = 13.sp); records.forEach { HistoryCard(it, onDelete) } } }

@Composable
private fun HistoryCard(record: PoopRecord, onDelete: (PoopRecord) -> Unit) { var confirm by remember { mutableStateOf(false) }; Row(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(20.dp)).border(1.dp, Line, RoundedCornerShape(20.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(52.dp).background(Meadow, CircleShape), contentAlignment = Alignment.Center) { StoolIcon(record.stoolType, 34.dp) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(formatDate(record.timestamp), color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold); Text("${record.stoolType}型 · ${record.smoothness}% 顺畅", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp)); Text(moodName(record.mood), color = SageDark, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp)) }; IconButton(onClick = { confirm = true }) { Icon(Icons.Outlined.DeleteOutline, "删除记录", tint = Muted) } }; if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("确定删除这条记录吗？") }, text = { Text("删除后无法恢复。") }, confirmButton = { TextButton(onClick = { onDelete(record); confirm = false }) { Text("删除", color = Peach) } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("取消") } }) }

@Composable
private fun StatisticsScreen(records: List<PoopRecord>) { var range by rememberSaveable { mutableStateOf("周") }; val average = if (records.isEmpty()) 0 else records.map { it.smoothness }.average().toInt(); LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { item { Spacer(Modifier.height(22.dp)); Text("统计分析", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold); Text("把每天的小变化，变成看得见的趋势", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)); Spacer(Modifier.height(11.dp)); SegmentedControl(range, listOf("日", "周", "月", "年")) { range = it } }; item { SectionCard("最近状态") { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { StatMetric("记录总数", records.size.toString()); StatMetric("平均顺畅度", "$average%"); StatMetric("连续记录", if (records.isEmpty()) "0 天" else "${streak(records)} 天") } } }; item { SectionCard("排便节奏") { RhythmChart(records) } }; item { SectionCard("便便类型分布") { TypeDonut(records) } }; item { TrendRow(records) }; item { Spacer(Modifier.height(22.dp)) } } }

@Composable
private fun StatMetric(label: String, value: String) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text(value, color = SageDark, fontSize = 27.sp, fontWeight = FontWeight.Bold); Text(label, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 3.dp)) } }

@Composable
private fun RhythmChart(records: List<PoopRecord>) {
    if (records.isEmpty()) { EmptyState("再记录几次，我就能帮你发现规律啦。", "你的每一次记录，都会变成温柔的趋势线。", MascotMood.Water); return }
    val points = records.take(7).reversed().map { it.smoothness }
    Canvas(Modifier.fillMaxWidth().height(190.dp).padding(horizontal = 6.dp)) {
        val chartW = size.width; val chartH = size.height - 28f; val slot = chartW / points.size.coerceAtLeast(1); val barW = (slot * .42f).coerceAtLeast(18f)
        for (i in 0..3) { val y = chartH * i / 3f; drawLine(Line, Offset(0f, y), Offset(chartW, y), 1f) }
        points.forEachIndexed { index, point -> val barH = chartH * point / 100f; val x = slot * index + (slot - barW) / 2f; drawRoundRect(Sage.copy(alpha = .88f), topLeft = Offset(x, chartH - barH), size = Size(barW, barH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f)); drawCircle(Color.White.copy(alpha = .55f), 2.5f, Offset(x + barW / 2f, chartH - barH + 10f)) }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { points.indices.forEach { Text("${it + 1}", color = Muted, fontSize = 10.sp) } }
}

@Composable
private fun TypeDonut(records: List<PoopRecord>) { if (records.isEmpty()) { Text("有了几次记录后，这里会显示你的类型分布。", color = Muted, fontSize = 13.sp); return }; val counts = (1..7).map { type -> records.count { it.stoolType == type } }; val colors = listOf(SageDark, Sage, SageLight, Yellow, Peach, Lilac, BlueMist); Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(155.dp), contentAlignment = Alignment.Center) { Canvas(Modifier.fillMaxSize().padding(14.dp)) { val total = counts.sum().coerceAtLeast(1); var start = -90f; counts.forEachIndexed { index, count -> val sweep = 360f * count / total; drawArc(colors[index], start, sweep - 3f, false, style = Stroke(25f)); start += sweep } }; Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("最常见", color = Muted, fontSize = 11.sp); Text("${counts.indices.maxByOrNull { counts[it] }?.plus(1) ?: 4}型", color = SageDark, fontSize = 25.sp, fontWeight = FontWeight.Bold) } }; Spacer(Modifier.width(14.dp)); Column(verticalArrangement = Arrangement.spacedBy(7.dp)) { counts.forEachIndexed { index, count -> Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(9.dp).background(colors[index], CircleShape)); Spacer(Modifier.width(7.dp)); Text("${index + 1}型  $count 次", color = Muted, fontSize = 11.sp) } } } } }

@Composable
private fun TrendRow(records: List<PoopRecord>) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { TrendCard(Icons.Outlined.WaterDrop, "平均顺畅度", if (records.isEmpty()) "—" else "${records.map { it.smoothness }.average().toInt()}%", Modifier.weight(1f)); TrendCard(Icons.Outlined.LocalFlorist, "记录天数", "${records.map { dayKey(it.timestamp) }.distinct().size} 天", Modifier.weight(1f)); TrendCard(Icons.Outlined.DirectionsWalk, "生活趋势", if (records.isEmpty()) "等待中" else "在变好", Modifier.weight(1f)) } }

@Composable
private fun TrendCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier) { Column(modifier.background(Surface, RoundedCornerShape(18.dp)).border(1.dp, Line, RoundedCornerShape(18.dp)).padding(12.dp)) { Icon(icon, null, tint = SageDark, modifier = Modifier.size(19.dp)); Text(value, color = SageDark, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp)); Text(label, color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp)) } }

@Composable
private fun ProfileScreen(records: List<PoopRecord>, vm: PoopViewModel) {
    val context = LocalContext.current; val scope = rememberCoroutineScope(); var confirm by remember { mutableStateOf(false) }
    fun encode(): String = JSONArray().apply { records.forEach { r -> put(JSONObject().apply { put("timestamp", r.timestamp); put("stoolType", r.stoolType); put("mood", r.mood); put("smoothness", r.smoothness); put("durationMinutes", r.durationMinutes ?: JSONObject.NULL); put("abdominalPain", r.abdominalPain); put("straining", r.straining); put("incompleteFeeling", r.incompleteFeeling); put("note", r.note) }) } }.toString()
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? -> uri?.let { selected -> scope.launch { context.contentResolver.openOutputStream(selected)?.use { it.write(encode().toByteArray()) } } } }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> uri?.let { selected -> scope.launch { context.contentResolver.openInputStream(selected)?.bufferedReader()?.use { reader -> val json = JSONArray(reader.readText()); val imported = buildList { for (i in 0 until json.length()) { val o = json.getJSONObject(i); add(PoopRecord(timestamp = o.getLong("timestamp"), stoolType = o.getInt("stoolType"), mood = o.getInt("mood"), smoothness = o.getInt("smoothness"), durationMinutes = if (o.isNull("durationMinutes")) null else o.getInt("durationMinutes"), abdominalPain = o.optBoolean("abdominalPain"), straining = o.optBoolean("straining"), incompleteFeeling = o.optBoolean("incompleteFeeling"), note = o.optString("note"))) } }; vm.addAll(imported) } } } }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { item { Spacer(Modifier.height(22.dp)); Text("我的", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(11.dp)); SectionCard("便便晴天用户") { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(80.dp).background(Meadow, CircleShape), contentAlignment = Alignment.Center) { PoopMascot(62.dp, MascotMood.Happy) }; Spacer(Modifier.width(15.dp)); Column { Text("小晴天", color = Ink, fontSize = 19.sp, fontWeight = FontWeight.Bold); Text("本地生活记录 · ${records.size} 条", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp)); Text("记录，是给身体的一封小情书", color = SageDark, fontSize = 12.sp, modifier = Modifier.padding(top = 7.dp)) } } } }; item { SectionCard("数据管理") { Column(verticalArrangement = Arrangement.spacedBy(2.dp)) { ActionRow(Icons.Outlined.Download, "导出本地备份") { exportLauncher.launch("便便晴天备份.json") }; ActionRow(Icons.Outlined.FileUpload, "恢复本地备份") { importLauncher.launch(arrayOf("application/json", "text/plain")) }; ActionRow(Icons.Outlined.DeleteOutline, "清除全部记录", Peach) { confirm = true } } } }; item { SectionCard("生活小设置") { Column(verticalArrangement = Arrangement.spacedBy(2.dp)) { ActionRow(Icons.Outlined.WaterDrop, "喝水提醒") {}; ActionRow(Icons.Outlined.CalendarMonth, "记录提醒") {}; ActionRow(Icons.Outlined.Info, "关于便便晴天") {} } } }; item { Text("便便晴天仅用于个人生活记录，不构成医疗诊断或治疗建议。", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp)) }; item { Spacer(Modifier.height(24.dp)) } }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("清除全部记录？") }, text = { Text("删除后无法恢复，请确认已不再需要这些本地数据。") }, confirmButton = { TextButton(onClick = { vm.clear(); confirm = false }) { Text("确认清除", color = Peach) } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("取消") } })
}

@Composable
private fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color = SageDark, onClick: () -> Unit = {}) { Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = tint, modifier = Modifier.size(21.dp)); Spacer(Modifier.width(13.dp)); Text(label, color = Ink, fontSize = 14.sp); Spacer(Modifier.weight(1f)); Text("›", color = Muted, fontSize = 23.sp) } }

@Composable
private fun OptionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = SageDark, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(10.dp)); Text(title, color = Muted, fontSize = 13.sp); Spacer(Modifier.weight(1f)); Text(value, color = Ink, fontSize = 13.sp) } }

@Composable
private fun EmptyState(title: String, subtitle: String, mood: MascotMood) { Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) { PoopMascot(76.dp, mood); Text(title, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp)); Text(subtitle, color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp)) } }

private fun formatTime(timestamp: Long): String = SimpleDateFormat("HH:mm", Locale.CHINA).format(Date(timestamp))
private fun formatDate(timestamp: Long): String = SimpleDateFormat("MM月dd日  HH:mm", Locale.CHINA).format(Date(timestamp))
private fun dayKey(timestamp: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(timestamp))
private fun moodName(mood: Int): String = listOf("很难受", "不舒服", "一般", "舒服", "超舒服").getOrElse(mood) { "一般" }
private fun streak(records: List<PoopRecord>): Int = records.map { dayKey(it.timestamp) }.distinct().size
