package pl.droidsonroids.kmpiotdemo.data

import com.juul.kable.characteristicOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal val blunoSerialPortCharacteristic = characteristicOf(
    service = Uuid.parse("0000dfb0-0000-1000-8000-00805f9b34fb"),
    characteristic = Uuid.parse("0000dfb1-0000-1000-8000-00805f9b34fb")
)

internal const val BLINK_DONE_TOKEN = "C"
