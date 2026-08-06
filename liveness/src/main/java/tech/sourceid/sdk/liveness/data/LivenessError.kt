package tech.sourceid.sdk.liveness.data

/**
 * Structured error delivered through `onError`.
 *
 * - [userMessage] is friendly, actionable text safe to show end users
 *   (toast/dialog/snackbar).
 * - [debugMessage] carries the full technical detail for logging and bug
 *   reports; the SDK also logs it under the `LivenessSDK` Logcat tag.
 * - [code] is a stable machine-readable identifier for programmatic handling.
 */
data class LivenessError(
    val code: String,
    val userMessage: String,
    val debugMessage: String
) {
    /** True when the user cancelled the flow rather than failing it. */
    val isCancelled: Boolean get() = code == CANCELLED

    override fun toString(): String = "[$code] $debugMessage"

    companion object {
        const val CANCELLED = "CANCELLED"
        const val CAMERA_PERMISSION_DENIED = "CAMERA_PERMISSION_DENIED"
        const val INVALID_ARGUMENTS = "INVALID_ARGUMENTS"
        const val CONFIG_FAILED = "CONFIG_FAILED"
        const val SESSION_NOT_USABLE = "SESSION_NOT_USABLE"
        const val STATUS_CHECK_FAILED = "STATUS_CHECK_FAILED"
        const val DETECTOR_FAILED = "DETECTOR_FAILED"

        fun cancelled() = LivenessError(
            code = CANCELLED,
            userMessage = "Liveness check cancelled",
            debugMessage = "User backed out of the flow before completing it"
        )
    }
}
