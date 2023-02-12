package com.andsoftapps.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.andsoftapps.ApplicationIOScope
import com.andsoftapps.db.DiaryCalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import java.time.YearMonth
import javax.inject.Inject

data class MonthViewUIState(val query: String?, val currentYearMonth: YearMonth)

@HiltViewModel
class DiaryCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    @ApplicationIOScope private val myCoroutineScope: CoroutineScope,
    private val diaryCalendarRepository: DiaryCalendarRepository
) : ViewModel() {
}