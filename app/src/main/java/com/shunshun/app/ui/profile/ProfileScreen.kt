package com.shunshun.app.ui.profile

import android.net.Uri
import android.widget.Toast
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shunshun.app.PoopViewModel
import com.shunshun.app.data.PoopRecord
import com.shunshun.app.ui.BrownDark
import com.shunshun.app.ui.Cream
import com.shunshun.app.ui.Ink
import com.shunshun.app.ui.Meadow
import com.shunshun.app.ui.Muted
import com.shunshun.app.ui.Peach
import com.shunshun.app.ui.SageDark
import com.shunshun.app.ui.Sage
import com.shunshun.app.ui.Line
import com.shunshun.app.ui.Surface
import com.shunshun.app.ui.components.ActionRow
import com.shunshun.app.ui.components.PoopMascot
import com.shunshun.app.ui.components.SectionCard
import com.shunshun.app.ui.components.MascotMood
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.settingsDataStore by preferencesDataStore(name = "shunshun_settings")
private val WATER_REMINDER = booleanPreferencesKey("water_reminder")
private val RECORD_REMINDER = booleanPreferencesKey("record_reminder")

@Composable
fun ProfileScreen(records: List<PoopRecord>, vm: PoopViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confirmClear by remember { mutableStateOf(false) }
    val waterReminder by context.settingsDataStore.data.map { it[WATER_REMINDER] ?: false }.collectAsState(initial = false)
    val recordReminder by context.settingsDataStore.data.map { it[RECORD_REMINDER] ?: false }.collectAsState(initial = false)
    var showAbout by remember { mutableStateOf(false) }

    fun encodeBackup(): String = JSONArray().apply {
        records.forEach { record ->
            put(JSONObject().apply {
                put("timestamp", record.timestamp)
                put("stoolType", record.stoolType)
                put("mood", record.mood)
                put("smoothness", record.smoothness)
                put("durationMinutes", record.durationMinutes ?: JSONObject.NULL)
                put("abdominalPain", record.abdominalPain)
                put("straining", record.straining)
                put("incompleteFeeling", record.incompleteFeeling)
                put("note", record.note)
            })
        }
    }.toString(2)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(encodeBackup().toByteArray(Charsets.UTF_8))
                } ?: error("无法打开备份文件")
            }.onSuccess {
                Toast.makeText(context, "备份已保存", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "备份保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: error("无法读取备份文件")
                val json = JSONArray(text)
                buildList {
                    for (index in 0 until json.length()) {
                        val item = json.getJSONObject(index)
                        add(
                            PoopRecord(
                                timestamp = item.getLong("timestamp"),
                                stoolType = item.getInt("stoolType").coerceIn(1, 7),
                                mood = item.optInt("mood", 3).coerceIn(0, 4),
                                smoothness = item.optInt("smoothness", 72).coerceIn(0, 100),
                                durationMinutes = if (item.isNull("durationMinutes")) null else item.optInt("durationMinutes"),
                                abdominalPain = item.optBoolean("abdominalPain"),
                                straining = item.optBoolean("straining"),
                                incompleteFeeling = item.optBoolean("incompleteFeeling"),
                                note = item.optString("note")
                            )
                        )
                    }
                }
            }.onSuccess { imported ->
                if (imported.isNotEmpty()) {
                    vm.addAll(imported)
                    Toast.makeText(context, "已恢复 ${imported.size} 条记录", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "备份里没有记录", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                Toast.makeText(context, "备份文件格式不正确", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.size(22.dp))
            Text("我的", color = Ink, fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
            Text("把每一次记录，都好好放在身边", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 5.dp))
        }
        item {
            SectionCard("顺顺用户") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(82.dp).background(Meadow, CircleShape), contentAlignment = Alignment.Center) {
                        PoopMascot(64.dp, MascotMood.Happy)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("小顺顺", color = BrownDark, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text("本地生活记录 · ${records.size} 条", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
                        Text("今天也要轻轻松松呀", color = SageDark, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
        item {
            SectionCard("数据管理") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ActionRow(Icons.Outlined.Download, "导出本地备份") { exportLauncher.launch("顺顺备份.json") }
                    ActionRow(Icons.Outlined.FileUpload, "恢复本地备份") { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }
                    ActionRow(Icons.Outlined.DeleteOutline, "清除全部记录", Peach) { confirmClear = true }
                }
            }
        }
        item {
            SectionCard("生活小设置") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ToggleSettingRow(Icons.Outlined.WaterDrop, "喝水提醒", waterReminder) {
                        val next = !waterReminder
                        scope.launch { context.settingsDataStore.edit { it[WATER_REMINDER] = next } }
                        Toast.makeText(context, if (next) "喝水提醒已开启" else "喝水提醒已关闭", Toast.LENGTH_SHORT).show()
                    }
                    ToggleSettingRow(Icons.Outlined.CalendarMonth, "记录提醒", recordReminder) {
                        val next = !recordReminder
                        scope.launch { context.settingsDataStore.edit { it[RECORD_REMINDER] = next } }
                        Toast.makeText(context, if (next) "记录提醒已开启" else "记录提醒已关闭", Toast.LENGTH_SHORT).show()
                    }
                    ActionRow(Icons.Outlined.Info, "关于顺顺") { showAbout = true }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(top = 1.dp, bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("顺顺是一款个人生活记录工具", color = Muted, fontSize = 12.sp)
                Text("不构成医疗诊断或治疗建议", color = Muted.copy(alpha = .82f), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp), textAlign = TextAlign.Center)
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清除全部记录？") },
            text = { Text("删除后无法恢复，请确认已不再需要这些本地数据。") },
            confirmButton = {
                TextButton(onClick = { vm.clear(); confirmClear = false }) { Text("确认清除", color = Peach) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消", color = SageDark) }
            },
            containerColor = Cream
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("关于顺顺", color = BrownDark) },
            text = { Text("顺顺是一款温柔的个人生活记录工具，帮助你用几秒钟记下身体的小变化。所有记录都保存在本机。\n\n版本 1.5.0", color = Muted, lineHeight = 21.sp) },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("知道啦", color = SageDark) } },
            containerColor = Cream
        )
    }
}

@Composable
private fun ToggleSettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, checked: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = SageDark, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(13.dp))
        Text(label, color = Ink, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Text(if (checked) "开启" else "关闭", color = if (checked) SageDark else Muted, fontSize = 12.sp, modifier = Modifier.padding(end = 9.dp))
        val knobOffset by animateDpAsState(if (checked) 18.dp else 2.dp, label = "setting-$label")
        Box(Modifier.size(width = 42.dp, height = 24.dp).background(if (checked) Sage.copy(alpha = .72f) else Line, RoundedCornerShape(12.dp))) {
            Box(Modifier.padding(start = knobOffset).size(20.dp).background(if (checked) Color.White else Surface, CircleShape))
        }
    }
}
