package com.matedroid.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom icons not available in Material Icons Extended.
 * These are converted from Material Symbols (Google Fonts).
 */
object CustomIcons {
    /**
     * Road icon from Material Symbols Outlined.
     * Source: https://fonts.google.com/icons?icon.query=road
     */
    val Road: ImageVector by lazy {
        ImageVector.Builder(
            name = "Road",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                // Original SVG path with Y-axis transformation (viewBox was "0 -960 960 960")
                // M160-160v-640h80v640h-80Z -> left lane line
                moveTo(160f, 800f)
                verticalLineToRelative(-640f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(640f)
                close()

                // m280 0v-160h80v160h-80Z -> bottom center dashed line
                moveTo(440f, 800f)
                verticalLineToRelative(-160f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(160f)
                close()

                // m280 0v-640h80v640h-80Z -> right lane line
                moveTo(720f, 800f)
                verticalLineToRelative(-640f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(640f)
                close()

                // M440-400v-160h80v160h-80Z -> middle center dashed line
                moveTo(440f, 560f)
                verticalLineToRelative(-160f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(160f)
                close()

                // m0-240v-160h80v160h-80Z -> top center dashed line
                moveTo(440f, 320f)
                verticalLineToRelative(-160f)
                horizontalLineToRelative(80f)
                verticalLineToRelative(160f)
                close()
            }
        }.build()
    }
}
