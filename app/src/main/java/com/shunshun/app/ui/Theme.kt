package com.shunshun.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Cream = Color(0xFFFCFBF6)
val Surface = Color(0xFFFFFDF8)
val Sage = Color(0xFF96B66D)
val SageDark = Color(0xFF6F8D51)
val SageLight = Color(0xFFE1EBCF)
val Meadow = Color(0xFFF0F3E5)
val Brown = Color(0xFFA97050)
val BrownDark = Color(0xFF60483D)
val Ink = Color(0xFF34322E)
val Muted = Color(0xFF8A8982)
val Line = Color(0xFFE8E6DC)
val Peach = Color(0xFFEBAF83)
val Yellow = Color(0xFFF5C873)
val Lilac = Color(0xFFE9E2F7)
val BlueMist = Color(0xFFDDEBF1)

private val Colors = lightColorScheme(
    primary = Sage,
    onPrimary = Color.White,
    primaryContainer = SageLight,
    onPrimaryContainer = SageDark,
    secondary = Peach,
    background = Cream,
    surface = Surface,
    onBackground = Ink,
    onSurface = Ink,
    outline = Line
)

@Composable
fun ShunShunTheme(content: @Composable () -> Unit) {
    val typography = Typography(
        displaySmall = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 29.sp, lineHeight = 37.sp),
        headlineSmall = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 23.sp, lineHeight = 30.sp),
        titleLarge = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 25.sp),
        bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp),
        labelLarge = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    )
    MaterialTheme(colorScheme = Colors, typography = typography, content = content)
}
