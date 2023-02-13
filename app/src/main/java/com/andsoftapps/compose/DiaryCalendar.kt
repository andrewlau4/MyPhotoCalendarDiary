package com.andsoftapps.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andsoftapps.navigation.Route
import com.andsoftapps.utils.ValueHolder
import com.andsoftapps.utils.plus
import com.andsoftapps.viewmodel.DiaryCalendarViewModel
import java.time.YearMonth

private val SCREEN_SLIDE_DURATION_MILLIS = 1000

@Composable
fun DiaryCalendarScreen(viewModel: DiaryCalendarViewModel = hiltViewModel()) {

    val uiState = viewModel.uiState.collectAsState()

    val navController = rememberNavController()

    NavigationLocalProvider(navController = navController) {
        NavHost(
            navController = navController,
            startDestination = Route.Home.route
        ) {

            composable(Route.Home.route) { from ->

                DiaryCalendar()

            }

        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiaryCalendar() {

    LocalHideShowActionBar.current(false)

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(
            initialValue = BottomSheetValue.Collapsed)
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 50.dp,
        sheetContent = {

        }
    ) { innerPadding ->

    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DiaryCalendarLayout(month: YearMonth, monthChangeCallback: ((YearMonth) -> Unit)? = null) {

    val lazyVerticalGridState = rememberLazyGridState()

    val previousMonthHolder = remember { ValueHolder<YearMonth>(month) }
    //slide the screen left or right depends on user's gesture
    val slideDirection = remember { ValueHolder<Int?>(null) }

    // if previousMonthHolder equals this month, this is likely a re-compose due to
    // animation or other reasons, so don't change the slideDirection
    if (slideDirection.value == null || previousMonthHolder.value !== month) {
        slideDirection.value = if (previousMonthHolder.value!! <= month) 1 else -1
        previousMonthHolder.value = month
    }

    //direction of user's gesture
    var dragDirection by remember { mutableStateOf(0) }

    //animate slide left/right
    AnimatedContent(
        targetState = month,
        transitionSpec = {
            fadeIn() + slideInHorizontally(
                animationSpec = tween(SCREEN_SLIDE_DURATION_MILLIS),
                initialOffsetX = { fullWidth -> slideDirection.value!! * fullWidth }) with
                    slideOutHorizontally (animationSpec = tween(SCREEN_SLIDE_DURATION_MILLIS),
                        targetOffsetX = { fullWidth -> -1 * slideDirection.value!! * fullWidth })
        }
        ) { month ->

        val mapMonthEntities by LocalQueryDairyEntries.current(month).collectAsState(emptyMap())

        CompositionLocalProvider(LocalDiaryEntities provides mapMonthEntities) {
            Column(modifier = Modifier.pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change: PointerInputChange, dragAmount: Offset ->
                        val (x, _) = dragAmount

                        dragDirection = if (x > 0) -1 else if (x < 0) 1 else 0

                        change.consume()
                    },

                    onDragEnd = {
                        if (dragDirection != 0) {
                            monthChangeCallback?.invoke(month + dragDirection)
                            dragDirection = 0
                        }
                    }

                )
            }) {

            }
        }
    }

}