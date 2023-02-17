package com.andsoftapps.compose

import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.andsoftapps.ui.theme.NotesLineColor
import com.andsoftapps.ui.theme.NotesPageColor
import com.andsoftapps.ui.theme.Ocean3
import com.andsoftapps.ui.theme.Shadow4
import com.andsoftapps.ui.theme.textSecondary
import com.andsoftapps.ui.theme.uiBackground
import com.andsoftapps.viewmodel.DiaryDetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.YearMonth
import kotlin.math.roundToInt


private val BottomBarHeight = 56.dp
private val TitleHeight = 128.dp          //green
private val GradientScroll = 180.dp     //magenta
private val ImageOverlap = 115.dp       //red
private val MinTitleOffset = 56.dp     //yellow
private val MinImageOffset = 12.dp
private val MaxTitleOffset = ImageOverlap + MinTitleOffset + GradientScroll
private val ExpandedImageSize = 300.dp
private val CollapsedImageSize = 150.dp
private val HzPadding = Modifier.padding(horizontal = 24.dp)

private val NumRows = 30

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


    var editBoxOffsetFromParent: Offset? = null

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
                .background(color = Color.Yellow)
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

                    Box(modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
                        editBoxOffsetFromParent = layoutCoordinates.positionInParent() }
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
        maxLines = NumRows,
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