package com.shunshun.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Cream = Color(0xFFFAF8F2)
val Surface = Color(0xFFFFFEFA)
val Sage = Color(0xFF91B468)
val SageDark = Color(0xFF637F49)
val SageLight = Color(0xFFD8E6BE)
val Meadow = Color(0xFFEAF1DC)
val Brown = Color(0xFFA86D4B)
val BrownDark = Color(0xFF65493B)
val Ink = Color(0xFF302E2A)
val Muted = Color(0xFF85847D)
val Line = Color(0xFFE8E4D9)
val Peach = Color(0xFFF0AA78)
val Yellow = Color(0xFFF7CA77)
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
        displaySmall = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 38.sp),
        headlineSmall = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 31.sp),
        titleLarge = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 25.sp),
        bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp),
        labelLarge = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    )
    MaterialTheme(colorScheme = Colors, typography = typography, content = content)
}
