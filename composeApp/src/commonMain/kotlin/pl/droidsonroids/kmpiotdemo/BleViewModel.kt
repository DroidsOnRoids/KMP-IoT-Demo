package pl.droidsonroids.kmpiotdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.droidsonroids.kmpiotdemo.data.BleRepository
import pl.droidsonroids.kmpiotdemo.status.ConnectionStatus

class BleViewModel : ViewModel() {
    private val bleRepository = BleRepository()

    val connectionStatus: StateFlow<ConnectionStatus> = bleRepository.connectionStatus
    val isReadyToSend: StateFlow<Boolean> = bleRepository.isReadyToSend

    fun scanAndConnect() {
        viewModelScope.launch {
            bleRepository.scanAndConnect()
        }
    }

    fun disconnect() {
        bleRepository.disconnect()
    }

    fun sendBlinkCount(count: Int) {
        bleRepository.sendBlinkCount(count)
    }

    override fun onCleared() {
        bleRepository.disconnect()
        super.onCleared()
    }
}
