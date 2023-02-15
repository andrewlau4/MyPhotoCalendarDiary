package com.andsoftapps.compose

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

fun Modifier.bounceClick(onClick: () -> Unit) = composed {
    val interactionSrc = remember { MutableInteractionSource() }
    val isPressed by interactionSrc.collectIsPressedAsState()

    val scale by animateFloatAsState(
        if (isPressed) 0.7f else 1.0f,
        animationSpec = tween(durationMillis = 10, easing = FastOutSlowInEasing)
    )

    graphicsLayer {
        println("bounceClickRevised scale  $scale")
        scaleX = scale
        scaleY = scale
    }
    .clickable(
        interactionSource = interactionSrc,
        indication = null,
        onClick = onClick
    )
}


@Composable
fun PulsateButton(onClick: () -> Unit, brush: Brush,
                  @DrawableRes backgroundDrawableId: Int, contentDescription: String,
                  modifier: Modifier
) {

    Box (modifier = Modifier
        .bounceClick(onClick)
        .size(30.dp)
        .clip(CircleShape)
        .background(
            brush = brush
        )
        .then(modifier)
    ) {
        Image(modifier = Modifier.align(Alignment.Center),
            contentDescription = contentDescription,
            painter = painterResource(id = backgroundDrawableId),
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
}

@Composable
fun PulsateButton(onClick: () -> Unit,
                  imageVector: ImageVector, contentDescription: String,
                  modifier: Modifier
) {

    Box (modifier = Modifier
        .bounceClick(onClick)
        .size(30.dp)
        .clip(CircleShape)
        .then(modifier)
    ) {
        Image(modifier = Modifier.align(Alignment.Center),
            contentDescription = contentDescription,
            imageVector = imageVector,
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
}