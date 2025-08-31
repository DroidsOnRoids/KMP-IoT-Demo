package pl.droidsonroids.kmpiotdemo.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.droidsonroids.kmpiotdemo.status.PermissionStatus

@Composable
fun PermissionPrompt(
    permissionStatus: PermissionStatus,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Bluetooth permissions required",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        when (permissionStatus) {
            PermissionStatus.DeniedCanAsk ->
                Button(onClick = onRequestPermissions) { Text("Grant") }
            PermissionStatus.DeniedPermanently ->
                Button(onClick = onOpenSettings) { Text("Open Settings") }
            else -> Unit
        }
    }
}
