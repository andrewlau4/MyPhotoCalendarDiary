package com.andsoftapps.compose

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.andsoftapps.db.DiaryCalendarEntityWithQueryResult
import com.andsoftapps.viewmodel.DiaryCalendarViewModel
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

val LocalSaveImgPath =
    staticCompositionLocalOf<(YearMonth, Int, Uri?) -> Unit> { error("LocalSaveImgPath not initialized") }

val LocalQueryDairyEntries =
    staticCompositionLocalOf<(YearMonth) -> Flow<Map<Int, DiaryCalendarEntityWithQueryResult?>>> { error("LocalQueryDairyEntries not initialized") }

val LocalDiaryEntities =
    compositionLocalOf<Map<Int, DiaryCalendarEntityWithQueryResult?>> { error("LocalDiaryEntities not initialized") }

val LocalHideShowActionBar =
    staticCompositionLocalOf<(Boolean) -> Unit> { error("LocalHideShowActionBar not provided") }


@Composable
fun ComposeDiaryCalendarProviders(viewModel: DiaryCalendarViewModel,
                                  activity: AppCompatActivity,
                                  content:  @Composable () -> Unit) {

    CompositionLocalProvider(
        LocalSaveImgPath provides {
            yearAndMonth, day, uri ->
        },
//
//        LocalQueryDairyEntries provides {
//            yearAndMonth ->
//            return flowOf()
//        },

        LocalHideShowActionBar provides {
            isHide ->
            if (isHide) {
                activity.supportActionBar?.hide()
            } else {
                activity.supportActionBar?.show()
            }
        }

    ) {
        content()
    }

}