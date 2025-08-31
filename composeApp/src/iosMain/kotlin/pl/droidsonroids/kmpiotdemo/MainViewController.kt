package pl.droidsonroids.kmpiotdemo

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import pl.droidsonroids.kmpiotdemo.status.BluetoothStatus
import pl.droidsonroids.kmpiotdemo.status.PermissionStatus
import platform.CoreBluetooth.CBCentralManager
import platform.CoreBluetooth.CBCentralManagerDelegateProtocol
import platform.CoreBluetooth.CBManager
import platform.CoreBluetooth.CBManagerAuthorizationAllowedAlways
import platform.CoreBluetooth.CBManagerAuthorizationDenied
import platform.CoreBluetooth.CBManagerAuthorizationNotDetermined
import platform.CoreBluetooth.CBManagerAuthorizationRestricted
import platform.CoreBluetooth.CBManagerStatePoweredOn
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import pl.droidsonroids.kmpiotdemo.ui.App

fun MainViewController() = ComposeUIViewController {
    val permission = remember { mutableStateOf(currentIosBlePermissionStatus()) }
    val bt = remember { mutableStateOf(currentIosBluetoothStatus()) }

    LaunchedEffect(Unit) {
        IosBlePermission.onUpdate = {
            permission.value = currentIosBlePermissionStatus()
            bt.value = currentIosBluetoothStatus()
        }
        IosBlePermission.ensure()
    }

    App(
        permissionStatus = permission.value,
        onRequestPermissions = {
            IosBlePermission.request {
                permission.value = currentIosBlePermissionStatus(); bt.value =
                currentIosBluetoothStatus()
            }
        },
        onOpenSettings = { openAppSettings() },
        bluetoothStatus = bt.value,
        onOpenBluetoothSettings = { showBluetoothAlert() },
    )
}

@OptIn(ExperimentalForeignApi::class)
private fun showBluetoothAlert() {
    val alert = UIAlertController.alertControllerWithTitle(
        title = "Enable Bluetooth",
        message = "To use this feature, please go to Settings and enable Bluetooth.",
        preferredStyle = UIAlertControllerStyleAlert
    )
    alert.addAction(
        UIAlertAction.actionWithTitle("Cancel", style = UIAlertActionStyleCancel, handler = null)
    )
    alert.addAction(
        UIAlertAction.actionWithTitle(
            "Open App Settings",
            style = UIAlertActionStyleDefault,
            handler = {
                openAppSettings()
            })
    )

    val window = UIApplication.sharedApplication.windows.first() as UIWindow
    window.rootViewController?.presentViewController(alert, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun currentIosBlePermissionStatus(): PermissionStatus = when (CBManager.authorization) {
    CBManagerAuthorizationAllowedAlways -> PermissionStatus.Granted
    CBManagerAuthorizationNotDetermined -> PermissionStatus.DeniedCanAsk
    CBManagerAuthorizationDenied, CBManagerAuthorizationRestricted -> PermissionStatus.DeniedPermanently
    else -> PermissionStatus.DeniedPermanently
}

@OptIn(ExperimentalForeignApi::class)
private fun currentIosBluetoothStatus(): BluetoothStatus {
    val central = IosBlePermission.central
    val poweredOn = central?.state == CBManagerStatePoweredOn
    return if (poweredOn) BluetoothStatus.Enabled else BluetoothStatus.DisabledPermanently
}

@OptIn(ExperimentalForeignApi::class)
private object IosBlePermission {
    var central: CBCentralManager? = null
        private set
    var onUpdate: (() -> Unit)? = null

    private val delegate = object : NSObject(), CBCentralManagerDelegateProtocol {
        override fun centralManagerDidUpdateState(central: CBCentralManager) {
            onUpdate?.invoke()
        }
    }

    fun ensure() {
        if (central == null) {
            central = CBCentralManager(delegate = delegate, queue = null, options = null)
        }
    }

    fun request(onAfter: () -> Unit) {
        onUpdate = onAfter
        ensure()
        onAfter()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun openAppSettings() {
    val url = NSURL(string = UIApplicationOpenSettingsURLString)
    UIApplication.sharedApplication.openURL(
        url = url,
        options = emptyMap<Any?, Any?>(),
        completionHandler = { _ -> }
    )
}
