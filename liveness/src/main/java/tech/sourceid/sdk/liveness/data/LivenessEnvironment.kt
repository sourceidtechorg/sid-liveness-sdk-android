package tech.sourceid.sdk.liveness.data

/**
 * SourceID gateway environment. Consumers pass only the environment — the
 * SDK derives the gateway base URL internally.
 */
enum class LivenessEnvironment(internal val baseUrl: String) {
    PRODUCTION("https://core-api.sourceid.tech/v1/api/liveness"),
    SANDBOX("https://core-api.sbx.sourceid.tech/v1/api/liveness"),
    DEVELOPMENT("https://api-rd.tailfaed50.ts.net/v1/api/liveness");

    companion object {
        /** Case-insensitive lookup, e.g. "production", "SANDBOX". */
        fun fromName(name: String?): LivenessEnvironment? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}
