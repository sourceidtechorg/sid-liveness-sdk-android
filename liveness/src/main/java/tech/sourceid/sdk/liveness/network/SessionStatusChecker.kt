package tech.sourceid.sdk.liveness.network

import org.json.JSONObject
import tech.sourceid.sdk.liveness.data.LivenessEnvironment
import java.net.HttpURLConnection
import java.net.URL

/** The gateway's view of a liveness session. [confidence] and
 * [referenceImageUrl] are present once the session has been run and scored
 * (status "SUCCEEDED"). */
data class GatewaySessionResult(
    val status: String,
    val confidence: Double?,
    val referenceImageUrl: String?
)

/** Outcome of a session status query, kept distinct so callers can phrase
 * user-facing messages accurately (gateway said no vs. couldn't reach it). */
internal sealed class StatusCheckOutcome {
    /** The gateway answered with the session's state. */
    data class Status(val result: GatewaySessionResult) : StatusCheckOutcome()

    /** The gateway answered but rejected the request (non-2xx). This is also
     * what happens for sessions that were already run. */
    data class GatewayRejected(val httpCode: Int, val gatewayMessage: String?) : StatusCheckOutcome()

    /** The gateway could not be reached or answered garbage. */
    data class Unreachable(val detail: String) : StatusCheckOutcome()
}

/**
 * Queries the gateway's `POST /liveness/liveness-result` endpoint for the
 * status of a session. The endpoint expects the session id in the
 * `reference` field and reports `data.status` (e.g. "CREATED") for a
 * session that has not been run yet.
 */
internal object SessionStatusChecker {

    fun fetchStatus(environment: LivenessEnvironment, sessionId: String): StatusCheckOutcome {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(environment.baseUrl.trimEnd('/') + "/liveness-result")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            connection.outputStream.use {
                it.write(JSONObject().put("reference", sessionId).toString().toByteArray())
            }

            val httpCode = connection.responseCode
            val body = (if (httpCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (httpCode !in 200..299) {
                val gatewayMessage = runCatching {
                    JSONObject(body).optString("message")
                }.getOrNull().takeUnless { it.isNullOrBlank() }
                return StatusCheckOutcome.GatewayRejected(httpCode, gatewayMessage)
            }

            val data = JSONObject(body).optJSONObject("data")
            val status = data?.optString("status")
            if (status.isNullOrBlank()) {
                StatusCheckOutcome.Unreachable("Gateway response has no session status (body: ${body.take(200)})")
            } else {
                StatusCheckOutcome.Status(
                    GatewaySessionResult(
                        status = status,
                        confidence = if (data.has("confidence")) data.optDouble("confidence") else null,
                        referenceImageUrl = data.optString("referenceImageUrl").takeUnless { it.isBlank() }
                    )
                )
            }
        } catch (e: Exception) {
            StatusCheckOutcome.Unreachable("${e.javaClass.simpleName}: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }

    fun fetchResult(environment: LivenessEnvironment, sessionId: String): StatusCheckOutcome {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(environment.baseUrl)//.trimEnd('/') + "/liveness-result")
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }
            connection.outputStream.use {
                it.write(JSONObject().put("reference", sessionId).toString().toByteArray())
            }

            val httpCode = connection.responseCode
            val body = (if (httpCode in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (httpCode !in 200..299) {
                val gatewayMessage = runCatching {
                    JSONObject(body).optString("message")
                }.getOrNull().takeUnless { it.isNullOrBlank() }
                return StatusCheckOutcome.GatewayRejected(httpCode, gatewayMessage)
            }

            val data = JSONObject(body).optJSONObject("data")
            val status = data?.optString("status")
            if (status.isNullOrBlank()) {
                StatusCheckOutcome.Unreachable("Gateway response has no session status (body: ${body.take(200)})")
            } else {
                StatusCheckOutcome.Status(
                    GatewaySessionResult(
                        status = status,
                        confidence = if (data.has("confidence")) data.optDouble("confidence") else null,
                        referenceImageUrl = data.optString("referenceImageUrl").takeUnless { it.isBlank() }
                    )
                )
            }
        } catch (e: Exception) {
            StatusCheckOutcome.Unreachable("${e.javaClass.simpleName}: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }
}
