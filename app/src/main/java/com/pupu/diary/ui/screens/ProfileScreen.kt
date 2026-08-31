@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.pupu.diary.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.pupu.diary.BuildConfig
import com.pupu.diary.ui.AppState
import com.pupu.diary.ui.components.PoopCharacter
import com.pupu.diary.ui.components.PupuDialog
import com.pupu.diary.ui.components.PupuStepper
import com.pupu.diary.ui.components.SoftCard
import com.pupu.diary.ui.theme.LocalAppPalette
import com.pupu.diary.ui.theme.PALETTES
import com.pupu.diary.util.Backup
import com.pupu.diary.util.Csv
import com.pupu.diary.util.minutesToCn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 「我的」页设置面板：reminder / water / privacy / backup / theme */
@Composable
fun ProfileScreen(state: AppState) {
    val palette = LocalAppPalette.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var panel by remember { mutableStateOf<String?>(null) }
    var showAbout by remember { mutableStateOf(false) }
    var showNameEdit by remember { mutableStateOf(false) }
    var nameDraft by remember { mutableStateOf("") }
    var showRecordTime by remember { mutableStateOf(false) }
    var showWaterTime by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showRestorePick by remember { mutableStateOf(false) }
    var showCrashLog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    // v4.0.0：通知权限被永久拒绝后的出路提示（permTick 在授权结果回调时自增触发重算）
    var permTick by remember { mutableIntStateOf(0) }
    val notifBlocked = remember(permTick) {
        val act = context as? android.app.Activity
        Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED &&
            (act == null || !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                act, Manifest.permission.POST_NOTIFICATIONS
            ))
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permTick++ }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->        if (uri != null) {
            scope.launch {
                val json = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)
                            ?.bufferedReader()?.readText()
                    } catch (_: Exception) {
                        null
                    }
                }
                if (json.isNullOrBlank()) {
                    Toast.makeText(context, "读取文件失败", Toast.LENGTH_SHORT).show()
                } else {
                    state.importFromJson(json) { added ->
                        Toast.makeText(
                            context,
                            if (added > 0) "已恢复 $added 条记录" else "没有新记录可导入",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    try {
                        val recs = state.records.toList()
                        // v4.0.0：表头元信息（版本/导出日/条数/范围），医生一眼可辨数据出处
                        val meta = mutableListOf(
                            "由噗噗日记 v${BuildConfig.VERSION_NAME} 导出",
                            "导出日期：${com.pupu.diary.util.todayKey()}",
                            "记录条数：${recs.size}"
                        )
                        if (recs.isNotEmpty()) {
                            meta.add(
                                "日期范围：${com.pupu.diary.util.formatDateCn(recs.last().time)} ~ ${com.pupu.diary.util.formatDateCn(recs.first().time)}"
                            )
                        }
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(Csv.build(recs, meta))
                        }
                        true
                    } catch (_: Exception) {
                        false
                    }
                }
                Toast.makeText(
                    context,
                    if (ok) "CSV 已导出，可发给医生或用表格软件打开" else "导出失败",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    fun ensureNotifPerm() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text("我的", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = palette.textMain)
        Spacer(Modifier.height(14.dp))

        // 头像卡（轻点设置昵称）
        SoftCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    nameDraft = state.userName
                    showNameEdit = true
                },
            corner = 24.dp
        ) {
            Row(
                Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(palette.primarySoft),
                    contentAlignment = Alignment.Center
                ) {
                    PoopCharacter(size = 46.dp, breathing = false)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        state.userName.ifBlank { "小治愈" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textMain
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "ID：${state.prefs.userId} · 记录 ${state.records.size} 条",
                        fontSize = 12.sp,
                        color = palette.textSub
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "轻点这里设置昵称 · 今天也要轻轻松松呀",
                        fontSize = 12.sp,
                        color = palette.primaryDeep
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        // 设置列表（对齐参考稿的六行式）
        SoftCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
            Column {
                SettingRow(
                    iconKind = 0,
                    title = "记录提醒",
                    value = if (state.recordRemindOn) minutesToCn(state.recordRemindMin) else "已关闭"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    panel = "record"
                }
                RowDivider()
                SettingRow(
                    iconKind = 1,
                    title = "喝水提醒",
                    value = if (state.waterRemindOn) minutesToCn(state.waterRemindMin) else "已关闭"
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    panel = "water"
                }
                RowDivider()
                SettingRow(iconKind = 2, title = "隐私设置") {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    panel = "privacy"
                }
                RowDivider()
                SettingRow(
                    iconKind = 3,
                    title = "数据备份",
                    value = if (state.autoBackupOn) "自动备份中" else null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    panel = "backup"
                }
                RowDivider()
                SettingRow(
                    iconKind = 4,
                    title = "主题颜色",
                    trailing = {
                        // v4.0.0：预览点显示全部 5 套主题（此前只画了 4 个）
                        Row {
                            PALETTES.forEach { p ->
                                Box(
                                    Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(11.dp)
                                        .clip(CircleShape)
                                        .background(p.primary)
                                )
                            }
                        }
                    }
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    panel = "theme"
                }
                RowDivider()
                SettingRow(iconKind = 5, title = "关于我们") {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAbout = true
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "噗噗日记 v${BuildConfig.VERSION_NAME} · 数据仅保存在本机",
            fontSize = 11.sp,
            color = palette.textSub,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(20.dp))
    }

    when (panel) {
        "record" -> Sheet(onDismiss = { panel = null }) {
            SheetTitle("记录提醒")
            SheetSwitchRow(
                text = "每天到点提醒我记录",
                checked = state.recordRemindOn,
                onToggle = { on ->
                    if (on) ensureNotifPerm()
                    state.setRecordReminder(context, on, state.recordRemindMin)
                }
            )
            NotifBlockedHint(notifBlocked)
            SheetTimeRow(
                enabled = state.recordRemindOn,
                timeText = minutesToCn(state.recordRemindMin),
                onClick = { showRecordTime = true }
            )
            SheetHint("固定时间记录，更容易发现身体的规律哦～")
        }
        "water" -> Sheet(onDismiss = { panel = null }) {
            SheetTitle("喝水提醒")
            SheetSwitchRow(
                text = "每天到点提醒我喝水",
                checked = state.waterRemindOn,
                onToggle = { on ->
                    if (on) ensureNotifPerm()
                    state.setWaterReminder(context, on, state.waterRemindMin)
                }
            )
            NotifBlockedHint(notifBlocked)
            SheetTimeRow(
                enabled = state.waterRemindOn,
                timeText = minutesToCn(state.waterRemindMin),
                onClick = { showWaterTime = true }
            )
            SheetHint("充足的水分让肠道更润滑，便便更顺畅～")
        }
        "privacy" -> Sheet(onDismiss = { panel = null }) {
            SheetTitle("隐私设置")
            SheetSwitchRow(
                text = "隐私模式",
                checked = state.privacyMode,
                onToggle = { state.setPrivacy(it) }
            )
            SheetHint("开启后，切换到最近任务时隐藏页面内容，防止旁人偷看你的小秘密。")
            Spacer(Modifier.height(6.dp))
            SheetTitle("夜间模式")
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf("跟随系统", "常开", "常关").forEachIndexed { i, label ->
                    val selected = state.darkMode == i
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (selected) palette.primarySoft else palette.cardAlt
                    ) {
                        Text(
                            label,
                            modifier = Modifier
                                .clickable { state.updateDarkMode(i) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) palette.primaryDeep else palette.textSub
                        )
                    }
                }
            }
            SheetHint("夜间为暖炭色深色主题，切换时全 App 颜色平滑过渡。")
        }
        "backup" -> Sheet(onDismiss = { panel = null }) {
            SheetTitle("数据备份")
            SheetActionRow(iconKind = 3, title = "导出备份", sub = "JSON 保存到下载目录，可分享") {
                panel = null
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        Backup.export(context, state.records.toList())
                    }
                    Toast.makeText(context, "已导出到：${result.displayPath}", Toast.LENGTH_LONG).show()
                    // 拉起系统分享，方便直接发给微信/QQ/网盘保存
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(android.content.Intent.EXTRA_STREAM, result.uri)
                        clipData = android.content.ClipData.newRawUri("backup", result.uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(
                            android.content.Intent.createChooser(send, "分享备份文件")
                        )
                    } catch (_: Exception) {
                    }
                }
            }
            SheetActionRow(iconKind = 6, title = "导入恢复", sub = "从备份文件合并记录") {
                panel = null
                importLauncher.launch(arrayOf("*/*"))
            }
            SheetActionRow(iconKind = 3, title = "导出 CSV（医生查看）", sub = "表格形式，含形状/颜色/腹痛等全部字段") {
                panel = null
                csvLauncher.launch("噗噗日记_${com.pupu.diary.util.todayKey()}.csv")
            }
            SheetSwitchRow(
                text = "每周自动备份",
                checked = state.autoBackupOn,
                onToggle = { state.setAutoBackup(it) }
            )
            // v4.0.0：自动备份此前只能去文件系统找，现在应用内可恢复；崩溃日志可见可分享
            SheetActionRow(iconKind = 6, title = "从自动备份恢复", sub = "直接选用应用内保存的每周备份") {
                panel = null
                showRestorePick = true
            }
            SheetActionRow(iconKind = 5, title = "崩溃诊断日志", sub = "应用异常时反馈用，可查看并分享", danger = false) {
                panel = null
                showCrashLog = true
            }
            SheetActionRow(iconKind = 8, title = "清空所有记录", sub = "不可恢复，建议先导出备份", danger = true) {
                panel = null
                showClearConfirm = true
            }
            SheetHint("所有数据仅保存在你的手机里，换机前记得先导出哦。")
        }
        "theme" -> Sheet(onDismiss = { panel = null }) {
            SheetTitle("主题颜色")
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PALETTES.forEachIndexed { i, p ->
                    val selected = state.themeIndex == i
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(46.dp)
                                .graphicsLayer {
                                    scaleX = if (selected) 1.12f else 1f
                                    scaleY = if (selected) 1.12f else 1f
                                }
                                .clip(CircleShape)
                                .background(p.primary)
                                .border(
                                    width = if (selected) 3.dp else 2.dp,
                                    color = if (selected) p.primaryDeep else palette.cardAlt,
                                    shape = CircleShape
                                )
                                .clickable { state.setTheme(i) }
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            p.name,
                            fontSize = 12.sp,
                            color = if (selected) palette.primaryDeep else palette.textSub,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            SheetHint("切换时整个应用的颜色会轻轻过渡，挑一个今天的心情吧～")
        }
    }

    if (showRecordTime) {
        TimeDialog(
            title = "记录提醒时间",
            initialMinutes = state.recordRemindMin,
            onDismiss = { showRecordTime = false }
        ) { h, m ->
            state.setRecordReminder(context, true, h * 60 + m)
        }
    }
    if (showWaterTime) {
        TimeDialog(
            title = "喝水提醒时间",
            initialMinutes = state.waterRemindMin,
            onDismiss = { showWaterTime = false }
        ) { h, m ->
            state.setWaterReminder(context, true, h * 60 + m)
        }
    }

    if (showClearConfirm) {
        PupuDialog(
            onDismissRequest = { showClearConfirm = false },
            title = "清空所有记录？",
            content = {
                Text(
                    "将删除全部 ${state.records.size} 条记录，且无法恢复。确定继续吗？",
                    fontSize = 14.sp,
                    color = palette.textSub,
                    lineHeight = 20.sp
                )
            },
            confirmText = "清空",
            confirmColor = Color(0xFFD06A5A),
            onConfirm = {
                state.clearAllRecords()
                showClearConfirm = false
                Toast.makeText(context, "已清空所有记录", Toast.LENGTH_SHORT).show()
            },
            dismissText = "取消"
        )
    }

    if (showNameEdit) {
        PupuDialog(
            onDismissRequest = { showNameEdit = false },
            title = "给自己起个昵称",
            content = {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { if (it.length <= 12) nameDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("最多 12 个字，留空则恢复默认", fontSize = 13.sp, color = palette.textSub)
                    },
                    shape = RoundedCornerShape(18.dp),
                    maxLines = 1,
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
            },
            confirmText = "保存",
            onConfirm = {
                state.updateUserName(nameDraft)
                Toast.makeText(context, "昵称已保存，首页会这样叫你哦", Toast.LENGTH_SHORT).show()
            },
            dismissText = "取消"
        )
    }

    // v4.0.0：应用内自动备份恢复（不用再去文件系统翻 JSON）
    if (showRestorePick) {
        val files = remember { Backup.autoBackupFiles(context) }
        PupuDialog(
            onDismissRequest = { showRestorePick = false },
            title = "从自动备份恢复",
            content = {
                Column(Modifier.heightIn(max = 320.dp)) {
                    if (files.isEmpty()) {
                        Text("还没有自动备份文件～开启「每周自动备份」后，应用里会悄悄存最近 4 份。",
                            fontSize = 13.sp, color = palette.textSub, lineHeight = 19.sp)
                    } else {
                        Text("选择一份备份导入（按时间去重合并，不会弄丢现有记录）：",
                            fontSize = 12.sp, color = palette.textSub)
                        Spacer(Modifier.height(6.dp))
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            files.forEach { f ->
                                val day = f.name.removePrefix("pupu_auto_").removeSuffix(".json")
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            showRestorePick = false
                                            val json = try { f.readText() } catch (_: Exception) { null }
                                            if (json.isNullOrBlank()) {
                                                Toast.makeText(context, "读取备份失败", Toast.LENGTH_SHORT).show()
                                            } else {
                                                state.importFromJson(json) { added ->
                                                    Toast.makeText(
                                                        context,
                                                        if (added > 0) "已从 $day 恢复 $added 条记录" else "这份备份里没有新记录",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                        .padding(vertical = 11.dp, horizontal = 10.dp)
                                ) {
                                    Text(
                                        "${day.take(4)}-${day.drop(4).take(2)}-${day.takeLast(2)} 的备份",
                                        fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textMain
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmText = "关闭"
        )
    }

    // v4.0.0：崩溃诊断日志应用内查看与分享（此前只能 adb 取）
    if (showCrashLog) {
        val log = remember { com.pupu.diary.util.CrashLog.tail(context) }
        PupuDialog(
            onDismissRequest = { showCrashLog = false },
            title = "崩溃诊断日志",
            content = {
                if (log.isBlank()) {
                    Text("没有崩溃记录，应用很健康～", fontSize = 13.sp, color = palette.textSub)
                } else {
                    Text(
                        log,
                        fontSize = 9.sp,
                        color = palette.textSub,
                        lineHeight = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmText = "分享日志",
            onConfirm = {
                showCrashLog = false
                try {
                    val cache = java.io.File(context.cacheDir, "pupu_diary_crash_log.txt")
                    cache.writeText(log.ifBlank { "empty" })
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        context, "${context.packageName}.files", cache
                    )
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, log)
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        clipData = android.content.ClipData.newRawUri("log", uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(android.content.Intent.createChooser(send, "分享诊断日志"))
                } catch (_: Exception) {
                    Toast.makeText(context, "分享失败", Toast.LENGTH_SHORT).show()
                }
            },
            dismissText = "关闭"
        )
    }

    if (showAbout) {
        PupuDialog(
            onDismissRequest = { showAbout = false },
            title = "关于 噗噗日记",
            content = {
                Text(
                    "版本 v${BuildConfig.VERSION_NAME}\n\n一个温柔的健康记录小本本。所有数据只保存在你的手机里，随时可以导出备份。\n\n本应用仅供健康记录参考，不构成医疗建议；如有持续不适应及时就医。",
                    fontSize = 14.sp,
                    color = palette.textSub,
                    lineHeight = 21.sp
                )
            },
            confirmText = "知道啦"
        )
    }
}

/** 通知权限被永久拒绝时，提醒面板里的自救入口（v4.0.0） */
@Composable
private fun NotifBlockedHint(blocked: Boolean) {
    if (!blocked) return
    val palette = LocalAppPalette.current
    val context = LocalContext.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(androidx.compose.ui.graphics.Color(0x1AD06A5A))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                try {
                    context.startActivity(
                        android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                    )
                } catch (_: Exception) {
                }
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "系统通知权限未开启，到点不会响",
            fontSize = 12.sp, color = Color(0xFFD06A5A), modifier = Modifier.weight(1f)
        )
        Text("去设置 ›", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD06A5A))
    }
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun RowDivider() {
    val palette = LocalAppPalette.current
    HorizontalDivider(color = palette.cardAlt, thickness = 1.dp, modifier = Modifier.padding(start = 58.dp))
}

/** 设置行：圆形图标 + 标题 + 值/尾部组件 + 右箭头 */
@Composable
private fun SettingRow(
    iconKind: Int,
    title: String,
    value: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val palette = LocalAppPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconChip(iconKind)
        Spacer(Modifier.width(10.dp))
        Text(title, fontSize = 15.sp, color = palette.textMain)
        if (value != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (value == "已关闭") palette.textSub else palette.primaryDeep
            )
        }
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
        Spacer(Modifier.width(8.dp))
        Chevron(palette.textSub.copy(alpha = 0.7f))
    }
}

@Composable
private fun Chevron(color: Color) {
    Canvas(Modifier.size(width = 8.dp, height = 14.dp)) {
        val p = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.16f)
            lineTo(size.width * 0.78f, size.height * 0.5f)
            lineTo(size.width * 0.25f, size.height * 0.84f)
        }
        drawPath(p, color, style = Stroke(size.width * 0.32f, cap = StrokeCap.Round))
    }
}

@Composable
private fun Sheet(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val palette = LocalAppPalette.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.card,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 34.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    val palette = LocalAppPalette.current
    Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = palette.textMain)
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun SheetHint(text: String) {
    val palette = LocalAppPalette.current
    Spacer(Modifier.height(12.dp))
    Text(text, fontSize = 12.sp, color = palette.textSub, lineHeight = 17.sp)
}

@Composable
private fun SheetSwitchRow(text: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    val palette = LocalAppPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 15.sp, color = palette.textMain, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedTrackColor = palette.primary,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = palette.cardAlt,
                uncheckedThumbColor = Color.White,
                uncheckedBorderColor = palette.cardAlt
            )
        )
    }
}

@Composable
private fun SheetTimeRow(enabled: Boolean, timeText: String, onClick: () -> Unit) {
    val palette = LocalAppPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "提醒时间",
            fontSize = 15.sp,
            color = if (enabled) palette.textMain else palette.textSub
        )
        Spacer(Modifier.weight(1f))
        Text(
            timeText,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) palette.primaryDeep else palette.textSub
        )
    }
}

@Composable
private fun SheetActionRow(
    iconKind: Int,
    title: String,
    sub: String? = null,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    val palette = LocalAppPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconChip(iconKind, danger = danger)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 15.sp,
                color = if (danger) Color(0xFFD06A5A) else palette.textMain
            )
            if (sub != null) {
                Text(sub, fontSize = 11.sp, color = palette.textSub)
            }
        }
    }
}

@Composable
private fun IconChip(kind: Int, danger: Boolean = false) {
    val palette = LocalAppPalette.current
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (danger) Color(0x1AD06A5A) else palette.primarySoft),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(16.dp)) {
            val w = size.width
            val h = size.height
            val c = if (danger) Color(0xFFD06A5A) else palette.primaryDeep
            when (kind) {
                0 -> { // 铃铛
                    val dome = Path().apply {
                        moveTo(w * 0.2f, h * 0.62f)
                        cubicTo(w * 0.2f, h * 0.28f, w * 0.36f, h * 0.16f, w * 0.5f, h * 0.16f)
                        cubicTo(w * 0.64f, h * 0.16f, w * 0.8f, h * 0.28f, w * 0.8f, h * 0.62f)
                        close()
                    }
                    drawPath(dome, c)
                    drawArc(
                        c, 0f, 180f, false,
                        topLeft = Offset(w * 0.38f, h * 0.64f),
                        size = Size(w * 0.24f, h * 0.22f),
                        style = Stroke(w * 0.1f, cap = StrokeCap.Round)
                    )
                }
                1 -> { // 水滴
                    val drop = Path().apply {
                        moveTo(w * 0.5f, h * 0.1f)
                        cubicTo(w * 0.72f, h * 0.4f, w * 0.82f, h * 0.52f, w * 0.82f, h * 0.64f)
                        cubicTo(w * 0.82f, h * 0.86f, w * 0.18f, h * 0.86f, w * 0.18f, h * 0.64f)
                        cubicTo(w * 0.18f, h * 0.52f, w * 0.28f, h * 0.4f, w * 0.5f, h * 0.1f)
                        close()
                    }
                    drawPath(drop, c)
                }
                2 -> { // 眼睛（隐私）
                    drawOval(
                        c,
                        topLeft = Offset(w * 0.08f, h * 0.28f),
                        size = Size(w * 0.84f, h * 0.44f),
                        style = Stroke(w * 0.09f)
                    )
                    drawCircle(c, radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.5f))
                }
                3 -> { // 盒子（备份）
                    drawRoundRect(
                        c,
                        topLeft = Offset(w * 0.15f, h * 0.4f),
                        size = Size(w * 0.7f, h * 0.45f),
                        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                        style = Stroke(w * 0.09f)
                    )
                    drawLine(c, Offset(w * 0.08f, h * 0.4f), Offset(w * 0.92f, h * 0.4f), w * 0.09f, StrokeCap.Round)
                    drawLine(c, Offset(w * 0.5f, h * 0.1f), Offset(w * 0.5f, h * 0.28f), w * 0.1f, StrokeCap.Round)
                }
                4 -> { // 调色板
                    drawCircle(c, radius = w * 0.38f, style = Stroke(w * 0.09f))
                    drawCircle(c, w * 0.07f, Offset(w * 0.38f, h * 0.4f))
                    drawCircle(c, w * 0.07f, Offset(w * 0.64f, h * 0.4f))
                    drawCircle(c, w * 0.07f, Offset(w * 0.5f, h * 0.64f))
                }
                5 -> { // 信息
                    drawCircle(c, radius = w * 0.4f, style = Stroke(w * 0.09f))
                    drawCircle(c, w * 0.07f, Offset(w * 0.5f, h * 0.28f))
                    drawLine(c, Offset(w * 0.5f, h * 0.44f), Offset(w * 0.5f, h * 0.7f), w * 0.11f, StrokeCap.Round)
                }
                6 -> { // 下载（导入）
                    drawLine(c, Offset(w * 0.5f, h * 0.12f), Offset(w * 0.5f, h * 0.58f), w * 0.1f, StrokeCap.Round)
                    drawLine(c, Offset(w * 0.32f, h * 0.42f), Offset(w * 0.5f, h * 0.6f), w * 0.1f, StrokeCap.Round)
                    drawLine(c, Offset(w * 0.68f, h * 0.42f), Offset(w * 0.5f, h * 0.6f), w * 0.1f, StrokeCap.Round)
                    drawLine(c, Offset(w * 0.18f, h * 0.82f), Offset(w * 0.82f, h * 0.82f), w * 0.1f, StrokeCap.Round)
                }
                8 -> { // 垃圾桶（清空）
                    drawLine(c, Offset(w * 0.14f, h * 0.22f), Offset(w * 0.86f, h * 0.22f), w * 0.1f, StrokeCap.Round)
                    drawLine(c, Offset(w * 0.5f, h * 0.1f), Offset(w * 0.5f, h * 0.22f), w * 0.1f, StrokeCap.Round)
                    drawRoundRect(
                        c,
                        topLeft = Offset(w * 0.24f, h * 0.32f),
                        size = Size(w * 0.52f, h * 0.56f),
                        cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                        style = Stroke(w * 0.09f)
                    )
                    drawLine(c, Offset(w * 0.42f, h * 0.46f), Offset(w * 0.42f, h * 0.74f), w * 0.08f, StrokeCap.Round)
                    drawLine(c, Offset(w * 0.58f, h * 0.46f), Offset(w * 0.58f, h * 0.74f), w * 0.08f, StrokeCap.Round)
                }
                else -> { // 循环（自动备份）
                    drawArc(
                        c, -60f, 280f, false,
                        topLeft = Offset(w * 0.14f, h * 0.14f),
                        size = Size(w * 0.72f, h * 0.72f),
                        style = Stroke(w * 0.1f, cap = StrokeCap.Round)
                    )
                    drawCircle(c, radius = w * 0.09f, center = Offset(w * 0.82f, h * 0.3f))
                }
            }
        }
    }
}

/** App 风格时间选择：上下步进 + 快捷时间片（替代安卓原生 TimePicker） */
@Composable
private fun TimeDialog(
    title: String,
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val palette = LocalAppPalette.current
    var hour by remember { mutableStateOf(initialMinutes / 60) }
    var minute by remember { mutableStateOf(initialMinutes % 60) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = palette.card
        ) {
            Column(Modifier.padding(22.dp)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textMain
                )
                Spacer(Modifier.height(18.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PupuStepper("时", hour, 0, 23) { hour = it }
                    Spacer(Modifier.width(16.dp))
                    Text(":", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = palette.textSub)
                    Spacer(Modifier.width(16.dp))
                    PupuStepper("分", minute, 0, 59) { minute = it }
                }
                Spacer(Modifier.height(16.dp))
                Text("快捷选择", fontSize = 11.sp, color = palette.textSub)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(8 * 60 to "08:00", 12 * 60 to "12:00", 20 * 60 + 30 to "20:30", 22 * 60 to "22:00")
                        .forEach { (mins, label) ->
                            val selected = hour * 60 + minute == mins
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) palette.primarySoft else palette.cardAlt)
                                    .clickable { hour = mins / 60; minute = mins % 60 }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) palette.primaryDeep else palette.textSub
                                )
                            }
                        }
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("取消", color = palette.textSub) }
                    TextButton(onClick = {
                        onConfirm(hour, minute)
                        onDismiss()
                    }) { Text("确定", fontWeight = FontWeight.SemiBold, color = palette.primary) }
                }
            }
        }
    }
}

// 步进器已抽到 components/PupuStepper（v4.0.0，含长按连发），此处复用
