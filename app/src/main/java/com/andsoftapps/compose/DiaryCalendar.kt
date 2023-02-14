package com.andsoftapps.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.andsoftapps.navigation.Route
import com.andsoftapps.utils.ValueHolder
import com.andsoftapps.utils.YEAR_MONTH_FORMATED_STRING
import com.andsoftapps.utils.plus
import com.andsoftapps.viewmodel.DiaryCalendarViewModel
import java.time.YearMonth

private val SCREEN_SLIDE_ANIM_DURATION_MILLIS = 1000

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

                DiaryCalendar(month = { uiState.value.currentYearMonth },
                    monthChangeCallback = viewModel::setCurrentYearMonth
                    )

            }

        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiaryCalendar(month: () -> YearMonth, monthChangeCallback: (YearMonth) -> Unit) {
    //this article explains why passing lambda and not actual value improve performance
    // https://developer.android.com/jetpack/compose/performance/bestpractices#defer-reads

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
        DiaryCalendarLayout(month, monthChangeCallback)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DiaryCalendarLayout(monthLambda: () -> YearMonth, monthChangeCallback: ((YearMonth) -> Unit)? = null) {

    val lazyVerticalGridState = rememberLazyGridState()

    val month = monthLambda()

    //don't need to use mutableStateOf because we don't need this to
    // cause re-compose, we just use this to remember previous value
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

    //animate sliding left/right
    AnimatedContent(
        targetState = month,
        transitionSpec = {
            fadeIn() + slideInHorizontally(
                animationSpec = tween(SCREEN_SLIDE_ANIM_DURATION_MILLIS),
                initialOffsetX = { fullWidth -> slideDirection.value!! * fullWidth }) with
                    slideOutHorizontally (animationSpec = tween(SCREEN_SLIDE_ANIM_DURATION_MILLIS),
                        targetOffsetX = { fullWidth -> -1 * slideDirection.value!! * fullWidth })
        }
        ) { monthTarget ->

        val mapMonthEntities by LocalQueryDairyEntries.current(monthTarget).collectAsState(emptyMap())

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
                            monthChangeCallback?.invoke(monthTarget + dragDirection)
                            dragDirection = 0
                        }
                    }

                )
            }) {

                Row(Modifier.weight(1f, true)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        horizontalArrangement = Arrangement.spacedBy(1.dp),
                        state = lazyVerticalGridState,
//                        contentPadding = PaddingValues(
//                            start = 3.dp,
//                            end = 3.dp
//                        ),

                        ) {

                        DiaryCalendarContent(monthTarget)

                    }
                }

                Row {
                    Box(
                        Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                            .background(color = Color.DarkGray)
                    )
                }

            }
        }
    }

}

fun LazyGridScope.DiaryCalendarContent(month: YearMonth) {

    //header
    item(span = {  GridItemSpan(maxLineSpan) }) {
        Text(text = month.format(YEAR_MONTH_FORMATED_STRING),
            color = Color.White,
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            modifier = Modifier.background(color = Color.Black)
            )
    }

}