package tech.sourceid.sdktesters

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import tech.sourceid.sdk.liveness.data.LivenessEnvironment
//import com.amplifyframework.core.Amplify
//import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.ui.LivenessLaunchParams
import tech.sourceid.sdk.liveness.ui.LivenessResult
import tech.sourceid.sdk.liveness.ui.LivenessSDK
import tech.sourceid.sdktesters.ui.theme.SDKTestersTheme


class MainActivity : ComponentActivity() {
    val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        // Initialize Amplify Auth
//        try {
//            Amplify.addPlugin(AWSCognitoAuthPlugin())
//            Amplify.configure(applicationContext)
//            Log.i("MainActivity", "Amplify initialized successfully")
//        } catch (e: Exception) {
//            Log.e("MainActivity", "Could not initialize Amplify", e)
//        }

        enableEdgeToEdge()
        setContent {
            SDKTestersTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                      /*  val launcher = rememberLauncherForActivityResult(
                            contract = LivenessSDK.Contract()
                        ) { result ->
                            when (result) {
                                is LivenessResult.Success -> Log.i("App", result.message)
                                is LivenessResult.Error -> Log.e("App", result.message)
                            }
                        }*/

                        Button(onClick = {
                            LivenessSDK.launch(
                                context = this@MainActivity,
                                sessionId = "963a24e9-cf58-4840-881b-8399d03cc0f9",
                                environment = LivenessEnvironment.SANDBOX,
//                                region = "us-east-1",
                                config = LivenessUIConfig(
                                    customTitle = "Verify Identity",
                                    theme = "dark",
                                    primaryColorHex = "#FF5733"
                                ),
                                onSuccess = { message, sessionResult ->
                                    // sessionResult is non-null when apiConfig was provided:
                                    // status, confidence score, and reference image URL.
                                    Log.i(TAG, "Liveness result: $sessionResult")
                                    val display = sessionResult?.confidence
                                        ?.let { "$message (confidence: ${"%.1f".format(it)}%)" }
                                        ?: message
                                    Toast.makeText(this@MainActivity, display, Toast.LENGTH_LONG).show()
                                },
                                onError = { error ->
                                    // Full technical detail for debugging ("[CODE] debug message")
                                    Log.e(TAG, "Liveness failed $error")
                                    // Friendly text for the user
                                    Toast.makeText(this@MainActivity, error.userMessage, Toast.LENGTH_LONG).show()
                                }
                            )

//                            launcher.launch(
//                                LivenessLaunchParams(
//                                    sessionId = "a12e9b8a-8799-4ded-91aa-dae843b7a9e1",
//                                    region = "us-east-1",
//                                    config = LivenessUIConfig(theme = "dark")
//                                )
//                            )
                        }) {
                            Text("Start Liveness")
                        }
                        /*
                                                FaceLivenessScreen(
                                                    sessionId = "c4f07f5c-9393-4c59-b86e-e2eef2e1040f",
                                                    region = "us-east-1",
                                                    config = LivenessUIConfig(
                                                        hideBranding = true,
                                                        customTitle = "Face Verification",
                                                        theme = "dark",
                                                        primaryColorHex = "#FF5733"
                                                    ),
                                                    onComplete = { Log.i("Liveness", "✅ Flow complete!") },
                                                    onError = { err -> Log.e("Liveness", "❌ Error: $err") }
                                                )*/
                    }
                }
            }
        }
    }
}
