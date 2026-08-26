package com.shunshun.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback

enum class AppHaptic { Selection, Light, Success, Warning }

@Composable
fun rememberAppHaptics(): (AppHaptic) -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) {
        { type ->
            // LongPress is the most consistently supported Android Compose feedback
            // across API 26+; keep it reserved for meaningful state changes.
            when (type) {
                AppHaptic.Selection, AppHaptic.Success, AppHaptic.Warning -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                AppHaptic.Light -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }
}
