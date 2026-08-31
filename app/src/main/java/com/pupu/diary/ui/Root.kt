package com.pupu.diary.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pupu.diary.data.PoopRecord
import com.pupu.diary.ui.components.BottomBar
import com.pupu.diary.ui.components.PoopCharacter
import com.pupu.diary.ui.components.Tab
import com.pupu.diary.ui.components.UndoBar
import com.pupu.diary.ui.screens.HomeScreen
import com.pupu.diary.ui.screens.ProfileScreen
import com.pupu.diary.ui.screens.RecordScreen
import com.pupu.diary.ui.screens.StatsScreen
import com.pupu.diary.ui.screens.TipsScreen
import com.pupu.diary.ui.theme.AnimatedAppPalette
import com.pupu.diary.ui.theme.DARK_PALETTES
import com.pupu.diary.ui.theme.LocalAppPalette
import com.pupu.diary.ui.theme.PALETTES
import com.pupu.diary.ui.theme.toColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppRoot(state: AppState, navTarget: androidx.compose.runtime.MutableState<String?>) {
    // 夜间模式：0 跟随系统 / 1 常开 / 2 常关；切换走 AnimatedAppPalette 的 450ms 全色过渡
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (state.darkMode) {
        1 -> true
        2 -> false
        else -> systemDark
    }
    val palette = AnimatedAppPalette(
        if (isDark) DARK_PALETTES[state.themeIndex.coerceIn(DARK_PALETTES.indices)] 
        else PALETTES[state.themeIndex.coerceIn(PALETTES.indices)]
    )
    androidx.compose.material3.MaterialTheme(colorScheme = palette.toColorScheme()) {
        CompositionLocalProvider(LocalAppPalette provides palette) {
            var showIntro by rememberSaveable { mutableStateOf(true) }
            AnimatedContent(
                targetState = showIntro,
                transitionSpec = {
                    fadeIn(tween(340)) togetherWith fadeOut(tween(300))
                },
                label = "introLayer"
            ) { intro ->
                if (intro) {
                    IntroScreen(onDone = { showIntro = false })
                } else {
                    MainScaffold(state, navTarget)
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(state: AppState, navTarget: androidx.compose.runtime.MutableState<String?>) {
    val palette = LocalAppPalette.current
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var showTips by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PoopRecord?>(null) }

    // 系统返回键/手势逐级返回：编辑页 → 贴士页 → 非首页 Tab → （再按才退出）
    androidx.activity.compose.BackHandler(enabled = editing != null) { editing = null }
    androidx.activity.compose.BackHandler(enabled = showTips) { showTips = false }
    androidx.activity.compose.BackHandler(enabled = !showTips && editing == null && tab != Tab.HOME) {
        tab = Tab.HOME
    }

    // 通知点击直达：记录提醒 → 记录页，喝水提醒 → 首页
    LaunchedEffect(navTarget.value) {
        when (navTarget.value) {
            "record" -> {
                showTips = false
                tab = Tab.RECORD
            }
            "home" -> {
                showTips = false
                tab = Tab.HOME
            }
        }
        navTarget.value = null
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(palette.bg)
    ) {
        Box(Modifier.weight(1f)) {
            AnimatedContent(
                targetState = showTips,
                transitionSpec = {
                    (fadeIn(tween(260)) togetherWith fadeOut(tween(200)))
                },
                label = "tipsLayer"
            ) { tips ->
                if (tips) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(palette.bg)
                            .statusBarsPadding()
                    ) {
                        TipsScreen(state, onBack = { showTips = false })
                    }
                } else {
                    AnimatedContent(
                        targetState = tab,
                        transitionSpec = {
                            val dir = if (targetState.ordinal >= initialState.ordinal) 1 else -1
                            (slideInHorizontally(tween(280)) { dir * it / 5 } + fadeIn(tween(260))) togetherWith
                                (slideOutHorizontally(tween(280)) { -dir * it / 5 } + fadeOut(tween(200)))
                        },
                        label = "tabLayer"
                    ) { t ->
                        Box(
                            Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                        ) {
                            when (t) {
                                Tab.HOME -> HomeScreen(
                                    state,
                                    onGoTab = { tab = it },
                                    onGoTips = { showTips = true }
                                )
                                Tab.RECORD -> RecordScreen(state, onDone = { tab = Tab.HOME })
                                Tab.STATS -> StatsScreen(state, onEdit = { editing = it })
                                Tab.MINE -> ProfileScreen(state)
                            }
                        }
                    }
                }
            }
            editing?.let { e ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(palette.bg)
                        .statusBarsPadding()
                ) {
                    RecordScreen(state, edit = e, onDone = { editing = null })
                }
            }
            // 删除撤销条（全 App 级，悬浮于内容区底部）
            UndoBar(
                visible = state.pendingUndo != null,
                onUndo = { state.undoLastDelete() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            )
        }
        Box(Modifier.navigationBarsPadding()) {
            BottomBar(current = tab, onSelect = { tab = it })
        }
    }
}

/** 开屏：角色落下 + 标题浮现 + 星星闪烁（点击任意处可跳过） */
@Composable
private fun IntroScreen(onDone: () -> Unit) {
    val palette = LocalAppPalette.current
    val drop = remember { Animatable(-700f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(36f) }

    LaunchedEffect(Unit) {
        launch { drop.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = 170f)) }
        launch {
            delay(420)
            titleAlpha.animateTo(1f, tween(420))
            titleOffset.animateTo(0f, tween(420, easing = FastOutSlowInEasing))
        }
        delay(1900)
        onDone()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(palette.bg)
            .pointerInput(Unit) { detectTapGestures { onDone() } },
        contentAlignment = Alignment.Center
    ) {
        // 闪烁的小星星
        Twinkle(0.18f, 0.22f, 900, 10f)
        Twinkle(0.80f, 0.30f, 1200, 8f)
        Twinkle(0.24f, 0.62f, 1500, 7f)
        Twinkle(0.74f, 0.66f, 1050, 9f)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.graphicsLayer { translationY = drop.value }) {
                PoopCharacter(size = 150.dp, celebrate = true)
            }
            Spacer(Modifier.height(22.dp))
            Column(
                Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleOffset.value
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("噗噗日记", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = palette.textMain)
                Spacer(Modifier.height(6.dp))
                Text("轻轻记录，轻松每一天", fontSize = 14.sp, color = palette.textSub)
                Spacer(Modifier.height(26.dp))
                Text("点击任意处进入", fontSize = 11.sp, color = palette.textSub.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun Twinkle(xFrac: Float, yFrac: Float, periodMs: Int, radius: Float) {
    val palette = LocalAppPalette.current
    val t = rememberInfiniteTransition(label = "twinkle")
    val alpha by t.animateFloat(
        initialValue = 0.15f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(periodMs, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "twinkleA"
    )
    Canvas(
        Modifier
            .fillMaxSize()
    ) {
        val cx = size.width * xFrac
        val cy = size.height * yFrac
        val r = radius * (1f + 0.15f * alpha)
        val star = Path().apply {
            moveTo(cx, cy - r)
            lineTo(cx + r * 0.32f, cy - r * 0.32f)
            lineTo(cx + r, cy)
            lineTo(cx + r * 0.32f, cy + r * 0.32f)
            lineTo(cx, cy + r)
            lineTo(cx - r * 0.32f, cy + r * 0.32f)
            lineTo(cx - r, cy)
            lineTo(cx - r * 0.32f, cy - r * 0.32f)
            close()
        }
        drawPath(star, palette.accent.copy(alpha = alpha), style = Fill)
    }
}
