package pl.droidsonroids.kmpiotdemo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import pl.droidsonroids.kmpiotdemo.status.BluetoothStatus
import pl.droidsonroids.kmpiotdemo.status.LocationStatus
import pl.droidsonroids.kmpiotdemo.status.PermissionStatus
import pl.droidsonroids.kmpiotdemo.ui.App

class MainActivity : ComponentActivity() {

    private var permissionStatus by mutableStateOf(PermissionStatus.Granted)
    private var bluetoothStatus by mutableStateOf(BluetoothStatus.Enabled)
    private var locationStatus by mutableStateOf(LocationStatus.Enabled)

    private fun getRequiredPermissions(): Array<String> = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val status = computePermissionStatus(permissions)
        permissionStatus = status
        if (status == PermissionStatus.DeniedPermanently) {
            openAppSettings()
        } else {
            bluetoothStatus = computeBluetoothStatus()
            locationStatus = computeLocationStatus()
        }
    }

    private val enableBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        bluetoothStatus = computeBluetoothStatus()
        if (bluetoothStatus != BluetoothStatus.Enabled && result.resultCode == RESULT_CANCELED) {
            openBluetoothSettings()
        }
    }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> bluetoothStatus = computeBluetoothStatus()
                LocationManager.PROVIDERS_CHANGED_ACTION -> locationStatus = computeLocationStatus()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionStatus = computePermissionStatus()
        bluetoothStatus = computeBluetoothStatus()
        locationStatus = computeLocationStatus()

        setContent {
            App(
                permissionStatus = permissionStatus,
                onRequestPermissions = { requestPermissions.launch(getRequiredPermissions()) },
                onOpenSettings = { openAppSettings() },
                bluetoothStatus = bluetoothStatus,
                onEnableBluetooth = { startEnableBluetoothFlow() },
                onOpenBluetoothSettings = { openBluetoothSettings() },
                locationStatus = locationStatus,
                onEnableLocation = { openLocationSettings() },
                onOpenLocationSettings = { openLocationSettings() },
            )
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        registerReceiver(stateReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(stateReceiver)
    }

    override fun onResume() {
        super.onResume()
        permissionStatus = computePermissionStatus()
        bluetoothStatus = computeBluetoothStatus()
        locationStatus = computeLocationStatus()
    }

    private fun startEnableBluetoothFlow() {
        enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun computePermissionStatus(latestResult: Map<String, Boolean>? = null): PermissionStatus {
        val required = getRequiredPermissions()
        fun isGranted(perm: String): Boolean {
            val latest = latestResult?.get(perm)
            return latest ?: (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED)
        }
        val missing = required.filterNot { isGranted(it) }
        if (missing.isEmpty()) return PermissionStatus.Granted

        val anyPermanentlyDenied = latestResult != null && missing.any { perm ->
            !shouldShowRequestPermissionRationale(perm)
        }
        return if (anyPermanentlyDenied) PermissionStatus.DeniedPermanently else PermissionStatus.DeniedCanAsk
    }

    private fun computeBluetoothStatus(): BluetoothStatus {
        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val adapter: BluetoothAdapter = manager.adapter
        return if (adapter.isEnabled) BluetoothStatus.Enabled else BluetoothStatus.DisabledCanAsk
    }

    private fun computeLocationStatus(): LocationStatus {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val enabled = try { lm.isLocationEnabled } catch (_: Exception) { false }
        return if (enabled) LocationStatus.Enabled else LocationStatus.DisabledCanAsk
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun openBluetoothSettings() {
        try {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    private fun openLocationSettings() {
        try {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
