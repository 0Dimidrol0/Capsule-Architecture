package io.github.dimidrol.capsule.debug.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color

internal data class CapsuleDebugColors(
    val isDark: Boolean,
    val surface: Int,
    val text: Int,
    val muted: Int,
    val accent: Int,
    val action: Int,
    val divider: Int
) {
    companion object {
        fun from(context: Context): CapsuleDebugColors {
            val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return if (mode == Configuration.UI_MODE_NIGHT_YES) {
                CapsuleDebugColors(
                    isDark = true,
                    surface = Color.rgb(24, 28, 33),
                    text = Color.rgb(245, 241, 232),
                    muted = Color.rgb(170, 178, 186),
                    accent = Color.rgb(255, 121, 88),
                    action = Color.rgb(42, 72, 86),
                    divider = Color.rgb(57, 65, 73)
                )
            } else {
                CapsuleDebugColors(
                    isDark = false,
                    surface = Color.rgb(248, 244, 236),
                    text = Color.rgb(31, 38, 46),
                    muted = Color.rgb(100, 108, 116),
                    accent = Color.rgb(218, 75, 43),
                    action = Color.rgb(31, 56, 70),
                    divider = Color.rgb(218, 213, 203)
                )
            }
        }
    }
}
