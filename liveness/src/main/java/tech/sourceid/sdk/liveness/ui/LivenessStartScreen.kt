package tech.sourceid.sdk.liveness.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import tech.sourceid.liveness.R
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.ui.theme.cardColor
import tech.sourceid.sdk.liveness.ui.theme.textColor

/** Start-page guidelines; a host string resource with the same name replaces or translates each one. */
private val GUIDELINES = listOf(
    R.string.sid_liveness_start_guideline_light,
    R.string.sid_liveness_start_guideline_center,
    R.string.sid_liveness_start_guideline_coverings,
    R.string.sid_liveness_start_guideline_photosensitivity,
    R.string.sid_liveness_start_guideline_retries,
    R.string.sid_liveness_start_guideline_ready
)

/**
 * Instruction page shown before the capture flow starts, mirroring the
 * SourceID web liveness start page. Hidden with `showStartPage = false`.
 */
@Composable
internal fun LivenessStartScreen(
    config: LivenessUIConfig,
    onStart: () -> Unit
) {
    val textColor = config.textColor
    val cardBackground = config.cardColor
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = config.customTitle ?: stringResource(R.string.sid_liveness_start_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.sid_liveness_start_intro),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        FaceScanIcon(tint = primary)

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardBackground)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.sid_liveness_start_guidelines_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            GUIDELINES.forEach { line ->
                // A separate bullet keeps it at the leading edge in right-to-left languages.
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "•", style = MaterialTheme.typography.bodySmall, color = textColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(line),
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primary)
        ) {
            Text(
                text = stringResource(R.string.sid_liveness_start_button),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/** Person silhouette inside camera-style corner brackets. */
@Composable
private fun FaceScanIcon(tint: Color) {
    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 10f)
            val corner = size.width * 0.22f
            val bracket = { startX: Float, startY: Float, midX: Float, midY: Float, endX: Float, endY: Float ->
                drawPath(
                    path = Path().apply {
                        moveTo(startX, startY)
                        lineTo(midX, midY)
                        lineTo(endX, endY)
                    },
                    color = tint,
                    style = stroke
                )
            }
            // top-left, top-right, bottom-left, bottom-right
            bracket(0f, corner, 0f, 0f, corner, 0f)
            bracket(size.width - corner, 0f, size.width, 0f, size.width, corner)
            bracket(0f, size.height - corner, 0f, size.height, corner, size.height)
            bracket(size.width - corner, size.height, size.width, size.height, size.width, size.height - corner)
        }
        Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(72.dp)
        )
    }
}
