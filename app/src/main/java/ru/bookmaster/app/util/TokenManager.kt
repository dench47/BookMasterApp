package ru.bookmaster.app.util

import android.content.Context
import androidx.core.content.edit
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val COMPANY_NAME_KEY = stringPreferencesKey("company_name")
        private val COMPANY_ID_KEY = longPreferencesKey("company_id")
        private val COMPANY_TYPE_KEY = stringPreferencesKey("company_type")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val email: Flow<String?> = context.dataStore.data.map { it[EMAIL_KEY] }
    val companyName: Flow<String?> = context.dataStore.data.map { it[COMPANY_NAME_KEY] }
    val companyId: Flow<Long?> = context.dataStore.data.map { it[COMPANY_ID_KEY] }
    val companyType: Flow<String?> = context.dataStore.data.map { it[COMPANY_TYPE_KEY] }

    suspend fun saveAuthData(
        token: String,
        email: String,
        companyName: String,
        companyId: Long,
        companyType: String = "salon",
        isPremium: Boolean = false,
        maxServices: Int? = null,
        maxBookingDays: Int? = null,
        remindersEnabled: Boolean = false
    ) {
        context.dataStore.edit {
            it[TOKEN_KEY] = token
            it[EMAIL_KEY] = email
            it[COMPANY_NAME_KEY] = companyName
            it[COMPANY_ID_KEY] = companyId
            it[COMPANY_TYPE_KEY] = companyType
        }
        // Дублируем JWT в SharedPreferences для быстрого доступа из MessagingService
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit { putString("jwt_token", token) }

        val prefs = context.getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean("is_premium", isPremium)
            putInt("max_services", maxServices ?: 3)
            putInt("max_booking_days", maxBookingDays ?: 7)
            putBoolean("reminders_enabled", remindersEnabled)
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit { remove("jwt_token") }
    }
}