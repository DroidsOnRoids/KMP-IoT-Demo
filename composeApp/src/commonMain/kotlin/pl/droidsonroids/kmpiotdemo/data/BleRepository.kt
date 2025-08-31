package pl.droidsonroids.kmpiotdemo.data

import com.juul.kable.Advertisement
import com.juul.kable.Peripheral
import com.juul.kable.Scanner
import com.juul.kable.UnmetRequirementException
import com.juul.kable.WriteType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import pl.droidsonroids.kmpiotdemo.status.ConnectionStatus
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class BleRepository {
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Idle)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _isReadyToSend = MutableStateFlow(false)
    val isReadyToSend: StateFlow<Boolean> = _isReadyToSend.asStateFlow()

    private lateinit var peripheral: Peripheral

    suspend fun scanAndConnect() {
        if (_connectionStatus.value != ConnectionStatus.Idle) return
        _isReadyToSend.value = false
        _connectionStatus.value = ConnectionStatus.InProgress

        val scanner = Scanner {
            filters {
                match {
                    services = listOf(blunoSerialPortCharacteristic.serviceUuid)
                }
            }
        }
        try {
            connectToAdvertisement(scanner.advertisements.first())
        } catch (_: UnmetRequirementException) {
            _connectionStatus.value = ConnectionStatus.Idle
            _isReadyToSend.value = false
        }
    }

    fun disconnect() {
        peripheral.close()
        _connectionStatus.value = ConnectionStatus.Idle
        _isReadyToSend.value = false
    }

    private suspend fun connectToAdvertisement(advertisement: Advertisement) {
        peripheral = Peripheral(advertisement)
        peripheral.connect().launch {
            _connectionStatus.value = ConnectionStatus.Connected
            _isReadyToSend.value = true
        }
    }

    fun sendBlinkCount(count: Int) {
        _isReadyToSend.value = false
        peripheral.scope.launch {
            awaitBlinkConfirmation()
            _isReadyToSend.value = true
        }
        peripheral.scope.launch {
            writeData(count)
        }
    }

    private suspend fun writeData(value: Int) {
        peripheral.write(
            characteristic = blunoSerialPortCharacteristic,
            data = value.toString().encodeToByteArray(),
            writeType = WriteType.WithoutResponse
        )
    }

    private suspend fun awaitBlinkConfirmation() {
        try {
            peripheral.observe(blunoSerialPortCharacteristic).awaitConfirmation()
        } catch (_: IOException) {
            createPollingFlow().awaitConfirmation()
        }
    }

    private fun createPollingFlow() = channelFlow {
        while (_connectionStatus.value == ConnectionStatus.Connected) {
            trySend(peripheral.read(blunoSerialPortCharacteristic))
        }
    }
}

private suspend fun Flow<ByteArray>.awaitConfirmation() = map(ByteArray::decodeToString)
    .first { it.contains(BLINK_DONE_TOKEN) }
