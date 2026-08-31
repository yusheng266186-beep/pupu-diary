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

    // v4.0.0：AppState 改为进程级单例（PupuApp 持有），Activity 重建不再重复加载
    private val state: AppState by lazy { PupuApp.of(this).appState }
    private val navTarget = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        CrashLog.install(applicationContext)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Reminders.ensureChannel(applicationContext)
        state.reArmReminders(applicationContext)
        navTarget.value = intent?.getStringExtra("nav")
        setContent { AppRoot(state, navTarget) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // App 在前台时点通知：singleTop 复用实例，从这里接收导航目标
        navTarget.value = intent.getStringExtra("nav")
    }

    override fun onResume() {
        super.onResume()
        // FLAG_SECURE 的兜底（主路径在 AppRoot 里即时挂载，见 v4.0.0 修复）
        if (state.privacyMode) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
