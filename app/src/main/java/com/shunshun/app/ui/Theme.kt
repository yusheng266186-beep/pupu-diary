package com.shunshun.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cream = Color(0xFFFAF8F1)
val Surface = Color(0xFFFFFDF8)
val Sage = Color(0xFF86A85D)
val SageDark = Color(0xFF657F43)
val SageLight = Color(0xFFC8D9A8)
val Brown = Color(0xFF9A6545)
val BrownDark = Color(0xFF5D4537)
val Ink = Color(0xFF292A25)
val Muted = Color(0xFF74756E)
val Line = Color(0xFFEAE7DC)
val Peach = Color(0xFFF3A35C)
val Yellow = Color(0xFFF4C879)

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
    MaterialTheme(colorScheme = Colors, content = content)
}
