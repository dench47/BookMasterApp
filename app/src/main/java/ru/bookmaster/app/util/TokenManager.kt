package ru.bookmaster.app.util

import android.content.Context
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
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val email: Flow<String?> = context.dataStore.data.map { it[EMAIL_KEY] }
    val companyName: Flow<String?> = context.dataStore.data.map { it[COMPANY_NAME_KEY] }
    val companyId: Flow<Long?> = context.dataStore.data.map { it[COMPANY_ID_KEY] }

    suspend fun saveAuthData(token: String, email: String, companyName: String, companyId: Long) {
        context.dataStore.edit {
            it[TOKEN_KEY] = token
            it[EMAIL_KEY] = email
            it[COMPANY_NAME_KEY] = companyName
            it[COMPANY_ID_KEY] = companyId
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}