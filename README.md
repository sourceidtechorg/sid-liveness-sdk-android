# SID Liveness SDK (Android)

The **SourceID Liveness SDK** provides a fast, secure, and customizable way to integrate **face liveness detection** into your Android application. It wraps the [AWS Amplify Face Liveness](https://ui.docs.amplify.aws/android/connected-components/liveness) detector (Amazon Rekognition Face Liveness) and adds SourceID session handling, theming, and branding.

---

## Features

* Real-time face liveness detection (AWS Rekognition Face Liveness)
* Customizable UI — theme, title, primary color, branding footer
* Automatic AWS Amplify initialization (no host-app setup required)
* Runtime camera-permission handling with error callbacks
* Guaranteed single callback — including user cancellation
* Kotlin + Jetpack Compose

---

## Installation

The SDK is published via [JitPack](https://jitpack.io).

**1. Add the JitPack repository** to your `settings.gradle(.kts)`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**2. Add the dependency** to your app-level `build.gradle(.kts)`:

```kotlin
dependencies {
    implementation("com.github.sourceidtechorg:sid-liveness-sdk-android:v1.8.0")
}
```

> **Note:** [`sourceidtechorg/sid-liveness-sdk-android`](https://github.com/sourceidtechorg/sid-liveness-sdk-android) is the official repository. The historical `com.github.EQua-Dev:liveness-expo` coordinate still resolves for existing consumers, but new integrations should use the coordinate above.

**3. Declare the camera permission** in your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```

The SDK requests the permission at runtime for you; the manifest entry is still required.

---

## Requirements

| Requirement | Version |
| --- | --- |
| **minSdk** | 24 (Android 7.0) |
| **compileSdk** | 36 |
| **Kotlin** | 2.x |
| **Jetpack Compose** | required (the SDK UI is Compose-based) |

---

## Prerequisite: a liveness session ID

A liveness check runs against a **session** created server-side with Amazon Rekognition (`CreateFaceLivenessSession`). Your backend (e.g. the SourceID gateway's `POST /liveness/generate-liveness-url`) creates the session and returns the `sessionId`; after the flow completes, your backend fetches the confidence result (`GetFaceLivenessSessionResults`).

The SDK **never scores the check itself** — it runs the capture flow. `onSuccess` means the user completed the flow. When you pass a `LivenessEnvironment`, the SDK also fetches the scored result from the gateway right after completion and hands it to `onSuccess` (status, confidence, reference image URL); without it, fetch the result from your backend.

---

## Usage

Launch the flow from any `Activity`, `Fragment`, or Compose click handler:

```kotlin
import tech.sourceid.sdk.liveness.ui.LivenessSDK
import tech.sourceid.sdk.liveness.data.LivenessUIConfig

LivenessSDK.launch(
    context = this,
    sessionId = sessionIdFromYourBackend,
    region = "us-east-1",
    config = LivenessUIConfig(
        hideBranding = false,
        customTitle = "Face Verification",
        theme = "dark",
        primaryColorHex = "#0A84FF"
    ),
    onSuccess = { message, sessionResult ->
        // The user completed the capture flow.
        // sessionResult is non-null when environment was provided:
        //   sessionResult.status            // e.g. "SUCCEEDED"
        //   sessionResult.confidence        // 0–100 liveness score
        //   sessionResult.referenceImageUrl // short-lived signed image URL
        // Without environment, fetch the result from your backend instead.
    },
    onError = { error ->
        // Permission denied, cancelled, session/network failure, ...
    }
)
```

### Pre-flight session status check (optional)

Liveness sessions are single-use and short-lived. To avoid opening the camera
for a session that was already used or has expired, pass a `LivenessEnvironment`
— the SDK derives the gateway base URL internally, asks for the session's
status first, and only launches the capture flow when the status is `CREATED`:

```kotlin
import tech.sourceid.sdk.liveness.data.LivenessEnvironment

LivenessSDK.launch(
    context = this,
    sessionId = sessionIdFromYourBackend,
    region = "us-east-1",
    config = LivenessUIConfig(theme = "dark"),
    environment = LivenessEnvironment.PRODUCTION, // or SANDBOX / DEVELOPMENT
    onSuccess = { /* ... */ },
    onError = { error ->
        // Also fires when the status check fails, e.g.
        // "Session cannot be used (status: COMPLETED). Generate a new session."
    }
)
```

The check calls the gateway's `liveness-result` endpoint with the session id
as the `reference` — no keys or tokens required. Omit `environment` to skip
the check and launch directly.

### Callbacks

| Callback | When it fires |
| --- | --- |
| `onSuccess(message, sessionResult: GatewaySessionResult?)` | The capture flow completed. `sessionResult` carries the scored gateway result (status/confidence/reference image URL) when `environment` was provided; verify server-side otherwise. |
| `onError(error: LivenessError)` | Anything else — see the error contract below. |

Exactly **one** callback fires per `launch`.

### Error contract (`LivenessError`)

`onError` receives a structured `LivenessError` that separates what you show
the user from what you log:

* **`userMessage`** — friendly, actionable text safe to show end users (toast/dialog/snackbar).
* **`debugMessage`** — full technical detail (exception type, gateway HTTP code, cause). The SDK also logs every failure itself under the `LivenessSDK` Logcat tag: `adb logcat -s LivenessSDK`.
* **`code`** — stable identifier for programmatic handling (plus the `error.isCancelled` shortcut).

```kotlin
onError = { error ->
    Log.e("MyApp", "Liveness failed $error")   // toString() = "[CODE] debug detail"
    Toast.makeText(this, error.userMessage, Toast.LENGTH_LONG).show()
}
```

| Code | Typical user message | Meaning |
| --- | --- | --- |
| `CANCELLED` | "Liveness check cancelled" | The user backed out before completing the flow. |
| `CAMERA_PERMISSION_DENIED` | "Camera access is required…" | The runtime camera permission was declined. |
| `INVALID_ARGUMENTS` | "Verification could not start…" | `launch` was called with a blank session id. |
| `SESSION_NOT_USABLE` | "This verification session has already been used or has expired…" | Pre-flight check: the status was not `CREATED`, or the gateway rejected the session (it also 400s for already-run sessions). |
| `STATUS_CHECK_FAILED` | "We couldn't verify your session…" | The pre-flight check couldn't reach the gateway (network/timeout). |
| `CONFIG_FAILED` | "The verification service could not start…" | AWS Amplify could not be configured. |
| `DETECTOR_FAILED` | Varies by cause — invalid/expired session, capture timeout, service rejection, connection trouble | AWS Face Liveness detector failure; `debugMessage` names the exact exception type, message, and recovery suggestion. |

---

## UI customization

`LivenessUIConfig`:

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `hideBranding` | `Boolean` | `false` | Hide the "Powered by SourceID" footer |
| `customTitle` | `String?` | `null` | Title shown above the camera view (omitted when `null`) |
| `theme` | `String` | `"light"` | `"light"` or `"dark"` |
| `primaryColorHex` | `String?` | `null` | Accent color, e.g. `"#0A84FF"` (falls back to theme default when null/invalid) |

---

## AWS configuration

The SDK bundles its own `amplifyconfiguration.json` (SourceID's Cognito identity pool) and initializes Amplify automatically on first launch of the flow — your app does not need any Amplify setup.

* If your app **already uses Amplify**, the SDK detects the existing configuration and reuses it.
* To point the SDK at **your own Cognito pool**, place an `amplifyconfiguration.json` in your app's `res/raw/` — app resources override the library's copy.

---

## Publishing (maintainers)

Releases are consumed through JitPack, which builds from git tags:

```bash
git tag v1.8.0
git push origin v1.8.0        # or the appropriate remote
```

The maven coordinates come from the repository (`com.github.<owner>:<repo>:<tag>`); the `maven-publish` block in `liveness/build.gradle.kts` supplies the POM metadata. To verify a build locally:

```bash
./gradlew :liveness:assembleRelease :liveness:publishReleasePublicationToMavenLocal
```

Release checklist for a new tag `vX.Y.Z`:

1. Bump `version` in `liveness/build.gradle.kts` and the versions in this README.
2. Bump the `:app` dependency to `vX.Y.Z` too — the tester consumes the published artifact.
3. Commit, tag `vX.Y.Z`, push branch + tag.
4. Trigger and watch the build: `curl -s https://jitpack.io/api/builds/com.github.sourceidtechorg/sid-liveness-sdk-android/vX.Y.Z` (first artifact request starts it; `"status": "ok"` = published; on failure read `.../vX.Y.Z/build.log`).
5. A failed tag stays failed — fix and cut `vX.Y.Z+1`, never move a tag.

> **Why `jitpack.yml` matters:** it restricts the JitPack build to `:liveness`. Without it JitPack builds `:app` as well, which depends on the very artifact version being built — a circular dependency that fails the release (this killed v1.6.0/v1.6.1).

---

## License

MIT © SourceID

## Support

📧 [dev@sourceid.tech](mailto:dev@sourceid.tech) · 🌐 [https://sourceid.tech](https://sourceid.tech)
