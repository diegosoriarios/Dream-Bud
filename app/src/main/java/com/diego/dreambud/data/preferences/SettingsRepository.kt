package com.diego.dreambud.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dreambud_settings", Context.MODE_PRIVATE)

    private val _modelPath = MutableStateFlow(prefs.getString(KEY_MODEL_PATH, null))
    val modelPath: StateFlow<String?> = _modelPath.asStateFlow()

    private val _useLocalLlm = MutableStateFlow(prefs.getBoolean(KEY_USE_LOCAL_LLM, true))
    val useLocalLlm: StateFlow<Boolean> = _useLocalLlm.asStateFlow()

    private val _isBiometricLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_LOCK, false))
    val isBiometricLockEnabled: StateFlow<Boolean> = _isBiometricLockEnabled.asStateFlow()

    private val _isReminderEnabled = MutableStateFlow(prefs.getBoolean(KEY_REMINDER_ENABLED, false))
    val isReminderEnabled: StateFlow<Boolean> = _isReminderEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(prefs.getInt(KEY_REMINDER_HOUR, 7))
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(prefs.getInt(KEY_REMINDER_MINUTE, 0))
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    private val _isAlarmEnabled = MutableStateFlow(prefs.getBoolean(KEY_ALARM_ENABLED, false))
    val isAlarmEnabled: StateFlow<Boolean> = _isAlarmEnabled.asStateFlow()

    private val _alarmHour = MutableStateFlow(prefs.getInt(KEY_ALARM_HOUR, 7))
    val alarmHour: StateFlow<Int> = _alarmHour.asStateFlow()

    private val _alarmMinute = MutableStateFlow(prefs.getInt(KEY_ALARM_MINUTE, 30))
    val alarmMinute: StateFlow<Int> = _alarmMinute.asStateFlow()

    private val _realityCheckIntervalMinutes =
        MutableStateFlow(prefs.getInt(KEY_REALITY_CHECK_INTERVAL, 0))
    val realityCheckIntervalMinutes: StateFlow<Int> = _realityCheckIntervalMinutes.asStateFlow()

    fun setModelPath(path: String?) {
        prefs.edit().putString(KEY_MODEL_PATH, path).apply()
        _modelPath.value = path
    }

    fun setUseLocalLlm(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_USE_LOCAL_LLM, enabled).apply()
        _useLocalLlm.value = enabled
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, enabled).apply()
        _isBiometricLockEnabled.value = enabled
    }

    fun setReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        prefs.edit()
            .putBoolean(KEY_REMINDER_ENABLED, enabled)
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()

        _isReminderEnabled.value = enabled
        _reminderHour.value = hour
        _reminderMinute.value = minute
    }

    fun setAlarmSettings(enabled: Boolean, hour: Int, minute: Int) {
        prefs.edit()
            .putBoolean(KEY_ALARM_ENABLED, enabled)
            .putInt(KEY_ALARM_HOUR, hour)
            .putInt(KEY_ALARM_MINUTE, minute)
            .apply()

        _isAlarmEnabled.value = enabled
        _alarmHour.value = hour
        _alarmMinute.value = minute
    }

    fun setRealityCheckInterval(minutes: Int) {
        prefs.edit().putInt(KEY_REALITY_CHECK_INTERVAL, minutes).apply()
        _realityCheckIntervalMinutes.value = minutes
    }

    companion object {
        private const val KEY_MODEL_PATH = "key_model_path"
        private const val KEY_USE_LOCAL_LLM = "key_use_local_llm"
        private const val KEY_BIOMETRIC_LOCK = "key_biometric_lock"
        private const val KEY_REMINDER_ENABLED = "key_reminder_enabled"
        private const val KEY_REMINDER_HOUR = "key_reminder_hour"
        private const val KEY_REMINDER_MINUTE = "key_reminder_minute"
        private const val KEY_ALARM_ENABLED = "key_dream_alarm_enabled"
        private const val KEY_ALARM_HOUR = "key_dream_alarm_hour"
        private const val KEY_ALARM_MINUTE = "key_dream_alarm_minute"
        private const val KEY_REALITY_CHECK_INTERVAL = "key_reality_check_interval"
    }
}
