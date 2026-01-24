package com.matedroid.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CarColorPalette(
    val surface: Color,
    val accent: Color,
    val accentDim: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val progressTrack: Color
)

// Default palette (used when no car color is available) - uses white car palette
val DefaultLightPalette = CarColorPalette(
    surface = Color(0xFFF5F3F0),
    accent = Color(0xFF8B7355),
    accentDim = Color(0xFF8B7355).copy(alpha = 0.3f),
    onSurface = Color(0xFF2A2520),
    onSurfaceVariant = Color(0xFF2A2520).copy(alpha = 0.7f),
    progressTrack = Color(0xFF2A2520).copy(alpha = 0.1f)
)

val DefaultDarkPalette = CarColorPalette(
    surface = Color(0xFF1E2530),
    accent = Color(0xFF8BAEE8),
    accentDim = Color(0xFF8BAEE8).copy(alpha = 0.3f),
    onSurface = Color(0xFFE8EEF8),
    onSurfaceVariant = Color(0xFFE8EEF8).copy(alpha = 0.7f),
    progressTrack = Color(0xFFE8EEF8).copy(alpha = 0.1f)
)

// Car-specific palettes
object CarColorPalettes {

    // White car - warm grey (same as old black, works well with white car)
    private val whiteLightPalette = CarColorPalette(
        surface = Color(0xFFF5F3F0),
        accent = Color(0xFF8B7355),
        accentDim = Color(0xFF8B7355).copy(alpha = 0.3f),
        onSurface = Color(0xFF2A2520),
        onSurfaceVariant = Color(0xFF2A2520).copy(alpha = 0.7f),
        progressTrack = Color(0xFF2A2520).copy(alpha = 0.1f)
    )

    private val whiteDarkPalette = CarColorPalette(
        surface = Color(0xFF1E2530),
        accent = Color(0xFF8BAEE8),
        accentDim = Color(0xFF8BAEE8).copy(alpha = 0.3f),
        onSurface = Color(0xFFE8EEF8),
        onSurfaceVariant = Color(0xFFE8EEF8).copy(alpha = 0.7f),
        progressTrack = Color(0xFFE8EEF8).copy(alpha = 0.1f)
    )

    // Black car - darker grey (darker than midnight silver)
    private val blackLightPalette = CarColorPalette(
        surface = Color(0xFFD8DADC),
        accent = Color(0xFF505458),
        accentDim = Color(0xFF505458).copy(alpha = 0.3f),
        onSurface = Color(0xFF1E2022),
        onSurfaceVariant = Color(0xFF1E2022).copy(alpha = 0.7f),
        progressTrack = Color(0xFF1E2022).copy(alpha = 0.1f)
    )

    private val blackDarkPalette = CarColorPalette(
        surface = Color(0xFF2A2520),
        accent = Color(0xFFC9A66B),
        accentDim = Color(0xFFC9A66B).copy(alpha = 0.3f),
        onSurface = Color(0xFFF5F3F0),
        onSurfaceVariant = Color(0xFFF5F3F0).copy(alpha = 0.7f),
        progressTrack = Color(0xFFF5F3F0).copy(alpha = 0.1f)
    )

    // Midnight Silver - cool grey
    private val midnightSilverLightPalette = CarColorPalette(
        surface = Color(0xFFECEEF0),
        accent = Color(0xFF6B7A8C),
        accentDim = Color(0xFF6B7A8C).copy(alpha = 0.3f),
        onSurface = Color(0xFF22262B),
        onSurfaceVariant = Color(0xFF22262B).copy(alpha = 0.7f),
        progressTrack = Color(0xFF22262B).copy(alpha = 0.1f)
    )

    private val midnightSilverDarkPalette = CarColorPalette(
        surface = Color(0xFF22262B),
        accent = Color(0xFF8FA4B8),
        accentDim = Color(0xFF8FA4B8).copy(alpha = 0.3f),
        onSurface = Color(0xFFECEEF0),
        onSurfaceVariant = Color(0xFFECEEF0).copy(alpha = 0.7f),
        progressTrack = Color(0xFFECEEF0).copy(alpha = 0.1f)
    )

    // Deep Blue
    private val deepBlueLightPalette = CarColorPalette(
        surface = Color(0xFFE5EBF5),
        accent = Color(0xFF3B5998),
        accentDim = Color(0xFF3B5998).copy(alpha = 0.3f),
        onSurface = Color(0xFF1A2235),
        onSurfaceVariant = Color(0xFF1A2235).copy(alpha = 0.7f),
        progressTrack = Color(0xFF1A2235).copy(alpha = 0.1f)
    )

    private val deepBlueDarkPalette = CarColorPalette(
        surface = Color(0xFF1A2235),
        accent = Color(0xFF6B8BC3),
        accentDim = Color(0xFF6B8BC3).copy(alpha = 0.3f),
        onSurface = Color(0xFFE5EBF5),
        onSurfaceVariant = Color(0xFFE5EBF5).copy(alpha = 0.7f),
        progressTrack = Color(0xFFE5EBF5).copy(alpha = 0.1f)
    )

    // Red Multi-Coat
    private val redLightPalette = CarColorPalette(
        surface = Color(0xFFF8E8E8),
        accent = Color(0xFFC45050),
        accentDim = Color(0xFFC45050).copy(alpha = 0.3f),
        onSurface = Color(0xFF2E1A1A),
        onSurfaceVariant = Color(0xFF2E1A1A).copy(alpha = 0.7f),
        progressTrack = Color(0xFF2E1A1A).copy(alpha = 0.1f)
    )

    private val redDarkPalette = CarColorPalette(
        surface = Color(0xFF2E1A1A),
        accent = Color(0xFFE07070),
        accentDim = Color(0xFFE07070).copy(alpha = 0.3f),
        onSurface = Color(0xFFF8E8E8),
        onSurfaceVariant = Color(0xFFF8E8E8).copy(alpha = 0.7f),
        progressTrack = Color(0xFFF8E8E8).copy(alpha = 0.1f)
    )

    // Quicksilver - warm silver
    private val quicksilverLightPalette = CarColorPalette(
        surface = Color(0xFFF0EDE8),
        accent = Color(0xFFA09080),
        accentDim = Color(0xFFA09080).copy(alpha = 0.3f),
        onSurface = Color(0xFF252320),
        onSurfaceVariant = Color(0xFF252320).copy(alpha = 0.7f),
        progressTrack = Color(0xFF252320).copy(alpha = 0.1f)
    )

    private val quicksilverDarkPalette = CarColorPalette(
        surface = Color(0xFF252320),
        accent = Color(0xFFB0A090),
        accentDim = Color(0xFFB0A090).copy(alpha = 0.3f),
        onSurface = Color(0xFFF0EDE8),
        onSurfaceVariant = Color(0xFFF0EDE8).copy(alpha = 0.7f),
        progressTrack = Color(0xFFF0EDE8).copy(alpha = 0.1f)
    )

    // Stealth Grey - cool grey
    private val stealthGreyLightPalette = CarColorPalette(
        surface = Color(0xFFECEDEE),
        accent = Color(0xFF606570),
        accentDim = Color(0xFF606570).copy(alpha = 0.3f),
        onSurface = Color(0xFF1E2022),
        onSurfaceVariant = Color(0xFF1E2022).copy(alpha = 0.7f),
        progressTrack = Color(0xFF1E2022).copy(alpha = 0.1f)
    )

    private val stealthGreyDarkPalette = CarColorPalette(
        surface = Color(0xFF1E2022),
        accent = Color(0xFF909598),
        accentDim = Color(0xFF909598).copy(alpha = 0.3f),
        onSurface = Color(0xFFECEDEE),
        onSurfaceVariant = Color(0xFFECEDEE).copy(alpha = 0.7f),
        progressTrack = Color(0xFFECEDEE).copy(alpha = 0.1f)
    )

    // Ultra Red - vibrant
    private val ultraRedLightPalette = CarColorPalette(
        surface = Color(0xFFFAEBEB),
        accent = Color(0xFFE03030),
        accentDim = Color(0xFFE03030).copy(alpha = 0.3f),
        onSurface = Color(0xFF301818),
        onSurfaceVariant = Color(0xFF301818).copy(alpha = 0.7f),
        progressTrack = Color(0xFF301818).copy(alpha = 0.1f)
    )

    private val ultraRedDarkPalette = CarColorPalette(
        surface = Color(0xFF301818),
        accent = Color(0xFFFF5050),
        accentDim = Color(0xFFFF5050).copy(alpha = 0.3f),
        onSurface = Color(0xFFFAEBEB),
        onSurfaceVariant = Color(0xFFFAEBEB).copy(alpha = 0.7f),
        progressTrack = Color(0xFFFAEBEB).copy(alpha = 0.1f)
    )

    // Midnight Cherry Red - deep sophisticated red
    private val midnightCherryLightPalette = CarColorPalette(
        surface = Color(0xFFF5E5E8),
        accent = Color(0xFF8B3040),
        accentDim = Color(0xFF8B3040).copy(alpha = 0.3f),
        onSurface = Color(0xFF251518),
        onSurfaceVariant = Color(0xFF251518).copy(alpha = 0.7f),
        progressTrack = Color(0xFF251518).copy(alpha = 0.1f)
    )

    private val midnightCherryDarkPalette = CarColorPalette(
        surface = Color(0xFF251518),
        accent = Color(0xFFC05068),
        accentDim = Color(0xFFC05068).copy(alpha = 0.3f),
        onSurface = Color(0xFFF5E5E8),
        onSurfaceVariant = Color(0xFFF5E5E8).copy(alpha = 0.7f),
        progressTrack = Color(0xFFF5E5E8).copy(alpha = 0.1f)
    )

    fun forExteriorColor(exteriorColor: String?, darkTheme: Boolean): CarColorPalette {
        val colorKey = exteriorColor?.lowercase()?.replace(" ", "") ?: ""

        return when {
            colorKey.contains("white") || colorKey == "ppsw" ->
                if (darkTheme) whiteDarkPalette else whiteLightPalette

            colorKey.contains("black") || colorKey == "pbsb" || colorKey == "pmbl" ->
                if (darkTheme) blackDarkPalette else blackLightPalette

            colorKey.contains("midnightsilver") || colorKey.contains("steelgrey") || colorKey == "pmng" ->
                if (darkTheme) midnightSilverDarkPalette else midnightSilverLightPalette

            colorKey.contains("silver") || colorKey == "pmss" ->
                if (darkTheme) midnightSilverDarkPalette else midnightSilverLightPalette

            colorKey.contains("deepblue") || colorKey == "ppsb" ->
                if (darkTheme) deepBlueDarkPalette else deepBlueLightPalette

            colorKey.contains("quicksilver") || colorKey == "pn00" ->
                if (darkTheme) quicksilverDarkPalette else quicksilverLightPalette

            colorKey.contains("stealthgrey") || colorKey.contains("stealth") || colorKey == "pn01" ->
                if (darkTheme) stealthGreyDarkPalette else stealthGreyLightPalette

            colorKey.contains("midnightcherry") || colorKey == "pr00" ->
                if (darkTheme) midnightCherryDarkPalette else midnightCherryLightPalette

            colorKey.contains("ultrared") || colorKey == "pr01" ->
                if (darkTheme) ultraRedDarkPalette else ultraRedDarkPalette

            colorKey.contains("red") || colorKey == "ppmr" ->
                if (darkTheme) redDarkPalette else redLightPalette

            else -> if (darkTheme) DefaultDarkPalette else DefaultLightPalette
        }
    }
}

val LocalCarColorPalette = staticCompositionLocalOf { DefaultLightPalette }
