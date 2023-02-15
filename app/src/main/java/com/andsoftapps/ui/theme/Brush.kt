package com.andsoftapps.ui.theme

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