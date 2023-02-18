package com.andsoftapps.compose

import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.andsoftapps.R
import com.andsoftapps.ui.theme.DividerAlpha
import com.andsoftapps.ui.theme.NavigationButtonBrush
import com.andsoftapps.ui.theme.Neutral0
import com.andsoftapps.ui.theme.Neutral4
import com.andsoftapps.ui.theme.NotesLineColor
import com.andsoftapps.ui.theme.NotesPageColor
import com.andsoftapps.ui.theme.Ocean3
import com.andsoftapps.ui.theme.RainbowColorsBrush
import com.andsoftapps.ui.theme.Shadow4
import com.andsoftapps.ui.theme.iconInteractive
import com.andsoftapps.ui.theme.textHelp
import com.andsoftapps.ui.theme.textPrimary
import com.andsoftapps.ui.theme.textSecondary
import com.andsoftapps.ui.theme.uiBackground
import com.andsoftapps.utils.MONTH_FORMATED_STRING
import com.andsoftapps.utils.YEAR_FORMATED_STRING
import com.andsoftapps.utils.YEAR_MONTH_FORMATED_STRING
import com.andsoftapps.viewmodel.DiaryDetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.YearMonth
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


private val BottomBarHeight = 56.dp
private val TitleHeight = 128.dp
private val GradientScroll = 180.dp
private val ImageOverlap = 115.dp
private val MinTitleOffset = 56.dp
private val MinImageOffset = 12.dp
private val MaxTitleOffset = ImageOverlap + MinTitleOffset + GradientScroll
private val ExpandedImageSize = 300.dp
private val CollapsedImageSize = 150.dp
private val HzPadding = Modifier.padding(horizontal = 24.dp)

private val LayoutId_Title1 = "title1"
private val LayoutId_Title2 = "title2"

private val NumRows = 30

//this layout and animation is based on jetpack comppose samples:
//  https://github.com/android/compose-samples
@Composable
fun DiaryDetail(month: YearMonth,
                day: Int,
                diaryDetailViewModel: DiaryDetailViewModel = hiltViewModel()) {
    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    LocalHideShowActionBar.current(true)

    diaryDetailViewModel.setCurrentDay(month, day)

    val uiState by diaryDetailViewModel.uiState.collectAsState()


    val navigationCallbackHandler = LocalNavigation.current
    InstallBackPressHandler { navigationCallbackHandler(null) }

    val coroutineScope = rememberCoroutineScope()

    Box {
        val scroll = rememberScrollState(0)


        Header()

        DetailBody(scroll = scroll,
            userDiaryTextField = { uiState.userDiary },
            userDiaryTextChangeCallback = { diaryDetailViewModel.setUserDiary(it) }
        )

        Title(month = month, day = day) { scroll.value }

        DairyDetailImage(getImageUrl = { uiState.uri },
            scrollProvider = { scroll.value })

        Up { navigationCallbackHandler(null)  }

    }
}

@Composable
private fun Header() {
    Spacer(
        modifier = Modifier
            .height(280.dp)
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Shadow4, Ocean3)))
    )
}

@Composable
private fun DetailBody(scroll: ScrollState,
                       userDiaryTextField: () -> TextFieldValue,
                       userDiaryTextChangeCallback: (TextFieldValue) -> Unit) {

    var detailSurfaceOffsetFromParent by remember { mutableStateOf<Offset?>(null) }
    var titleHeightOffsetFromParent by remember { mutableStateOf<Offset?>(null) }
    var notepadWidth by remember { mutableStateOf<Int?>(null) }

    val coroutineScope = rememberCoroutineScope()

    KeyboardShownListener {
        coroutineScope.launch {
            while (scroll.isScrollInProgress) {
                delay(500)
            }
            delay(1000)
            scroll.scrollTo(
                (detailSurfaceOffsetFromParent!!.y).roundToInt() +
                        (titleHeightOffsetFromParent!!.y).roundToInt()
            )
        }
    }

    //https://developer.android.com/reference/kotlin/androidx/compose/foundation/interaction/InteractionSource
    val interactionSource = scroll.interactionSource

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction: Interaction ->
            println("interaction: ${interaction}")
        }
    }

    Column {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(MinTitleOffset)
                .background(color = MaterialTheme.colors.primary)
        )

        Column(modifier = Modifier.verticalScroll(state = scroll)) {

            Spacer(
                Modifier
                    .height(GradientScroll)
                    .fillMaxWidth(fraction = 0.05f)
                    .background(color = Color.Magenta)
            )

            DetailSurface(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.9f)
                    .onGloballyPositioned { layoutCoordinates ->
                        detailSurfaceOffsetFromParent = layoutCoordinates.positionInParent()
                    },
                color = Color.LightGray,
                elevation = 10.dp,
                shape = Shapes().medium
            ) {
                Column {
                    Spacer(
                        Modifier
                            .height(ImageOverlap)
                            .fillMaxWidth(fraction = 0.05f)
                            .background(color = Color.Red)
                    )

                    Spacer(
                        Modifier
                            .height(TitleHeight)
                            .onGloballyPositioned { layoutCoordinates ->
                                titleHeightOffsetFromParent = layoutCoordinates.positionInParent()
                            }
                            .fillMaxWidth(fraction = 0.05f)
                            .background(color = Color.Green)
                    )

                    Spacer(
                        Modifier
                            .height(16.dp)
                            .fillMaxWidth(fraction = 0.05f)
                            .background(color = Color.Cyan)
                    )

                    var lineBottomHeight by remember { mutableStateOf<Float?>(null) }

                    Box(modifier = Modifier
                        .layout {
                                measurable, constraints ->

                            //this call is needed for Compose to call the onTextLayout (further below)
                            // so that i can get the lineBottomHeight, but i don't know why
                            // without this call, lineBottomHeight will be null
                            measurable.maxIntrinsicHeight(constraints.maxWidth)

                            if (constraints.hasBoundedWidth) {
                                notepadWidth = constraints.maxWidth
                            }

                            val maxHeight = lineBottomHeight!!.toInt() * NumRows
                            val placeable =
                                measurable.measure(constraints.copy(maxHeight = maxHeight))

                            layout(
                                constraints.maxWidth,
                                maxHeight
                            ) {
                                placeable.place(0, 0)
                            }
                        }
                        .drawWithContent {
                            NotepadBackground(notepadWidth!!, lineBottomHeight!!)

                            drawContent()
                        }
                    ) {

                        NotepadTextField(
                            userDiaryTextField = userDiaryTextField,
                            userDiaryTextChangeCallback = userDiaryTextChangeCallback,
                            setLineBottomHeight = { lineBottomHeight = it }
                        )

                    }

                }
            }

        }

    }

}


private fun ContentDrawScope.NotepadBackground(
    notepadWidth: Int,
    lineBottomHeight: Float
) {
    //draw the Note pad to the background

    drawRect(
        NotesPageColor,
        topLeft = Offset(1f, 0f),
        size = Size(
            notepadWidth.toFloat(),
            lineBottomHeight * NumRows
        )
    )

    for (i in 1..NumRows) {
        drawRect(
            NotesLineColor,
            topLeft = Offset(
                1f,
                lineBottomHeight * i
            ),
            size = Size(notepadWidth.toFloat(), 1f),
        )
    }

}

@Composable
fun DetailSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = uiBackground,  // JetsnackTheme.colors.uiBackground,
    contentColor: Color = textSecondary, // JetsnackTheme.colors.textSecondary,
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = shape, clip = false)
            .zIndex(elevation.value)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .background(
                color = color, // getBackgroundColorForElevation(color, elevation),
                shape = shape
            )
            .clip(shape)
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NotepadTextField(
    //https://www.youtube.com/watch?v=zMKMwh9gZuI
    // 24:44  explain passing lambda is better than passing value
    userDiaryTextField: () -> TextFieldValue,
    userDiaryTextChangeCallback: (TextFieldValue) -> Unit,
    setLineBottomHeight: (Float) -> Unit) {

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        modifier = Modifier.fillMaxSize(),
        value = userDiaryTextField(),
        onValueChange = userDiaryTextChangeCallback,
        maxLines = NumRows - 1,
        textStyle = TextStyle(color = Color.Black,
            fontWeight = FontWeight.Normal,
            fontSize = 4.em,
            lineHeightStyle = LineHeightStyle(
                trim = LineHeightStyle.Trim.Both,
                alignment = LineHeightStyle.Alignment.Center
            ),
            //see https://developer.android.com/jetpack/compose/text#includefontpadding_and_lineheight_apis
            platformStyle = PlatformTextStyle(
                includeFontPadding = false
            )
        ),
        onTextLayout = {
                textLayoutResult: TextLayoutResult ->
            if (textLayoutResult.lineCount > 0) {
                setLineBottomHeight(textLayoutResult.getLineBottom(0))
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text, autoCorrect = true,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            focusManager.clearFocus()
            keyboardController?.hide() }),
    )
}

@Composable
private fun Title(month: YearMonth, day: Int, scrollProvider: () -> Int) {
    val maxOffset = with(LocalDensity.current) { MaxTitleOffset.toPx() }
    val minOffset = with(LocalDensity.current) { MinTitleOffset.toPx() }

    val collapseRange = with(LocalDensity.current) { (MaxTitleOffset - MinTitleOffset).toPx() }
    val collapseFractionProvider = {
        val scrollProviderVal = scrollProvider()
        (scrollProviderVal / collapseRange).coerceIn(0f, 1f)
    }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .heightIn(min = TitleHeight, max = TitleHeight)
            .statusBarsPadding()
            .offset {
                val scroll = scrollProvider()
                val offset = (maxOffset - scroll).coerceAtLeast(minOffset)
                IntOffset(x = 0, y = offset.toInt())
            }
            .background(
                color = MaterialTheme.colors.primary
            )
    ) {
//        Spacer(Modifier.height(16.dp))
        TitleLayout(
            collapseFractionProvider = collapseFractionProvider,
//            title1 =  {
//                Text(
//                    text = month.format(YEAR_FORMATED_STRING),
//                    style = MaterialTheme.typography.h4,
//                    color = textSecondary,
//                    modifier = HzPadding
//                )
//            },
//            title2 = {
//                Text(
//                    text = month.format(MONTH_FORMATED_STRING),
//                    style = MaterialTheme.typography.h4,
//                    color = textSecondary,
//                    modifier = HzPadding
//                )
//            }
        ) {
            Text(
                text = "$day ${month.format(MONTH_FORMATED_STRING)}",
                style = MaterialTheme.typography.h4,
                color = Color.White,
                modifier = HzPadding.layoutId(LayoutId_Title1)
            )
            Text(
                text =  month.format(YEAR_FORMATED_STRING),
                style = MaterialTheme.typography.h4,
                color = Color.White,
                modifier = HzPadding.layoutId(LayoutId_Title2)
            )
        }
//        Text(
//            text = "tagLine",  //snack.tagline,
//            style = MaterialTheme.typography.subtitle2,
//            fontSize = 20.sp,
//            color = textHelp,
//            modifier = HzPadding
//        )
//        Spacer(Modifier.height(24.dp))
//        Text(
//            text = "$4.00", //formatPrice(snack.price),
//            style = MaterialTheme.typography.h6,
//            color = textPrimary,
//            modifier = HzPadding
//        )
//
//        Spacer(Modifier.height(8.dp))
//
//        Divider(
//            modifier = Modifier,  // modifier,
//            color =   Neutral4.copy(alpha = DividerAlpha),  // color,
//            thickness = 1.dp, // thickness,
//            startIndent = 0.dp   //startIndent
//        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TitleLayout(
    collapseFractionProvider: () -> Float,
//    title1: @Composable () -> Unit,
//    title2: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val constraintHeight = with(LocalDensity.current) { TitleHeight.roundToPx() }

    Layout(
        modifier = modifier,
        contents = listOf(content)
    ) {
        (measurables), constraints ->

        check(measurables.size == 2)

        val collapseFraction = collapseFractionProvider()

        val placeables = measurables.groupBy {
            it.layoutId
        }.mapValues {
            it.value[0].measure(constraints)
        }

        val title1Width = placeables.get(LayoutId_Title1)!!.width
        val title1Height = placeables.get(LayoutId_Title1)!!.height

        val title2Width = placeables.get(LayoutId_Title2)!!.width
        val title2Height = placeables.get(LayoutId_Title2)!!.height

        val constraintHeight = constraintHeight

        //want to move title1 to the next line when it is fully collapsed, so the
        // destination of Y position is title2's height
        val title1_XPosition = 0 // lerp(title1Width, 0, collapseFraction)
        val title1_YPosition = lerp((constraintHeight - title1Height) / 2, title2Height, collapseFraction)

        val title2_XPosition = lerp(title1Width, 0, collapseFraction)
        val title2_YPosition = lerp((constraintHeight - title2Height) / 2, 0, collapseFraction)

        //val constraintHeight = constraintHeight //if (constraints.hasBoundedHeight) {
        //    constraints.maxHeight
        //} else {
            //title1_YPosition + placeables.get(LayoutId_Title1)!!.height
        //}

        layout(constraints.maxWidth, constraintHeight) {
            placeables.get(LayoutId_Title1)!!.place(title1_XPosition, title1_YPosition)
            placeables.get(LayoutId_Title2)!!.place(title2_XPosition, title2_YPosition)
        }
    }
}

@Composable
fun DairyDetailImage(
    getImageUrl: () -> String?,
    scrollProvider: () -> Int
) {
    val collapseRange = with(LocalDensity.current) { (MaxTitleOffset - MinTitleOffset).toPx() }
    val collapseFractionProvider = {
        val scrollProviderVal = scrollProvider()
        (scrollProviderVal / collapseRange).coerceIn(0f, 1f)
    }

    CollapsingImageLayout(
        collapseFractionProvider = collapseFractionProvider,
        modifier = HzPadding.then(Modifier.statusBarsPadding())
    ) {
        val imageUrl = getImageUrl()
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "contentDescription",
                //https://developer.android.com/jetpack/compose/graphics/images/customize
                placeholder = painterResource(R.drawable.placeholder_image),
                modifier = Modifier.fillMaxSize()
                    .border(
                        BorderStroke(1.dp, RainbowColorsBrush),
                        CircleShape
                    )
                    .padding(1.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.placeholder_image),
                contentScale = ContentScale.Crop,
                contentDescription = "contentDescription",
            )
        }

    }
}


@Composable
private fun CollapsingImageLayout(
    collapseFractionProvider: () -> Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        check(measurables.size == 1)

        val collapseFraction = collapseFractionProvider()

        val imageMaxSize = min(ExpandedImageSize.roundToPx(), constraints.maxWidth)
        val imageMinSize = max(CollapsedImageSize.roundToPx(), constraints.minWidth)

        val imageWidth = lerp(imageMaxSize, imageMinSize, collapseFraction)
        val imagePlaceable = measurables[0].measure(Constraints.fixed(imageWidth, imageWidth))

        val imageY =
            lerp(MinTitleOffset, MinImageOffset, collapseFraction).roundToPx()
        val imageX = lerp(
            (constraints.maxWidth - imageWidth) / 2, // centered when expanded
            constraints.maxWidth - imageWidth, // right aligned when collapsed
            collapseFraction
        )
        layout(
            width = constraints.maxWidth,
            height = imageY + imageWidth
        ) {
            imagePlaceable.placeRelative(imageX, imageY)
        }
    }
}

@Composable
private fun Up(upPress: () -> Unit) {
    IconButton(
        onClick = upPress,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .size(36.dp)
            .background(
                brush = NavigationButtonBrush,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = mirroringBackIcon(),
            tint = iconInteractive,
            contentDescription = stringResource(R.string.label_back)
        )
    }
}

@Composable
fun mirroringBackIcon() = mirroringIcon(
    ltrIcon = Icons.Outlined.ArrowBack, rtlIcon = Icons.Outlined.ArrowForward
)

@Composable
fun mirroringIcon(ltrIcon: ImageVector, rtlIcon: ImageVector): ImageVector =
    if (LocalLayoutDirection.current == LayoutDirection.Ltr) ltrIcon else rtlIcon