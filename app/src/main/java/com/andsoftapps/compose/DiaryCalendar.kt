package com.andsoftapps.compose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.andsoftapps.navigation.Route
import com.andsoftapps.utils.ValueHolder
import com.andsoftapps.utils.YEAR_MONTH_FORMATED_STRING
import com.andsoftapps.utils.firstDayOfWeek
import com.andsoftapps.utils.plus
import com.andsoftapps.utils.totalDaysInMonthPlusLeftOver
import com.andsoftapps.viewmodel.DiaryCalendarViewModel
import java.time.DayOfWeek
import java.time.YearMonth

private val SCREEN_SLIDE_ANIM_DURATION_MILLIS = 1000
private val BOTTOM_SHEET_HEIGHT = 130.dp
private val DAY_BOX_HEIGHT = 70.dp
private val DAY_BOX_BODRDER = 0.1.dp

private val TAG = "DiaryCalendarScreen"

@Composable
fun DiaryCalendarScreen(viewModel: DiaryCalendarViewModel = hiltViewModel()) {

    val uiState by viewModel.uiState.collectAsState()

    val navController = rememberNavController()

    NavigationLocalProvider(navController = navController) {
        NavHost(
            navController = navController,
            startDestination = Route.Home.route
        ) {

            composable(Route.Home.route) { from ->

                DiaryCalendar(month = { uiState.currentYearMonth },
                    monthChangeCallback = viewModel::setCurrentYearMonth)

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
            DiaryCalendarBottomSheetNavigation(month, monthChangeCallback)
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

                    //these values are derived from YearMonth and won't change as long
                    // as the YearMonth doesn't change, so they need not be MutableState
                    val firstDayOfWeek = remember(monthTarget) {
                        monthTarget.firstDayOfWeek }
                    val totalDaysInMonthPlusLeftOver = remember(monthTarget) {
                        monthTarget.totalDaysInMonthPlusLeftOver
                    }

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

                        DiaryCalendarContent(monthTarget, firstDayOfWeek,
                            totalDaysInMonthPlusLeftOver)

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


fun LazyGridScope.DiaryCalendarContent(month: YearMonth, firstDayOfWeek: Int, totalDaysInMonth: Int) {
    //header
    item(span = {  GridItemSpan(maxLineSpan) }) {
        Text(text = month.format(YEAR_MONTH_FORMATED_STRING),
            color = Color.White,
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            modifier = Modifier.background(color = Color.Black)
            )
    }

    items(7) {
        index ->
        Text(text = DayOfWeek.of(
            when (index) {
                0 -> 7
                else -> index }).name[0].toString(),
            color = Color.White,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            modifier = Modifier.background(color = Color.Black)
        )
    }

    items(totalDaysInMonth,
        key = { index ->
            "${month.year}-${index}"
        }) { index ->
        val dayInMonth = if (index >= firstDayOfWeek) { index - firstDayOfWeek + 1 }
                            else null

        DayBox(dayInMonth, month)

    }
}

@Composable
fun DayBox(dayInMonth: Int?, month: YearMonth) {

    var expanded by remember { mutableStateOf(false) }
    val dayImage = LocalDiaryEntities.current[dayInMonth]?.diaryCalendarEntity?.imagePath
    val context = LocalContext.current
    val saveImgCallBack = LocalSaveImgPath.current
    var launchResultUri by remember { mutableStateOf<Uri?>( dayImage?.let { Uri.parse(it) }) }
    val getContentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result?.let { nonNullableResult ->
                when (nonNullableResult.resultCode == Activity.RESULT_OK) {
                    true -> {
                        val intent = nonNullableResult.data
                        launchResultUri = intent?.data
                        launchResultUri?.apply {
                            context.getContentResolver()
                                .takePersistableUriPermission(launchResultUri!!,
                                    // flags
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )

                            saveImgCallBack(month, dayInMonth!!, launchResultUri)
                        }
                    }
                    else -> {
                        Log.d(TAG, "GetContent URI returns false")
                    }
                }
            }
        }

    Card(
        shape = RoundedCornerShape(3.dp),
        modifier = Modifier.fillMaxWidth().height(DAY_BOX_HEIGHT)
            .clickable {
                if (dayInMonth != null) {
                    expanded = true
                }
            },
        border = BorderStroke(DAY_BOX_BODRDER, Color.Black)
    ) {
        Box {
            if (dayInMonth != null && launchResultUri != null) {
                AsyncImage(
                    model = launchResultUri,
                    contentDescription = "Image from photo picker",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp, 200.dp)
                    //.clip(CircleShape)
                )
            }

            if (dayInMonth != null) {
                DayIcon(Modifier.align(Alignment.TopStart), dayInMonth.toString())
            }

        }
    }

}

@Composable
fun BoxScope.CalendarIconBackground(modifier: Modifier = Modifier,
                                    brush: Brush,
                                    content: @Composable BoxScope.() -> Unit) {
    Box (modifier = modifier
        .clip(CircleShape)
        .background(
            brush = brush),
        content = content
    )
}


@Composable
fun BoxScope.DayIcon(modifier: Modifier = Modifier, text: String) {
    CalendarIconBackground(modifier = modifier,
        brush = Brush.horizontalGradient(listOf(
            Color.Black,
            Color.DarkGray))) {
        Text("00", Modifier.align(Alignment.Center), color = Color.White.copy(alpha = 0f))
        Text(text,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center))
    }
}


@Composable
fun DiaryCalendarBottomSheetNavigation(monthLambda: () -> YearMonth,
                                       monthChangeCallback: ((YearMonth) -> Unit)?) {

    val modifier = Modifier.fillMaxWidth().height(BOTTOM_SHEET_HEIGHT)

    Box(modifier.background(color = Color.Green))

}