package com.pupu.diary.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pupu.diary.MainActivity
import com.pupu.diary.R
import com.pupu.diary.data.Prefs
import java.util.Calendar

object Reminders {

    const val CHANNEL_ID = "reminders"
    const val CODE_RECORD = 1001
    const val CODE_WATER = 1002

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID, "贴心提醒", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "记录与喝水的定时提醒"
        }
        nm.createNotificationChannel(channel)
    }

    /** 每天 timeMinOfDay 提醒；返回是否调度成功 */
    fun schedule(context: Context, code: Int, timeMinOfDay: Int, title: String, body: String): Boolean {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pending(context, code, title, body)

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, timeMinOfDay / 60)
            set(Calendar.MINUTE, timeMinOfDay % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        return try {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun cancel(context: Context, code: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(context, code, "", ""))
    }

    private fun pending(context: Context, code: Int, title: String, body: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("code", code)
            putExtra("title", title)
            putExtra("body", body)
        }
        return PendingIntent.getBroadcast(
            context, code, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** 点击通知跳转的目标页：记录提醒 → 记录页，喝水提醒 → 首页 */
    fun tapIntent(context: Context, code: Int): PendingIntent {
        val nav = if (code == CODE_WATER) "home" else "record"
        val intent = Intent(context, MainActivity::class.java).putExtra("nav", nav)
        return PendingIntent.getActivity(
            context, code, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val code = intent.getIntExtra("code", Reminders.CODE_RECORD)
        val title = intent.getStringExtra("title") ?: "噗噗日记"
        val body = intent.getStringExtra("body") ?: "该记录一下啦～"

        Reminders.ensureChannel(context)

        val notification = NotificationCompat.Builder(context, Reminders.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_pupu)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(Reminders.tapIntent(context, code))
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            try {
                NotificationManagerCompat.from(context).notify(code, notification)
            } catch (_: SecurityException) {
            }
        }

        // 自续期：一次性闹钟触发后立刻调度明天的同一提醒，
        // 否则只要用户不打开 App / 不重启手机，提醒第二天就会静默失效
        val prefs = Prefs(context)
        when (code) {
            Reminders.CODE_RECORD -> if (prefs.recordRemindOn) {
                Reminders.schedule(context, code, prefs.recordRemindMin, title, body)
            }
            Reminders.CODE_WATER -> if (prefs.waterRemindOn) {
                Reminders.schedule(context, code, prefs.waterRemindMin, title, body)
            }
        }
    }
}
