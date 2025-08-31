package pl.droidsonroids.kmpiotdemo.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.droidsonroids.kmpiotdemo.status.ConnectionStatus

@Composable
fun ConnectButton(
    connectionStatus: ConnectionStatus,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(
            when (connectionStatus) {
                ConnectionStatus.Idle, ConnectionStatus.InProgress -> "Scan & Connect"
                ConnectionStatus.Connected -> "Disconnect"
            }
        )
    }
}
