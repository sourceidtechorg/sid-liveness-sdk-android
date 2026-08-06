package tech.sourceid.sdk.liveness.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
import tech.sourceid.sdk.liveness.data.LivenessUIConfig

private val GUIDELINES = listOf(
    "Use a well-lit space with even lighting, avoid very dark or overly bright areas.",
    "Keep your head centered and follow prompts.",
    "Remove any head coverings which may be obstructing your face.",
    "After 5 fails, apply a 30-60 minute timeout.",
    "When you're ready, tap Start."
)

/**
 * Instruction page shown before the capture flow starts, mirroring the
 * SourceID web liveness start page.
 */
@Composable
internal fun LivenessStartScreen(
    config: LivenessUIConfig,
    onStart: () -> Unit
) {
    val isDark = config.theme == "dark"
    val textColor = if (isDark) Color.White else Color(0xFF111111)
    val cardBackground = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF5F3FA)
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
            text = config.customTitle ?: "Liveness Check",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "We need to verify you are a real person for security purposes.",
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
                text = "Your face needs to be verified against your information. " +
                    "Please follow the guidelines below to ensure proper capture:",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            GUIDELINES.forEach { line ->
                Text(
                    text = "- $line",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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
            Text(text = "Start Liveness Check", color = Color.White)
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
