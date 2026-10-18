package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "spark_preferences")

class SparkPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SELECTED_MODEL_ID = stringPreferencesKey("selected_model_id")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val THINKING_HIGH = booleanPreferencesKey("thinking_high")
        val SEARCH_GROUNDING = booleanPreferencesKey("search_grounding")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.DARK_THEME] ?: true }

    val selectedModelId: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.SELECTED_MODEL_ID] ?: "gemini-3.8-flash" }

    val temperature: Flow<Float> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.TEMPERATURE] ?: 0.7f }

    val thinkingHigh: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.THINKING_HIGH] ?: true }

    val searchGrounding: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.SEARCH_GROUNDING] ?: false }

    val fontScale: Flow<Float> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.FONT_SCALE] ?: 1.0f }

    val customApiKey: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.CUSTOM_API_KEY] ?: "" }

    val biometricLock: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.BIOMETRIC_LOCK] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DARK_THEME] = enabled }
    }

    suspend fun setSelectedModelId(modelId: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_MODEL_ID] = modelId }
    }

    suspend fun setTemperature(temp: Float) {
        context.dataStore.edit { it[PreferencesKeys.TEMPERATURE] = temp }
    }

    suspend fun setThinkingHigh(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.THINKING_HIGH] = enabled }
    }

    suspend fun setSearchGrounding(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SEARCH_GROUNDING] = enabled }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SCALE] = scale }
    }

    suspend fun setCustomApiKey(key: String) {
        context.dataStore.edit { it[PreferencesKeys.CUSTOM_API_KEY] = key }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BIOMETRIC_LOCK] = enabled }
    }
}
