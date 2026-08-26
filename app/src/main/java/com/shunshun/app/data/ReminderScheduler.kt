package com.shunshun.app.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.shunshun.app.MainActivity
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ReminderScheduler {
    const val WATER = "water"
    const val RECORD = "record"
    private const val REQUEST_WATER = 7401
    private const val REQUEST_RECORD = 7402

    fun sync(context: Context, waterEnabled: Boolean, waterHour: Int, waterMinute: Int, recordEnabled: Boolean, recordHour: Int, recordMinute: Int) {
        schedule(context, WATER, waterEnabled, waterHour, waterMinute)
        schedule(context, RECORD, recordEnabled, recordHour, recordMinute)
    }

    fun schedule(context: Context, kind: String, enabled: Boolean, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = pendingIntent(context, kind)
        alarmManager.cancel(pendingIntent)
        if (!enabled) return
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    private fun pendingIntent(context: Context, kind: String): PendingIntent {
        val requestCode = if (kind == WATER) REQUEST_WATER else REQUEST_RECORD
        val intent = Intent(context, ReminderReceiver::class.java).putExtra("kind", kind)
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        ensureChannel(context)
        val water = intent.getStringExtra("kind") == ReminderScheduler.WATER
        val openApp = PendingIntent.getActivity(context, if (water) 8401 else 8402, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(if (water) "喝口水吧～" else "顺顺提醒你")
            .setContentText(if (water) "补充一点水分，肚肚会更舒服。" else "花几秒钟记下今天的身体感受。")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(if (water) 7401 else 7402, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(NotificationChannel(CHANNEL_ID, "顺顺提醒", NotificationManager.IMPORTANCE_DEFAULT).apply { description = "喝水和生活记录提醒" })
        }
    }

    companion object { private const val CHANNEL_ID = "shunshun_reminders" }
}

class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val prefs = context.settingsDataStore.data.first()
                ReminderScheduler.sync(
                    context,
                    prefs[WATER_REMINDER] ?: false,
                    prefs[WATER_REMINDER_HOUR] ?: 15,
                    prefs[WATER_REMINDER_MINUTE] ?: 0,
                    prefs[RECORD_REMINDER] ?: false,
                    prefs[RECORD_REMINDER_HOUR] ?: 21,
                    prefs[RECORD_REMINDER_MINUTE] ?: 0
                )
            }
            pending.finish()
        }
    }
}
