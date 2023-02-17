package com.andsoftapps.compose

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

//https://stackoverflow.com/questions/69230049/how-to-force-orientation-for-some-screens-in-jetpack-compose
@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            println("LockScreenOrientation DisposableEffect onDispose")
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InstallBackPressHandler(backPressHandlerCallback: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BackHandler {
        focusManager.clearFocus()

        val insets = ViewCompat.getRootWindowInsets(view)
        val imeVisible = insets?.isVisible(WindowInsetsCompat.Type.ime())

        if (imeVisible == true) {
            keyboardController?.hide()
        } else {
            backPressHandlerCallback()
        }
    }
}

@Composable
fun KeyboardShownListener(callbackWhenShown: (() -> Unit)? = null) {
    //https://stackoverflow.com/questions/68847559/how-can-i-detect-keyboard-opening-and-closing-in-jetpack-compose

    val view = LocalView.current
    val focusManager = LocalFocusManager.current

    var previousIsOpenKeyboard by remember {
        val insets: WindowInsetsCompat? = ViewCompat.getRootWindowInsets(view)
        val imeVisible = insets?.isVisible(WindowInsetsCompat.Type.ime())

        mutableStateOf(imeVisible ?: false)
    }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime())
                ?: false

            if (!previousIsOpenKeyboard && isKeyboardOpen) {
                callbackWhenShown?.invoke()
            } else if (previousIsOpenKeyboard && !isKeyboardOpen) {
                focusManager.clearFocus(true)
            }
            previousIsOpenKeyboard = isKeyboardOpen
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)

        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
}