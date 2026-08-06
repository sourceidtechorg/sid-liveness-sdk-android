package tech.sourceid.sdk.liveness.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.util.Log
import com.amplifyframework.ui.liveness.model.FaceLivenessDetectionException
import com.amplifyframework.ui.liveness.ui.FaceLivenessDetector
import tech.sourceid.liveness.R
import tech.sourceid.sdk.liveness.data.LivenessError
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.ui.theme.buildColorScheme

/**
 * Maps the AWS detector failure to a [LivenessError] with a friendly user
 * message per failure kind. [FaceLivenessDetectionException] is not a
 * [Throwable] (its toString() is just "ClassName@hash"), so the debug
 * message is rebuilt from its actual parts.
 */
internal fun FaceLivenessDetectionException.toLivenessError(): LivenessError {
    if (this is FaceLivenessDetectionException.UserCancelledException) {
        return LivenessError.cancelled()
    }

    // Some subtypes are internal to the AWS library, so match by class name.
    val userMessage = when (this::class.simpleName) {
        "SessionNotFoundException" ->
            "This verification session is invalid or was already used. Please start a new check."
        "SessionTimedOutException" ->
            "The verification session timed out. Please start a new check."
        "FaceInOvalMatchExceededTimeLimitException" ->
            "We couldn't capture your face in time. Find a well-lit spot and try again."
        "CameraPermissionDeniedException" ->
            "Camera access is required for the liveness check. Please allow camera access and try again."
        "AccessDeniedException" ->
            "The verification service rejected the request. Please try again later."
        else ->
            "Something went wrong during the liveness check. Please check your connection and try again."
    }

    val kind = this::class.simpleName ?: "FaceLivenessDetectionException"
    val debugMessage = buildString {
        append("$kind: $message")
        if (recoverySuggestion.isNotBlank()) append(" — $recoverySuggestion")
        // The wrapped throwable's TYPE often carries the real story (e.g.
        // WebSocket/timeout classes) even when its message is generic.
        var cause: Throwable? = throwable
        var depth = 0
        while (cause != null && depth < 3) {
            append(" (cause: ${cause.javaClass.simpleName}: ${cause.message})")
            cause = cause.cause?.takeIf { it !== cause }
            depth++
        }
    }

    return LivenessError(
        code = LivenessError.DETECTOR_FAILED,
        userMessage = userMessage,
        debugMessage = debugMessage
    )
}


@Composable
fun FaceLivenessScreen(
    modifier: Modifier = Modifier,
    sessionId: String,
    region: String,
    config: LivenessUIConfig,
    onComplete: () -> Unit,
    onError: (LivenessError) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            onError(
                LivenessError(
                    code = LivenessError.CAMERA_PERMISSION_DENIED,
                    userMessage = "Camera access is required for the liveness check. Please allow camera access in Settings and try again.",
                    debugMessage = "Runtime CAMERA permission denied by the user"
                )
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(android.Manifest.permission.CAMERA)
        }
    }

    var showInstructions by remember { mutableStateOf(true) }

    if (hasCameraPermission) {
        MaterialTheme(colorScheme = buildColorScheme(config)) {
            if (showInstructions) {
                LivenessStartScreen(config = config) { showInstructions = false }
                return@MaterialTheme
            }
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                val isDarkMode = config.theme == "dark"
                val brandColor = if (isDarkMode) {
                    Color.White.copy(alpha = 0.6f)
                } else {
                    Color.Black.copy(alpha = 0.6f)
                }
                if (config.customTitle != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = config.customTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = brandColor
                        )

                    }
                }
                // Main detector takes most of the space
                Box(modifier = Modifier.weight(1f)) {
                    FaceLivenessDetector(
                        sessionId = sessionId,
                        region = region,
                        // The SDK shows its own instruction page first.
                        disableStartView = true,
                        onComplete = { onComplete() },
                        onError = { error ->
                            Log.e(LivenessSDK.TAG, "Face liveness failed: ${error.message}", error.throwable)
                            onError(error.toLivenessError())
                        }
                    )
                }

                // Conditional branding footer
                if (!config.hideBranding) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(
                                text = "Powered by ",
                                style = MaterialTheme.typography.titleLarge,
                                color = brandColor
                            )
                            Image(
                                modifier = Modifier.size(96.dp),
                                painter = painterResource(
                                    // sid_logo has white lettering (for dark
                                    // backgrounds); sid_logo_light has dark
                                    // lettering for the light theme.
                                    id = if (isDarkMode) R.drawable.sid_logo else R.drawable.sid_logo_light
                                ),
                                contentDescription = null
                            )
                        }


                    }
                }
            }
        }
    } else {
        Text(
            text = "Camera permission required to continue",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}