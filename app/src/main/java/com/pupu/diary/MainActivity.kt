package com.pupu.diary

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.pupu.diary.ui.AppRoot
import com.pupu.diary.ui.AppState
import com.pupu.diary.util.CrashLog
import com.pupu.diary.util.Reminders

class MainActivity : ComponentActivity() {

    private lateinit var state: AppState
    private val navTarget = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        CrashLog.install(applicationContext)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        state = AppState(applicationContext)
        Reminders.ensureChannel(applicationContext)
        state.reArmReminders(applicationContext)
        navTarget.value = intent?.getStringExtra("nav")
        setContent { AppRoot(state, navTarget) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // App 在前台时点通知：singleTop 复用实例，从这里接收导航目标
        navTarget.value = intent.getStringExtra("nav")
    }

    override fun onResume() {
        super.onResume()
        if (this::state.isInitialized && state.privacyMode) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
