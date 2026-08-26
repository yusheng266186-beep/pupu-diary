package com.shunshun.app.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.shunshun.app.PoopViewModel
import com.shunshun.app.PoopViewModelFactory
import com.shunshun.app.data.AppDatabase
import com.shunshun.app.data.PoopRepository
import com.shunshun.app.ui.Cream
import com.shunshun.app.ui.Line
import com.shunshun.app.ui.Muted
import com.shunshun.app.ui.SageDark
import com.shunshun.app.ui.SageLight
import com.shunshun.app.ui.Surface
import com.shunshun.app.ui.components.NavGlyph
import com.shunshun.app.ui.components.AppHaptic
import com.shunshun.app.ui.components.appClickable
import com.shunshun.app.ui.components.rememberAppHaptics
import com.shunshun.app.ui.home.HomeScreen
import com.shunshun.app.ui.profile.ProfileScreen
import com.shunshun.app.ui.record.RecordScreen
import com.shunshun.app.ui.statistics.StatisticsScreen

enum class AppTab(val title: String) { HOME("首页"), RECORD("记录"), STATS("统计"), PROFILE("我的") }

@Composable
fun ShunShunApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.get(context) }
    val repository = remember { PoopRepository(database.poopDao()) }
    val vm: PoopViewModel = viewModel(factory = PoopViewModelFactory(repository))
    val records by vm.records.collectAsState()
    var tabName by rememberSaveable { mutableStateOf(AppTab.HOME.name) }
    val tab = AppTab.valueOf(tabName)
    val snack = remember { SnackbarHostState() }
    Scaffold(containerColor = Cream, snackbarHost = { SnackbarHost(snack) }, bottomBar = { AppBottomBar(tab) { tabName = it.name } }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(tab, transitionSpec = { (fadeIn(tween(180))) togetherWith (fadeOut(tween(120))) }, label = "page") { page ->
                when (page) {
                    AppTab.HOME -> HomeScreen(records) { tabName = AppTab.RECORD.name }
                    AppTab.RECORD -> RecordScreen(records, onSaved = { vm.add(it); tabName = AppTab.HOME.name }, onDelete = vm::delete, onUpdate = vm::update)
                    AppTab.STATS -> StatisticsScreen(records)
                    AppTab.PROFILE -> ProfileScreen(records, vm)
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit) {
    val haptics = rememberAppHaptics()
    Row(Modifier.fillMaxWidth().background(Surface).border(1.dp, Line.copy(alpha = .55f)).padding(horizontal = 18.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        AppTab.values().forEach { tab ->
            val active = tab == selected
            val scale by animateFloatAsState(if (active) 1.08f else .94f, spring(dampingRatio = .72f), label = "tab-${tab.name}")
            Column(Modifier.width(62.dp).appClickable { if (!active) haptics(AppHaptic.Selection); onSelect(tab) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(40.dp).scale(scale).background(if (active) SageLight.copy(alpha = .72f) else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) { NavGlyph(tab, if (active) SageDark else Muted) }
                Text(tab.title, color = if (active) SageDark else Muted, fontSize = 10.sp, fontWeight = if (active) FontWeight.Medium else FontWeight.Normal)
            }
        }
    }
}
