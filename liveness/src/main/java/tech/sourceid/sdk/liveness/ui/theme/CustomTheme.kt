package tech.sourceid.sdk.liveness.ui.theme

import android.content.Context
import android.util.Log
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.amplifyframework.ui.liveness.ui.LivenessColorScheme
import tech.sourceid.sdk.liveness.data.LivenessFonts
import tech.sourceid.sdk.liveness.data.LivenessUIConfig
import tech.sourceid.sdk.liveness.data.parseHexColor

/* Resolves a LivenessUIConfig into concrete colours and fonts. Defaults match
   the iOS SDK so both platforms look the same out of the box (CONTRACT.md). */

private const val TAG = "LivenessSDK"

/** Primary: configured, else black (light) / white (dark). */
val LivenessUIConfig.primaryColor: Color
    get() = getPrimaryColor() ?: if (isDark) Color.White else Color.Black

/** Text and icons on [primaryColor]. The dark theme's default primary is white, so its default text is dark. */
val LivenessUIConfig.onPrimaryColor: Color
    get() = parseHexColor(colors.onPrimary)
        ?: if (isDark && getPrimaryColor() == null) Color.Black else Color.White

/** Screen background around the camera, start page, title bar and footer. */
val LivenessUIConfig.backgroundColor: Color
    get() = parseHexColor(colors.background) ?: if (isDark) Color.Black else Color.White

/** Text on [backgroundColor]. */
val LivenessUIConfig.textColor: Color
    get() = parseHexColor(colors.onBackground) ?: if (isDark) Color.White else Color(0xFF111111)

/** Start-page guideline card. */
val LivenessUIConfig.cardColor: Color
    get() = parseHexColor(colors.surface)
        ?: if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFF5F3FA)

/**
 * Material colour scheme shared by the SDK's own screens and the AWS detector:
 * AWS's light or dark scheme (by `theme`) with every configured colour applied.
 */
@Composable
fun buildColorScheme(config: LivenessUIConfig): ColorScheme {
    val base = if (config.isDark) {
        LivenessColorScheme.Defaults.darkColorScheme
    } else {
        LivenessColorScheme.Defaults.lightColorScheme
    }
    val c = config.colors
    return base.copy(
        primary = config.primaryColor,
        onPrimary = config.onPrimaryColor,
        background = config.backgroundColor,
        onBackground = config.textColor,
        surface = parseHexColor(c.surface) ?: base.surface,
        error = parseHexColor(c.error) ?: base.error,
        onError = parseHexColor(c.onError) ?: base.onError,
        errorContainer = parseHexColor(c.errorContainer) ?: base.errorContainer,
        onErrorContainer = parseHexColor(c.onErrorContainer) ?: base.onErrorContainer
    )
}

/**
 * [base] typography with the configured font family applied to every style.
 * The family holds the regular, medium and bold fonts as weights 400, 500 and
 * 700, so each Material style picks the closest weight.
 */
@Composable
fun buildTypography(config: LivenessUIConfig, base: Typography): Typography {
    val context = LocalContext.current
    val family = remember(config.fonts) { buildFontFamily(context, config.fonts) } ?: return base
    fun TextStyle.withFamily() = copy(fontFamily = family)
    return base.copy(
        displayLarge = base.displayLarge.withFamily(),
        displayMedium = base.displayMedium.withFamily(),
        displaySmall = base.displaySmall.withFamily(),
        headlineLarge = base.headlineLarge.withFamily(),
        headlineMedium = base.headlineMedium.withFamily(),
        headlineSmall = base.headlineSmall.withFamily(),
        titleLarge = base.titleLarge.withFamily(),
        titleMedium = base.titleMedium.withFamily(),
        titleSmall = base.titleSmall.withFamily(),
        bodyLarge = base.bodyLarge.withFamily(),
        bodyMedium = base.bodyMedium.withFamily(),
        bodySmall = base.bodySmall.withFamily(),
        labelLarge = base.labelLarge.withFamily(),
        labelMedium = base.labelMedium.withFamily(),
        labelSmall = base.labelSmall.withFamily()
    )
}

internal fun buildFontFamily(context: Context, fonts: LivenessFonts): FontFamily? {
    if (fonts.isEmpty) return null
    val entries = listOfNotNull(
        resolveFont(context, fonts.regular, FontWeight.Normal),
        resolveFont(context, fonts.medium ?: fonts.regular, FontWeight.Medium),
        resolveFont(context, fonts.bold ?: fonts.medium ?: fonts.regular, FontWeight.Bold)
    )
    return if (entries.isEmpty()) null else FontFamily(entries)
}

/** A font from `assets/fonts/<name>.ttf|.otf`, else from `res/font/<name>`; null when not found. */
private fun resolveFont(context: Context, name: String?, weight: FontWeight): Font? {
    if (name.isNullOrBlank()) return null
    for (extension in listOf("ttf", "otf")) {
        val path = "fonts/$name.$extension"
        val exists = try {
            context.assets.open(path).close()
            true
        } catch (_: Exception) {
            false
        }
        if (exists) return Font(path, context.assets, weight)
    }
    val resourceId = context.resources.getIdentifier(name, "font", context.packageName)
    if (resourceId != 0) return Font(resourceId, weight)
    Log.w(TAG, "Font \"$name\" not found in assets/fonts or res/font; using the system font")
    return null
}
