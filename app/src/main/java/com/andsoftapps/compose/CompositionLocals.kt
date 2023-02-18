package com.andsoftapps.compose

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import com.andsoftapps.db.DiaryCalendarEntityWithQueryResult
import com.andsoftapps.viewmodel.DiaryCalendarViewModel
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

val LocalSaveImgPath =
    staticCompositionLocalOf<(YearMonth, Int, Uri?) -> Unit> { error("LocalSaveImgPath not initialized") }

val LocalQueryDairyEntries =
    staticCompositionLocalOf<(YearMonth) -> Flow<Map<Int, DiaryCalendarEntityWithQueryResult?>>> { error("LocalQueryDairyEntries not initialized") }

val LocalDiaryEntities =
    compositionLocalOf<Map<Int, DiaryCalendarEntityWithQueryResult?>> { mapOf() }

val LocalHideShowActionBar =
    staticCompositionLocalOf<(Boolean) -> Unit> { error("LocalHideShowActionBar not provided") }

val LocalNavigation =
    staticCompositionLocalOf<(String?) -> Unit> { error("LocalNavigateComposition not initialized") }

@Composable
fun ComposeDiaryCalendarLocalProviders(viewModel: DiaryCalendarViewModel,
                                       activity: AppCompatActivity,
                                       content:  @Composable () -> Unit) {

    CompositionLocalProvider(
        LocalSaveImgPath provides {
            yearAndMonth, day, uri ->
            viewModel.saveDateImageUri(yearAndMonth, day, uri)
        },

        LocalQueryDairyEntries provides {
            yearAndMonth ->
            viewModel.queryDiaryCalendarEntries(yearAndMonth)
        },

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

@Composable
fun NavigationLocalProvider(navController: NavHostController, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalNavigation provides { destination: String? ->
            if (destination != null && destination != navController.currentDestination?.route) {
                navController.navigate(destination) {
                    launchSingleTop = true
                }
            } else {
                navController.popBackStack()
            }
        }
    ) {
        content()
    }
}