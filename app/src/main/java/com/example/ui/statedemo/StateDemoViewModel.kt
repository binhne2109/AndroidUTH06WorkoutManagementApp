package com.example.ui.statedemo

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "STATE_DEMO"

class StateDemoViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _vmName = MutableStateFlow("")
    val vmName: StateFlow<String> = _vmName.asStateFlow()

    private val _vmCounter = MutableStateFlow(0)
    val vmCounter: StateFlow<Int> = _vmCounter.asStateFlow()

    private val _vmSelected = MutableStateFlow(false)
    val vmSelected: StateFlow<Boolean> = _vmSelected.asStateFlow()

    fun setVmName(name: String) {
        _vmName.value = name
    }

    fun incrementVmCounter() {
        _vmCounter.value += 1
    }

    fun setVmSelected(selected: Boolean) {
        _vmSelected.value = selected
    }

    val sshName: StateFlow<String> = savedStateHandle.getStateFlow("ssh_name", "")
    val sshCounter: StateFlow<Int> = savedStateHandle.getStateFlow("ssh_counter", 0)
    val sshSelected: StateFlow<Boolean> = savedStateHandle.getStateFlow("ssh_selected", false)

    fun setSshName(name: String) {
        savedStateHandle["ssh_name"] = name
    }

    fun incrementSshCounter() {
        val current = savedStateHandle.get<Int>("ssh_counter") ?: 0
        savedStateHandle["ssh_counter"] = current + 1
    }

    fun setSshSelected(selected: Boolean) {
        savedStateHandle["ssh_selected"] = selected
    }

    private val appDataStore = AppDataStore(application)

    val dsState: StateFlow<DataStoreState> = appDataStore.stateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = DataStoreState()
        )

    fun saveDsName(name: String) {
        viewModelScope.launch {
            appDataStore.saveName(name)
        }
    }

    fun incrementDsCounter() {
        viewModelScope.launch {
            val current = dsState.value.counter
            appDataStore.saveCounter(current + 1)
        }
    }

    fun saveDsSelected(selected: Boolean) {
        viewModelScope.launch {
            appDataStore.saveSelected(selected)
        }
    }

    init {
        val initName = savedStateHandle.get<String>("ssh_name") ?: ""
        val initCounter = savedStateHandle.get<Int>("ssh_counter") ?: 0
        val initSelected = savedStateHandle.get<Boolean>("ssh_selected") ?: false
        Log.i(
            TAG,
            "ViewModel INIT — SavedStateHandle có sẵn: name='$initName', counter=$initCounter, selected=$initSelected (khác default = đã khôi phục từ Bundle)"
        )
    }

    override fun onCleared() {
        super.onCleared()
        Log.w(TAG, "ViewModel onCleared() — ViewModel ĐÃ BỊ HUỶ")
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val savedStateHandle = createSavedStateHandle()
                StateDemoViewModel(application, savedStateHandle)
            }
        }
    }
}
