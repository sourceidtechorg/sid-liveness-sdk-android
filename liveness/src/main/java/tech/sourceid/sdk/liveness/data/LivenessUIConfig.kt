package tech.sourceid.sdk.liveness.data

import androidx.compose.ui.graphics.Color
import java.io.Serializable

/**
 * Look and behaviour of the liveness flow. Field names and defaults are
 * shared with the iOS SDK; see CONTRACT.md.
 *
 * @property hideBranding hides the "Powered by SourceID" footer.
 * @property customTitle title above the camera and on the start page; hidden above the camera when null.
 * @property theme `"light"` or `"dark"`: the base palette every colour falls back to.
 * @property primaryColorHex shorthand for [LivenessColors.primary]; `colors.primary` wins when both are set.
 * @property showStartPage shows the SourceID instruction page before the camera. Set `false` when the
 *   host app provides its own intro.
 * @property hideCancelButton hides AWS's close button during capture. Only hide it when the host gives
 *   the person another way out.
 * @property colors full palette; each colour falls back to the [theme] default.
 * @property fonts custom fonts; each falls back to the system font.
 */
data class LivenessUIConfig(
    val hideBranding: Boolean = false,
    val customTitle: String? = null,
    val theme: String = "light",   // "light" | "dark"
    val primaryColorHex: String? = null,
    val showStartPage: Boolean = true,
    val hideCancelButton: Boolean = false,
    val colors: LivenessColors = LivenessColors(),
    val fonts: LivenessFonts = LivenessFonts()
) : Serializable {

    val isDark: Boolean get() = theme.equals("dark", ignoreCase = true)

    /** The configured primary colour, or null to use the theme default. */
    fun getPrimaryColor(): Color? = parseHexColor(colors.primary ?: primaryColorHex)
}

/**
 * Optional `"#RRGGBB"` colours. Names follow the Material / AWS Amplify roles,
 * so they map one-to-one onto the AWS detector theme on both platforms.
 *
 * @property primary buttons, accents, start-page icon, title.
 * @property onPrimary text and icons on [primary].
 * @property background screen background around the camera, start page, title bar, footer.
 * @property onBackground text on [background], including capture guidance.
 * @property surface start-page guideline card; AWS surfaces.
 */
data class LivenessColors(
    val primary: String? = null,
    val onPrimary: String? = null,
    val background: String? = null,
    val onBackground: String? = null,
    val surface: String? = null,
    val error: String? = null,
    val onError: String? = null,
    val errorContainer: String? = null,
    val onErrorContainer: String? = null
) : Serializable

/**
 * Custom fonts by name. Each name is the file name, without extension, of a
 * font in the app's `assets/fonts/` (`.ttf` or `.otf`, the React Native / Expo
 * convention), or the name of a `res/font` resource. Unknown names fall back to
 * the system font.
 *
 * @property regular body, captions, guidance.
 * @property medium headlines, labels, buttons; falls back to [regular].
 * @property bold titles; falls back to [medium], then [regular].
 */
data class LivenessFonts(
    val regular: String? = null,
    val medium: String? = null,
    val bold: String? = null
) : Serializable {
    val isEmpty: Boolean get() = regular == null && medium == null && bold == null
}

private val HEX_COLOR = Regex("^#?([0-9A-Fa-f]{6})$")

/** `"#RRGGBB"` (the `#` is optional) to an opaque colour; null for null or invalid input. */
internal fun parseHexColor(hex: String?): Color? {
    val digits = hex?.trim()?.let { HEX_COLOR.matchEntire(it)?.groupValues?.get(1) } ?: return null
    return Color(0xFF000000 or digits.toLong(16))
}
