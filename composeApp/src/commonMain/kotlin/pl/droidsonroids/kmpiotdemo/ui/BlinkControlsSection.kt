package pl.droidsonroids.kmpiotdemo.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun BlinkControls(
    isReadyToSend: Boolean,
    onSend: (Int) -> Unit,
) {
    var sliderValue by remember { mutableFloatStateOf(3f) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Number of blinks: ${sliderValue.roundToInt()}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 12.dp)
        )
    }
    Slider(
        value = sliderValue,
        onValueChange = { sliderValue = it },
        valueRange = 1f..15f,
        steps = 13,
        enabled = isReadyToSend,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = { onSend(sliderValue.roundToInt()) },
        enabled = isReadyToSend
    ) {
        Text("Send")
    }

    if (!isReadyToSend) {
        ProgressRow(text = "Waiting for Blinking complete…")
    }
}
