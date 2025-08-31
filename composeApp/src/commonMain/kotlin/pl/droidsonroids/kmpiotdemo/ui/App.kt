package pl.droidsonroids.kmpiotdemo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import pl.droidsonroids.kmpiotdemo.BleViewModel
import pl.droidsonroids.kmpiotdemo.BleViewModelFactory
import pl.droidsonroids.kmpiotdemo.status.BluetoothStatus
import pl.droidsonroids.kmpiotdemo.status.ConnectionStatus
import pl.droidsonroids.kmpiotdemo.status.LocationStatus
import pl.droidsonroids.kmpiotdemo.status.PermissionStatus

@Composable
@Preview
fun App(
    permissionStatus: PermissionStatus = PermissionStatus.Granted,
    onRequestPermissions: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    bluetoothStatus: BluetoothStatus = BluetoothStatus.Enabled,
    onEnableBluetooth: () -> Unit = {},
    onOpenBluetoothSettings: () -> Unit = onOpenSettings,
    locationStatus: LocationStatus = LocationStatus.Enabled,
    onEnableLocation: () -> Unit = {},
    onOpenLocationSettings: () -> Unit = onOpenSettings,
) {
    val bleViewModel: BleViewModel = viewModel(factory = BleViewModelFactory())

    val connectionState by bleViewModel.connectionStatus.collectAsState()
    val isReadyToSend by bleViewModel.isReadyToSend.collectAsState()

    val hasPermissions = permissionStatus == PermissionStatus.Granted
    val isBluetoothEnabled = bluetoothStatus == BluetoothStatus.Enabled
    val isLocationEnabled = locationStatus == LocationStatus.Enabled
    val canConnect = hasPermissions && isBluetoothEnabled && isLocationEnabled

    val isConnected = connectionState == ConnectionStatus.Connected
    val isInProgress = connectionState == ConnectionStatus.InProgress

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Bluno Blink Controller",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (!hasPermissions) {
                PermissionPrompt(
                    permissionStatus = permissionStatus,
                    onRequestPermissions = onRequestPermissions,
                    onOpenSettings = onOpenSettings
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!isBluetoothEnabled) {
                BluetoothPrompt(
                    bluetoothStatus = bluetoothStatus,
                    onEnableBluetooth = onEnableBluetooth,
                    onOpenBluetoothSettings = onOpenBluetoothSettings
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!isLocationEnabled) {
                LocationPrompt(
                    locationStatus = locationStatus,
                    onEnableLocation = onEnableLocation,
                    onOpenLocationSettings = onOpenLocationSettings
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            ConnectButton(
                connectionStatus = connectionState,
                enabled = canConnect && !isInProgress,
                onClick = {
                    when (connectionState) {
                        ConnectionStatus.Idle -> bleViewModel.scanAndConnect()
                        ConnectionStatus.InProgress -> Unit
                        ConnectionStatus.Connected -> bleViewModel.disconnect()
                    }
                },
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (isInProgress) {
                ProgressRow(text = "Scanning & connecting...")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isConnected) {
                BlinkControls(
                    isReadyToSend = isReadyToSend,
                    onSend = { count -> bleViewModel.sendBlinkCount(count) }
                )
            }
        }
    }
}
