package app.practice.geofencing.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import app.practice.geofencing.util.Constants.PREFERENCE_FIRST_LAUNCH
import app.practice.geofencing.util.Constants.PREFERENCE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(PREFERENCE_NAME)

interface DataStoreRepository {
    suspend fun saveFirstLaunch(firstLaunch: Boolean)
    fun readFirstLaunch(): Flow<Boolean>
}

class DataStoreRepositoryImpl @Inject constructor(
    private val context: Context
) : DataStoreRepository {

    private object PreferenceKey {
        val firstLaunch = booleanPreferencesKey(PREFERENCE_FIRST_LAUNCH)
    }

    private val dataStore = context.dataStore

    override suspend fun saveFirstLaunch(firstLaunch: Boolean) {
        dataStore.edit { preference ->
            preference[PreferenceKey.firstLaunch] = firstLaunch
        }
    }

    override fun readFirstLaunch(): Flow<Boolean> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preference ->
            val firstLaunch = preference[PreferenceKey.firstLaunch] ?: true
            firstLaunch
        }
    }

}