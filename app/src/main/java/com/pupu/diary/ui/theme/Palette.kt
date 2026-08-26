package com.pupu.diary.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

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
    val poopLight: Color
)

private val PoopC = Color(0xFFBE8E60)
private val PoopDarkC = Color(0xFF96683F)
private val PoopLightC = Color(0xFFDDB68F)

val PALETTES = listOf(
    AppPalette("鼠尾草", Color(0xFFF6F2E7), Color(0xFFFDFBF2), Color(0xFFEDEBDC),
        Color(0xFF8FAE7E), Color(0xFFDCE8CC), Color(0xFF5C7A4E),
        Color(0xFF47503C), Color(0xFF8C927B), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC),
    AppPalette("蜜桃", Color(0xFFFBF1EC), Color(0xFFFFFAF6), Color(0xFFF6E3D9),
        Color(0xFFE39A82), Color(0xFFF8DFD4), Color(0xFFB96A50),
        Color(0xFF5C463C), Color(0xFFA78D80), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC),
    AppPalette("晴空", Color(0xFFEFF4F8), Color(0xFFFAFDFF), Color(0xFFDFEAF2),
        Color(0xFF7FA8C9), Color(0xFFD7E6F2), Color(0xFF4F7796),
        Color(0xFF3C4A56), Color(0xFF8496A5), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC),
    AppPalette("薰衣草", Color(0xFFF4F1F9), Color(0xFFFDFBFF), Color(0xFFE7E1F0),
        Color(0xFFA48FC9), Color(0xFFE4DCF2), Color(0xFF6F5A96),
        Color(0xFF463D56), Color(0xFF9188A3), Color(0xFFF2C14E),
        PoopC, PoopDarkC, PoopLightC),
    AppPalette("落日", Color(0xFFFBF4E7), Color(0xFFFFFBF1), Color(0xFFF2E7D2),
        Color(0xFFD9A441), Color(0xFFF4E4C6), Color(0xFFA6761F),
        Color(0xFF54472E), Color(0xFF9C8E6E), Color(0xFFE58F5A),
        PoopC, PoopDarkC, PoopLightC)
)

val LocalAppPalette = staticCompositionLocalOf { PALETTES[0] }

/** 把 App 色板映射到 Material3 配色，让 TextButton/AlertDialog 等组件跟随主题而非默认紫色 */
fun AppPalette.toColorScheme(): androidx.compose.material3.ColorScheme =
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
        error = Color(0xFFD06A5A),
        onError = Color.White
    )

/** 主题切换时让所有颜色平滑过渡 */
@Composable
fun AnimatedAppPalette(target: AppPalette): AppPalette {
    @Composable
    fun a(c: Color) = animateColorAsState(
        targetValue = c,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
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
