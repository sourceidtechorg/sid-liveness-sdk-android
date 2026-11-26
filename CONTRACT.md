# SourceID Liveness SDK — cross-platform contract

This is the single source of truth for every SourceID liveness SDK:

| Package | Repository | Role |
| --- | --- | --- |
| iOS (`LivenessCheck`) | `sourceidtechorg/sid-liveness-sdk-ios` | Implements the contract natively |
| Android (`tech.sourceid.sdk.liveness`) | `sourceidtechorg/sid-liveness-sdk-android` | Implements the contract natively |
| Flutter (`liveness_sdk`) | `sourceidtechorg/sid-liveness-sdk-flutter` | Passes the contract through to the native SDKs |
| React Native (`@sourceid/liveness-react-native`) | `sourceidtechorg/sid-liveness-sdk-react-native` | Passes the contract through to the native SDKs |

**Rules**

1. Behaviour and UI customisation are implemented once per native SDK. Wrappers only translate types; they add no UI and no logic.
2. Field names, defaults and error codes below are identical on every platform. A change here ships in iOS and Android in the same release, with the same version number.
3. Each wrapper's changelog states which native version it consumes.

The file is kept identical in the iOS and Android repositories.

---

## Starting a check

| Input | Type | Default | Notes |
| --- | --- | --- | --- |
| `sessionId` | string | required | AWS Face Liveness session id, created server-side. Never create sessions or hold API keys in an app. |
| `region` | string | `"us-east-1"` | AWS region of the session. |
| `config` | `LivenessUIConfig` | defaults below | Look and behaviour. |
| `environment` | `"production"` \| `"sandbox"` \| `"development"` \| null | null | When set, the SDK checks the session is `CREATED` before opening the camera, and fetches the scored result after capture. Leave null when results must stay server-side. |

The outcome is delivered exactly once: success, or one `LivenessError`.

## `LivenessUIConfig`

| Field | Type | Default | Since | Notes |
| --- | --- | --- | --- | --- |
| `hideBranding` | bool | `false` | 1.0 | Hides the "Powered by SourceID" footer. |
| `customTitle` | string? | null | 1.0 | Title above the camera and on the start page. Hidden above the camera when null. |
| `theme` | `"light"` \| `"dark"` | `"light"` | 1.0 | Base palette. Every colour below falls back to it. |
| `primaryColorHex` | string? | null | 1.0 | Shorthand for `colors.primary`. `colors.primary` wins when both are set. |
| `showStartPage` | bool | `true` | 1.9 | Shows the SourceID instruction page before the camera. Set `false` when the host app has its own intro. |
| `hideCancelButton` | bool | `false` | 1.9 | Hides AWS's close button during capture. Only hide it when the host gives the person another way out. |
| `colors` | `LivenessColors` | all null | 1.9 | Full palette, see below. |
| `fonts` | `LivenessFonts` | all null | 1.9 | Custom fonts, see below. |

### `LivenessColors`

Every field is an optional `"#RRGGBB"` string. Invalid or missing values fall back to the `theme` default.

| Field | Used for |
| --- | --- |
| `primary` | Buttons, accents, start-page icon, title |
| `onPrimary` | Text and icons on `primary` |
| `background` | Screen background around the camera, start page, title bar and footer |
| `onBackground` | Text on `background`, including capture guidance |
| `surface` | Start-page guideline card; AWS surfaces |
| `error` / `onError` | AWS error banners |
| `errorContainer` / `onErrorContainer` | AWS error and hint containers |

Not themeable without forking AWS Amplify: the oval outline, the white overlay around the oval during the challenge, the oval geometry (set by the AWS service) and the colour-flash sequence (anti-spoofing).

### `LivenessFonts`

Every field is an optional font name. Unknown names fall back to the system font.

| Field | Used for |
| --- | --- |
| `regular` | Body, captions, guidance |
| `medium` | Headlines, labels, buttons (falls back to `regular`) |
| `bold` | Titles (falls back to `medium`, then `regular`) |

- **iOS:** the PostScript name of a font registered by the app (for example listed under `UIAppFonts`), such as `"Exo2-Regular"`.
- **Android:** the file name, without extension, of a font in the app's `assets/fonts/` (`.ttf` or `.otf`), or the name of a `res/font` resource.

### Text and languages

All visible text can be replaced, including translations such as Arabic, by adding string resources with the same keys to the app:

- **iOS:** `Localizable.strings` in the app bundle (for example `ar.lproj/Localizable.strings`).
- **Android:** `res/values/strings.xml` or `res/values-ar/strings.xml` in the app.

AWS keys start with `amplify_ui_liveness_`; SourceID start-page keys start with `sid_liveness_`.

## Result

| Field | Type | Notes |
| --- | --- | --- |
| `status` | `"success"` | The capture completed. This is not a verdict. |
| `sessionStatus` | string? | Only when `environment` was set, for example `SUCCEEDED`. |
| `confidence` | number? | Only when `environment` was set. Do not show to end users unless the product requires it. |
| `referenceImageUrl` | string? | Only when `environment` was set. Short-lived signed URL. |

## `LivenessError`

| Field | Notes |
| --- | --- |
| `code` | Stable identifier, below. Branch on this. |
| `userMessage` | Friendly text, safe to show. |
| `debugMessage` | Full technical detail for logs. |

| Code | Meaning |
| --- | --- |
| `CANCELLED` | The person left the flow. |
| `CAMERA_PERMISSION_DENIED` | Camera permission declined. |
| `INVALID_ARGUMENTS` | Missing or blank `sessionId`. |
| `SESSION_NOT_USABLE` | Pre-flight check: session already used or expired. |
| `STATUS_CHECK_FAILED` | Pre-flight check could not reach the gateway. |
| `CONFIG_FAILED` | AWS Amplify could not be configured. |
| `DETECTOR_FAILED` | The AWS detector failed (network, timeout, camera, service). |

Wrappers may add: `NO_ACTIVITY`, `NO_VIEW_CONTROLLER`, `IN_PROGRESS`.

## Versions

| Native SDK | AWS Amplify liveness | Flutter | React Native |
| --- | --- | --- | --- |
| 1.9.0 | iOS 1.6.0 / Android 1.13.0 | — | 0.1.0 |
| 1.8.1 | iOS 1.4.2 / Android 1.6.0 | — | — |
| 1.7.x | iOS 1.4.2 / Android 1.6.0 | 0.3.0 | — |
