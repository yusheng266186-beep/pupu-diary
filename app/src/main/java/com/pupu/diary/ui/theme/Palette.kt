package com.pupu.diary.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** 系统「移除动画/减弱动态效果」开关，AppRoot 提供；动效点按需读取做短路 */
val LocalReduceMotion = androidx.compose.runtime.staticCompositionLocalOf { false }

data class AppPalette(
    val name: String,
    val bg: Color,
    val card: Color,
    val cardAlt: Color,
    val primary: Color,
    val primarySoft: Color,
    val primaryDeep: Color,
    val textMain: Color,
    val textSub: Color,
    val accent: Color,
    val poop: Color,
    val poopDark: Color,
    val poopLight: Color,
    val isDark: Boolean = false
)

private val PoopC = Color(0xFFBE8E60)
private val PoopDarkC = Color(0xFF96683F)
private val PoopLightC = Color(0xFFDDB68F)

// v4.0.0：textSub 统一加深一档，小字号下对比度更接近 WCAG AA
val PALETTES = listOf(
    AppPalette("鼠尾草", Color(0xFFF6F2E7), Color(0xFFFDFBF2), Color(0xFFEDEBDC),
        Color(0xFF8FAE7E), Color(0xFFDCE8CC), Color(0xFF5C7A4E),
        Color(0xFF47503C), Color(0xFF7B8069), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC),
    AppPalette("蜜桃", Color(0xFFFBF1EC), Color(0xFFFFFAF6), Color(0xFFF6E3D9),
        Color(0xFFE39A82), Color(0xFFF8DFD4), Color(0xFFB96A50),
        Color(0xFF5C463C), Color(0xFF94796C), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC),
    AppPalette("晴空", Color(0xFFEFF4F8), Color(0xFFFAFDFF), Color(0xFFDFEAF2),
        Color(0xFF7FA8C9), Color(0xFFD7E6F2), Color(0xFF4F7796),
        Color(0xFF3C4A56), Color(0xFF728494), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC),
    AppPalette("薰衣草", Color(0xFFF4F1F9), Color(0xFFFDFBFF), Color(0xFFE7E1F0),
        Color(0xFFA48FC9), Color(0xFFE4DCF2), Color(0xFF6F5A96),
        Color(0xFF463D56), Color(0xFF7F7691), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC),
    AppPalette("落日", Color(0xFFFBF4E7), Color(0xFFFFFBF1), Color(0xFFF2E7D2),
        Color(0xFFD9A441), Color(0xFFF4E4C6), Color(0xFFA6761F),
        Color(0xFF54472E), Color(0xFF8A7C5C), Color(0xFFE58F5A),
        PoopC, PoopDarkC, PoopLightC)
)

/** 夜间色板：与 PALETTES 一一对应（同索引同主题），暖炭底 + 同色相低饱和，插画棕保持不变 */
val DARK_PALETTES = listOf(
    AppPalette("鼠尾草", Color(0xFF1B2017), Color(0xFF252B21), Color(0xFF303729),
        Color(0xFF8FAE7E), Color(0xFF2D3A28), Color(0xFFC4D6AE),
        Color(0xFFE6E4D6), Color(0xFF99A08D), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC, isDark = true),
    AppPalette("蜜桃", Color(0xFF241C18), Color(0xFF2F2620), Color(0xFF3B2F28),
        Color(0xFFE39A82), Color(0xFF3E2D26), Color(0xFFF2C6B4),
        Color(0xFFF1E7E0), Color(0xFFA8978D), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC, isDark = true),
    AppPalette("晴空", Color(0xFF161C23), Color(0xFF1F262E), Color(0xFF29323C),
        Color(0xFF7FA8C9), Color(0xFF243039), Color(0xFFC6D9E9),
        Color(0xFFE3E9EE), Color(0xFF8D99A5), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC, isDark = true),
    AppPalette("薰衣草", Color(0xFF1E1A26), Color(0xFF282231), Color(0xFF342C41),
        Color(0xFFA48FC9), Color(0xFF302A42), Color(0xFFD7CBEA),
        Color(0xFFEAE5F1), Color(0xFF9B93A9), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC, isDark = true),
    AppPalette("落日", Color(0xFF221D13), Color(0xFF2C2618), Color(0xFF38301F),
        Color(0xFFD9A441), Color(0xFF372F1D), Color(0xFFF0DCAA),
        Color(0xFFF0EAD8), Color(0xFFA2957B), Color(0xFFE58F5A),
        PoopC, PoopDarkC, PoopLightC, isDark = true)
)

val LocalAppPalette = staticCompositionLocalOf { PALETTES[0] }

/** 把 App 色板映射到 Material3 配色，让 TextButton/AlertDialog 等组件跟随主题而非默认紫色 */
fun AppPalette.toColorScheme(): androidx.compose.material3.ColorScheme {
    val error = Color(0xFFD06A5A)
    return if (isDark) {
        androidx.compose.material3.darkColorScheme(
            primary = primary,
            onPrimary = bg,
            primaryContainer = primarySoft,
            onPrimaryContainer = primaryDeep,
            secondary = primary,
            onSecondary = bg,
            secondaryContainer = primarySoft,
            onSecondaryContainer = primaryDeep,
            background = bg,
            onBackground = textMain,
            surface = card,
            onSurface = textMain,
            surfaceVariant = cardAlt,
            onSurfaceVariant = textSub,
            outline = cardAlt,
            outlineVariant = cardAlt,
            error = error,
            onError = Color.White
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primarySoft,
            onPrimaryContainer = primaryDeep,
            secondary = primary,
            onSecondary = Color.White,
            secondaryContainer = primarySoft,
            onSecondaryContainer = primaryDeep,
            background = bg,
            onBackground = textMain,
            surface = card,
            onSurface = textMain,
            surfaceVariant = cardAlt,
            onSurfaceVariant = textSub,
            outline = cardAlt,
            outlineVariant = cardAlt,
            error = error,
            onError = Color.White
        )
    }
}

/** 主题切换时让所有颜色平滑过渡（系统「移除动画」开启时改为直接切换） */
@Composable
fun AnimatedAppPalette(target: AppPalette): AppPalette {
    val duration = if (LocalReduceMotion.current) 0 else 450
    @Composable
    fun a(c: Color) = animateColorAsState(
        targetValue = c,
        animationSpec = tween(duration, easing = FastOutSlowInEasing),
        label = "palette"
    ).value

    return AppPalette(
        name = target.name,
        bg = a(target.bg),
        card = a(target.card),
        cardAlt = a(target.cardAlt),
        primary = a(target.primary),
        primarySoft = a(target.primarySoft),
        primaryDeep = a(target.primaryDeep),
        textMain = a(target.textMain),
        textSub = a(target.textSub),
        accent = a(target.accent),
        poop = a(target.poop),
        poopDark = a(target.poopDark),
        poopLight = a(target.poopLight)
    )
}
