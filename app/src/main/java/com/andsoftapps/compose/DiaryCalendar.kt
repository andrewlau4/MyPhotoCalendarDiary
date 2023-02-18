package com.andsoftapps.compose

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.andsoftapps.navigation.Route
import com.andsoftapps.ui.theme.BrightRed5
import com.andsoftapps.ui.theme.DarkGreen2
import com.andsoftapps.ui.theme.LightGreen8
import com.andsoftapps.ui.theme.LightRed8
import com.andsoftapps.ui.theme.NavigationButtonBrush
import com.andsoftapps.utils.YEAR_MONTH_FORMATED_STRING
import com.andsoftapps.utils.firstDayOfWeek
import com.andsoftapps.utils.plus
import com.andsoftapps.utils.minus
import com.andsoftapps.utils.totalDaysInMonthPlusLeftOver
import com.andsoftapps.viewmodel.DiaryCalendarViewModel
import kotlinx.coroutines.launch
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

            composable(Route.Detail.route,
                arguments = listOf(navArgument("date") { type = NavType.StringType })) {
                    backStackEntry ->

                val date = backStackEntry.arguments?.getString("date")

                val (month, day) = Route.Detail.retrieveFromRoute(date!!)

                DiaryDetail(month, day)
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

    val bottomSheetExpandState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed)

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetExpandState
    )

    val coroutineScope = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 50.dp,
        sheetContent = {
            DiaryCalendarBottomSheetNavigation(
                monthLambda = month,
                monthChangeCallback = monthChangeCallback,
                isBottomSheetExpanded = { bottomSheetExpandState.isExpanded },
                toggleExpandedCallback = {
                    coroutineScope.launch {
                        if (bottomSheetExpandState.isCollapsed)
                            bottomSheetExpandState.expand()
                        else
                            bottomSheetExpandState.collapse()
                    }
                }
            )
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

    //direction of user's gesture
    var dragDirection by remember { mutableStateOf(0) }

    //animate sliding left/right
    AnimatedContent(
        targetState = month,
        transitionSpec = {
            //https://developer.android.com/jetpack/compose/animation#animatedcontent
            val slideDir = if (initialState <= targetState) 1 else -1
            fadeIn() + slideInHorizontally(
                animationSpec = tween(SCREEN_SLIDE_ANIM_DURATION_MILLIS),
                initialOffsetX = { fullWidth -> slideDir * fullWidth }) with
                    slideOutHorizontally (animationSpec = tween(SCREEN_SLIDE_ANIM_DURATION_MILLIS),
                        targetOffsetX = { fullWidth -> -1 * slideDir * fullWidth })
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
                        ) {
                            DiaryCalendarContent(
                                month = monthTarget,
                                firstDayOfWeek = firstDayOfWeek,
                                totalDaysInMonth = totalDaysInMonthPlusLeftOver,
                                ////https://developer.android.com/reference/kotlin/androidx/compose/animation/AnimatedVisibilityScope
                                isSlideAnimationRunning = { transition.isRunning }
                            )
                    }
                }

                Row {
                    Box(
                        Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                    )
                }

            }
        }
    }

}


fun LazyGridScope.DiaryCalendarContent(month: YearMonth,
                                       firstDayOfWeek: Int,
                                       totalDaysInMonth: Int,
                                       isSlideAnimationRunning: () -> Boolean
) {
    //header
    item(span = {  GridItemSpan(maxLineSpan) }) {
        Text(text = month.format(YEAR_MONTH_FORMATED_STRING),
            color = Color.White,
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
            modifier = Modifier.background(color = Color.Black)
            )
    }

    if (!isSlideAnimationRunning()) {
        items(7) { index ->
            Text(
                text = DayOfWeek.of(
                    when (index) {
                        0 -> 7
                        else -> index
                    }
                ).name[0].toString(),
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
            val dayInMonth = if (index >= firstDayOfWeek) {
                index - firstDayOfWeek + 1
            } else null

            DayBox(dayInMonth, month)
        }
    }

}

@Composable
fun DayBox(dayInMonth: Int?, month: YearMonth) {

    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(DAY_BOX_HEIGHT)
            .clickable {
                if (dayInMonth != null) {
                    expanded = true
                }
            },
        border = BorderStroke(DAY_BOX_BODRDER, Color.Black)
    ) {
        Box {

            val context = LocalContext.current
            val saveImgCallBack = LocalSaveImgPath.current

            val dayImage = LocalDiaryEntities.current[dayInMonth]?.diaryCalendarEntity?.imagePath
            var launchResultUri by remember(dayImage) { mutableStateOf<Uri?>( dayImage?.let { Uri.parse(it) }) }
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


            if (dayInMonth != null && launchResultUri != null) {
                AsyncImage(
                    model = launchResultUri,
                    contentDescription = "Image from photo picker",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp, 200.dp)
                )
            }

            if (dayInMonth != null) {
                DayIcon(Modifier.align(Alignment.TopStart), dayInMonth.toString())
            }

            val diaryEntity = LocalDiaryEntities.current[dayInMonth]
            if (diaryEntity?.diaryCalendarEntity?.userDiary != null) {
                UserDiaryHasEntryIcon(Modifier.align(Alignment.BottomStart))
            }
            if (diaryEntity?.queryResult == true) {
                QueryResultFoundIcon(Modifier.align(Alignment.BottomEnd))
            }


            dayInMonth?.apply {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    val navigationCallback = LocalNavigation.current

                    DropdownMenuItem(
                        onClick = {
                            val pickImagesIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                                    type = "image/*"
                                    putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 2) // deafult is 1
                                }
                            } else {
                                Intent(Intent.ACTION_OPEN_DOCUMENT, null).apply {
                                    type = "image/*"
                                }
                            }

                            getContentLauncher.launch(pickImagesIntent)

                            expanded = false
                        }
                    ) {
                        Icon(imageVector = Icons.Filled.Face, contentDescription = "Pick Photos Menu Item")
                        Spacer(modifier = Modifier.width(1.dp))
                        Text("Pick Photo")
                    }

                    DropdownMenuItem(onClick = {
                        navigationCallback(Route.Detail.createRoute(month, dayInMonth))
                    }) {

                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Diary")
                        Spacer(modifier = Modifier.width(1.dp))
                        Text("Diary Detail")
                    }
                }
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
            brush = brush
        ),
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

@Preview
@Composable
fun BoxScope.QueryResultFoundIcon(modifier: Modifier = Modifier) {
    CalendarIconBackground(modifier = modifier,
        brush = Brush.horizontalGradient(listOf(
            BrightRed5,
            LightRed8))) {
        Icon(
            modifier = Modifier.align(Alignment.Center),
            imageVector = Icons.Filled.Search,
            contentDescription = "Query Matched",
            tint = Color.White
        )
    }
}

@Composable
fun BoxScope.UserDiaryHasEntryIcon(modifier: Modifier = Modifier) {
    CalendarIconBackground(modifier = modifier,
        brush = Brush.horizontalGradient(listOf(
            DarkGreen2,
            LightGreen8))) {
        Icon(
            modifier = Modifier.align(Alignment.Center),
            imageVector = Icons.Outlined.Info,
            contentDescription = "User Diary Has Entry",
            tint = Color.White
        )
    }

}

@Composable
fun DiaryCalendarBottomSheetNavigation(monthLambda: () -> YearMonth,
                                       monthChangeCallback: ((YearMonth) -> Unit)? = null,
                                       isBottomSheetExpanded: (() -> Boolean)? = null,
                                       toggleExpandedCallback: (() -> Unit)? = null
) {

    ConstraintLayout(modifier = Modifier
        .fillMaxWidth()
        .height(BOTTOM_SHEET_HEIGHT)
        .clip(
            shape = RoundedCornerShape(
                topStart = 20.dp, topEnd = 20.dp,
                bottomStart = 0.dp, bottomEnd = 0.dp
            )
        )
        .background(
            color = MaterialTheme.colors.primary
        )
    ) {
        val (backButton, forwardButton, upButton, monthSelectorDropdown) = createRefs()

        val month = monthLambda()

        PulsateButton(
            onClick = {
                monthChangeCallback?.invoke(month - 1) },
            brush = NavigationButtonBrush,
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back Button",
            boxModifier = Modifier.constrainAs(backButton) {
                top.linkTo(parent.top, margin = 12.dp)
                start.linkTo(parent.absoluteLeft, margin = 12.dp)
            })

        PulsateButton(
            onClick = {
                monthChangeCallback?.invoke(month + 1) },
            brush = NavigationButtonBrush,
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Forward Button",
            boxModifier = Modifier.constrainAs(forwardButton) {
                top.linkTo(parent.top, margin = 12.dp)
                end.linkTo(parent.absoluteRight, margin = 12.dp)
            })

        val rotationZValue: Float by animateFloatAsState(targetValue =
            if (isBottomSheetExpanded?.invoke() == false) 180f else 0f )

        PulsateButton(onClick = { toggleExpandedCallback?.invoke() },
            brush = NavigationButtonBrush,
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "Up Button",
            boxModifier = Modifier.constrainAs(upButton) {
                top.linkTo(parent.top, margin = 12.dp)
                absoluteLeft.linkTo(backButton.absoluteRight, margin = 12.dp)
                absoluteRight.linkTo(forwardButton.absoluteLeft, margin = 12.dp)
            },
            imageModifier = Modifier.graphicsLayer {
                rotationZ = rotationZValue }
        )

        AndroidViewMonthSelector(month,
            modifier = Modifier.constrainAs(monthSelectorDropdown) {
                top.linkTo(upButton.bottom, margin = 1.dp)
                absoluteLeft.linkTo(parent.absoluteLeft, margin = 12.dp)
                absoluteRight.linkTo(parent.absoluteRight, margin = 12.dp)
            }
        ) { newMonth ->
            monthChangeCallback?.invoke(newMonth)
        }
    }
}
