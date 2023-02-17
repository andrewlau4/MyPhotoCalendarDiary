package com.andsoftapps.ui.theme

import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val NavigationButtonBrush = Brush.linearGradient(
    colorStops = arrayOf(
        0f to Color(0xFF3399CC),
        0.5f to Color(0xFF3700B3),
        1f to Color(0xFFFFFFFF)
    ),
    start = Offset(
        0f,
        Offset.Infinite.y
    ),
    end = Offset(
        Offset.Infinite.x,
        0f
    )
)

val RainbowColorsBrush = Brush.sweepGradient(
        listOf(
            Color(0xFF9575CD),
            Color(0xFFBA68C8),
            Color(0xFFE57373),
            Color(0xFFFFB74D),
            Color(0xFFFFF176),
            Color(0xFFAED581),
            Color(0xFF4DD0E1),
            Color(0xFF9575CD)
        )
    )