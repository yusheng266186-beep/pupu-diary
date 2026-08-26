package com.pupu.diary.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pupu.diary.data.Prefs

/** 开机完成后重挂提醒闹钟 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = Prefs(context)
        if (prefs.recordRemindOn) {
            Reminders.schedule(
                context, Reminders.CODE_RECORD, prefs.recordRemindMin,
                "该噗噗啦～", "深呼吸，找个舒服的姿势，去迎接今天的轻松时刻吧"
            )
        }
        if (prefs.waterRemindOn) {
            Reminders.schedule(
                context, Reminders.CODE_WATER, prefs.waterRemindMin,
                "喝口水吧 💧", "小口慢饮，给身体补补水"
            )
        }
    }
}
