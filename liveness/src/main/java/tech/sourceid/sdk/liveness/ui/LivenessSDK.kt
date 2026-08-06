package tech.sourceid.sdk.liveness.ui

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import tech.sourceid.sdk.liveness.data.LivenessEnvironment
import tech.sourceid.sdk.liveness.data.LivenessError
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.network.GatewaySessionResult
import tech.sourceid.sdk.liveness.network.SessionStatusChecker
import tech.sourceid.sdk.liveness.network.StatusCheckOutcome
import kotlin.concurrent.thread

data class LivenessLaunchParams(
    val sessionId: String,
    val region: String,
    val config: LivenessUIConfig
)

sealed class LivenessResult {
    /** [sessionResult] is populated (status/confidence/reference image) when
     * an environment was provided to `launch`; null otherwise or when
     * the post-completion fetch failed. */
    data class Success(
        val message: String = "Completed",
        val sessionResult: GatewaySessionResult? = null
    ) : LivenessResult()

    data class Error(val error: LivenessError) : LivenessResult()
}

object LivenessSDK {
    internal const val TAG = "LivenessSDK"

    private var callback: ((LivenessResult) -> Unit)? = null

    /** Session + gateway environment for the post-completion result fetch. */
    private var resultFetchContext: Pair<String, LivenessEnvironment>? = null

    /** True while the post-completion result fetch is running. */
    @Volatile
    private var fetchingResult = false

    /** Session status required before the capture flow is allowed to start. */
    private const val STATUS_CREATED = "CREATED"

    /**
     * Starts the liveness capture flow.
     *
     * When [environment] is provided, the session's status is first verified
     * against the gateway and the flow only launches if the status is
     * `CREATED`; any other status (already used, expired, ...) is reported
     * through [onError] without opening the camera. When [environment] is null
     * the check is skipped and the flow launches directly.
     *
     * [onError] receives a [LivenessError]: show `userMessage` to the user,
     * log `debugMessage` (the SDK already logs it under the `LivenessSDK`
     * tag), branch on `code` programmatically.
     */
    fun launch(
        context: Context,
        sessionId: String,
        region: String = "us-east-1",
        config: LivenessUIConfig,
        environment: LivenessEnvironment? = null,
        onSuccess: ((String, GatewaySessionResult?) -> Unit)? = null,
        onError: ((LivenessError) -> Unit)? = null
    ) {
        callback = {
            when (it) {
                is LivenessResult.Success -> onSuccess?.invoke(it.message, it.sessionResult)
                is LivenessResult.Error -> onError?.invoke(it.error)
            }
        }
        fetchingResult = false
        resultFetchContext = environment?.let { sessionId to it }

        if (sessionId.isBlank()) {
            notifyResult(
                LivenessResult.Error(
                    LivenessError(
                        code = LivenessError.INVALID_ARGUMENTS,
                        userMessage = "Verification could not start. Please go back and try again.",
                        debugMessage = "launch() called with a blank sessionId"
                    )
                )
            )
            return
        }

        val intent = Intent(context, LivenessActivity::class.java).apply {
            putExtra("sessionId", sessionId)
            putExtra("region", region)
            putExtra("customTitle", config.customTitle)
            putExtra("theme", config.theme)
            putExtra("primaryColorHex", config.primaryColorHex)
            putExtra("hideBranding", config.hideBranding)
        }

        if (environment == null) {
            context.startActivity(intent)
            return
        }

        val mainHandler = Handler(Looper.getMainLooper())
        thread(name = "LivenessSessionStatus") {
            val outcome = SessionStatusChecker.fetchResult(environment, sessionId)
            mainHandler.post {
                when (outcome) {
                    is StatusCheckOutcome.Status -> {
                        if (outcome.result.status.equals(STATUS_CREATED, ignoreCase = true)) {
                            context.startActivity(intent)
                        } else {
                            notifyResult(
                                LivenessResult.Error(
                                    LivenessError(
                                        code = LivenessError.SESSION_NOT_USABLE,
                                        userMessage = "This verification session has already been used or has expired. Please start a new check.",
                                        debugMessage = "Gateway reported session status \"${outcome.result.status}\"; launch requires \"$STATUS_CREATED\""
                                    )
                                )
                            )
                        }
                    }
                    is StatusCheckOutcome.GatewayRejected -> {
                        // 401/403 is an endpoint/config problem, not a spent
                        // session; other rejections (the gateway also 400s for
                        // already-run sessions) mean "don't open the camera".
                        val error = if (outcome.httpCode == 401 || outcome.httpCode == 403) {
                            LivenessError(
                                code = LivenessError.STATUS_CHECK_FAILED,
                                userMessage = "We couldn't verify your session. Please try again later.",
                                debugMessage = "Gateway rejected the status check with HTTP ${outcome.httpCode} (auth) — ${outcome.gatewayMessage ?: "no message"}"
                            )
                        } else {
                            LivenessError(
                                code = LivenessError.SESSION_NOT_USABLE,
                                userMessage = "This verification session has already been used or has expired. Please start a new check.",
                                debugMessage = "Gateway rejected the status check: HTTP ${outcome.httpCode} — ${outcome.gatewayMessage ?: "no message"}"
                            )
                        }
                        notifyResult(LivenessResult.Error(error))
                    }
                    is StatusCheckOutcome.Unreachable -> {
                        notifyResult(
                            LivenessResult.Error(
                                LivenessError(
                                    code = LivenessError.STATUS_CHECK_FAILED,
                                    userMessage = "We couldn't verify your session. Please check your internet connection and try again.",
                                    debugMessage = "Status check failed before reaching a verdict: ${outcome.detail}"
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    internal fun notifyResult(result: LivenessResult) {
        if (result is LivenessResult.Error) {
            Log.e(TAG, "Liveness failed ${result.error}")
            deliver(result)
            return
        }

        // After a successful capture, fetch the scored result from the
        // gateway (when credentials were provided) and enrich the callback.
        val context = resultFetchContext
        if (result is LivenessResult.Success && context != null && result.sessionResult == null) {
            val (sessionId, environment) = context
            fetchingResult = true
            val mainHandler = Handler(Looper.getMainLooper())
            thread(name = "LivenessResultFetch") {
                val outcome = SessionStatusChecker.fetchResult(environment, sessionId)
                val enriched = when (outcome) {
                    is StatusCheckOutcome.Status -> result.copy(sessionResult = outcome.result)
                    else -> {
                        // The capture itself succeeded — deliver success and
                        // leave the result fetch to the host's backend.
                        Log.w(TAG, "Could not fetch session result after completion: $outcome")
                        result
                    }
                }
                mainHandler.post { deliver(enriched) }
            }
            return
        }

        deliver(result)
    }

    private fun deliver(result: LivenessResult) {
        callback?.invoke(result)
        callback = null // Avoid memory leaks
        resultFetchContext = null
        fetchingResult = false
    }

    // Whether a launch is still awaiting its result (used to detect user
    // cancellation). False during the post-completion result fetch so the
    // closing activity is not mistaken for a cancellation.
    internal val hasPendingCallback: Boolean
        get() = callback != null && !fetchingResult
}
