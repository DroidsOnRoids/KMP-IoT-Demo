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
import pl.droidsonroids.kmpiotdemo.status.LocationStatus

@Composable
fun LocationPrompt(
    locationStatus: LocationStatus,
    onEnableLocation: () -> Unit,
    onOpenLocationSettings: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Location services must be turned on",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        when (locationStatus) {
            LocationStatus.DisabledCanAsk ->
                Button(onClick = onEnableLocation) { Text("Turn On") }
            LocationStatus.DisabledPermanently ->
                Button(onClick = onOpenLocationSettings) { Text("Open Settings") }
            else -> Unit
        }
    }
}
