package tech.sourceid.sdk.liveness.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.amplifyframework.AmplifyException
import com.amplifyframework.core.Amplify
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import tech.sourceid.sdk.liveness.data.LivenessError
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.ui.theme.SDKTestersTheme
import tech.sourceid.sdk.liveness.ui.theme.backgroundColor

class LivenessActivity : ComponentActivity() {

    companion object {
        /** Intent extra carrying the whole [LivenessUIConfig] (Serializable). */
        const val EXTRA_CONFIG = "tech.sourceid.sdk.liveness.CONFIG"

        // Amplify may only be configured once per process; this also covers
        // host apps that configure Amplify themselves before launching the flow.
        @Volatile
        private var amplifyReady = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!amplifyReady) {
            try {
                Amplify.addPlugin(AWSCognitoAuthPlugin())
                Amplify.configure(applicationContext)
                amplifyReady = true
                Log.i("LivenessActivity", "Amplify initialized successfully")
            } catch (e: AmplifyException) {
                if (e.message?.contains("already", ignoreCase = true) == true) {
                    amplifyReady = true
                    Log.i("LivenessActivity", "Amplify already configured")
                } else {
                    Log.e("LivenessActivity", "Could not initialize Amplify", e)
                    LivenessSDK.notifyResult(
                        LivenessResult.Error(
                            LivenessError(
                                code = LivenessError.CONFIG_FAILED,
                                userMessage = "The verification service could not start. Please try again later.",
                                debugMessage = "Amplify initialization failed: ${e.message} (recovery: ${e.recoverySuggestion})"
                            )
                        )
                    )
                    finish()
                    return
                }
            }
        }

        val sessionId = intent.getStringExtra("sessionId") ?: ""
        val region = intent.getStringExtra("region") ?: ""
        val config = readConfig()

        setContent {
            SDKTestersTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = config.backgroundColor
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        FaceLivenessScreen(
                            sessionId = sessionId,
                            region = region,
                            config = config,
                            onComplete = {
                                LivenessSDK.notifyResult(
                                    LivenessResult.Success("Liveness flow completed successfully.")
                                )
                                finish()
                            },
                            onError = { err ->
                                LivenessSDK.notifyResult(LivenessResult.Error(err))
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    /** The config passed by [LivenessSDK.launch], or one rebuilt from the pre-1.9 per-field extras. */
    private fun readConfig(): LivenessUIConfig {
        val passed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_CONFIG, LivenessUIConfig::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_CONFIG) as? LivenessUIConfig
        }
        return passed ?: LivenessUIConfig(
            hideBranding = intent.getBooleanExtra("hideBranding", false),
            customTitle = intent.getStringExtra("customTitle"),
            theme = intent.getStringExtra("theme") ?: "light",
            primaryColorHex = intent.getStringExtra("primaryColorHex")
        )
    }

    override fun onDestroy() {
        // If the user backs out (or the activity is killed) before a result was
        // delivered, resolve the pending callback so callers never hang.
        if (isFinishing && LivenessSDK.hasPendingCallback) {
            LivenessSDK.notifyResult(LivenessResult.Error(LivenessError.cancelled()))
        }
        super.onDestroy()
    }
}
