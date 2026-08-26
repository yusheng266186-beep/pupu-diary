package com.shunshun.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "shunshun_settings")

val WATER_REMINDER = booleanPreferencesKey("water_reminder")
val RECORD_REMINDER = booleanPreferencesKey("record_reminder")
val WATER_REMINDER_HOUR = intPreferencesKey("water_reminder_hour")
val WATER_REMINDER_MINUTE = intPreferencesKey("water_reminder_minute")
val RECORD_REMINDER_HOUR = intPreferencesKey("record_reminder_hour")
val RECORD_REMINDER_MINUTE = intPreferencesKey("record_reminder_minute")
