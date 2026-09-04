package com.example.ui.statedemo

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val TAG = "STATE_DEMO"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "state_demo")

data class DataStoreState(
    val name: String = "",
    val counter: Int = 0,
    val isSelected: Boolean = false
)

class AppDataStore(private val context: Context) {

    private val KEY_NAME = stringPreferencesKey("ds_name")
    private val KEY_COUNTER = intPreferencesKey("ds_counter")
    private val KEY_SELECTED = booleanPreferencesKey("ds_selected")

    private var isFirstRead = true

    val stateFlow: Flow<DataStoreState> = context.dataStore.data
        .map { preferences ->
            DataStoreState(
                name = preferences[KEY_NAME] ?: "",
                counter = preferences[KEY_COUNTER] ?: 0,
                isSelected = preferences[KEY_SELECTED] ?: false
            )
        }
        .onEach { state ->
            if (isFirstRead) {
                Log.i(TAG, "DataStore ĐỌC lần đầu: name='${state.name}', counter=${state.counter}, selected=${state.isSelected}")
                isFirstRead = false
            }
        }

    suspend fun saveName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_NAME] = name
        }
        Log.i(TAG, "DataStore GHI: name='$name'")
    }

    suspend fun saveCounter(counter: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_COUNTER] = counter
        }
        Log.i(TAG, "DataStore GHI: counter=$counter")
    }

    suspend fun saveSelected(isSelected: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED] = isSelected
        }
        Log.i(TAG, "DataStore GHI: selected=$isSelected")
    }
}
