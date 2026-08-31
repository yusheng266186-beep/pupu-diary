package com.pupu.diary

import android.app.Application
import android.content.Context
import com.pupu.diary.ui.AppState

/**
 * v4.0.0：AppState 提升为进程级单例（挂在 Application 上），
 * Activity 重建（字体缩放/分屏/开发者选项等）不再重复建 AppState 或泄漏旧协程作用域。
 */
class PupuApp : Application() {

    val appState: AppState by lazy { AppState(this) }

    companion object {
        fun of(context: Context): PupuApp = context.applicationContext as PupuApp
    }
}
