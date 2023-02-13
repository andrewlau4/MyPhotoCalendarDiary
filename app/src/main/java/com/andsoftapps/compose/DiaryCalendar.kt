package com.andsoftapps.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.andsoftapps.viewmodel.DiaryCalendarViewModel

@Composable
fun DiaryCalendar(viewModel: DiaryCalendarViewModel = hiltViewModel()) {

    val uiState = viewModel.uiState.collectAsState()

    val navController = rememberNavController()

    NavigationLocalProvider(navController = navController) {

    }
}