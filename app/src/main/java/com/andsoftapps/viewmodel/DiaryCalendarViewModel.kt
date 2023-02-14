package com.andsoftapps.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andsoftapps.ApplicationIOScope
import com.andsoftapps.db.DiaryCalendarEntityWithQueryResult
import com.andsoftapps.db.DiaryCalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject


private val TAG = "DiaryCalendarViewModel"
data class DiaryCalendarUIState(val query: String?, val currentYearMonth: YearMonth)

@HiltViewModel
class DiaryCalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    @ApplicationIOScope private val myCoroutineScope: CoroutineScope,
    private val diaryCalendarRepository: DiaryCalendarRepository
) : ViewModel() {

    val QUERY_KEY = "QUERY"
    val CURRENT_YEARMONTH_KEY = "CURRENT_YEARMONTH"

    private val _uiState = MutableStateFlow(DiaryCalendarUIState(_query, _currentYearMonth))
    val uiState: StateFlow<DiaryCalendarUIState> = _uiState.asStateFlow()

    private var _query: String?
        set(value) {
            savedStateHandle[QUERY_KEY] = value
            _uiState.update { it.copy(query = value) }
        }
        get() {
            return savedStateHandle[QUERY_KEY]
        }

    private var _currentYearMonth: YearMonth
        set(andrewValue) {
            savedStateHandle[CURRENT_YEARMONTH_KEY] = andrewValue
            _uiState.update { it.copy(currentYearMonth = andrewValue) }
        }
        get() {
            return savedStateHandle[CURRENT_YEARMONTH_KEY] ?: YearMonth.now()
        }


//    private val queryFlowState: Flow<Map<Int, DiaryCalendarEntityWithQueryResult?>> = uiState.flatMapLatest {
//            state -> diaryCalendarRepository.getDiaryCalendarByMonth(state.currentYearMonth.year,
//        state.currentYearMonth.monthValue - 1, state.query)
//    }.map {
//        it.groupBy { it.diaryCalendarEntity.day }.mapValues { if (it.value.size > 0) it.value[0] else null }
//    }.stateIn(scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000, replayExpirationMillis = 0),
//        initialValue = mapOf<Int, DiaryCalendarEntityWithQueryResult>())

    fun setQuery(query: String?) {
        _query = query
    }

    fun setCurrentYearMonth(currentYearMonth: YearMonth) {
        _currentYearMonth = currentYearMonth
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun queryDiaryCalendarEntries(month: YearMonth): Flow<Map<Int, DiaryCalendarEntityWithQueryResult?>> {
        //due to animation, we may have 2 active queries flow at the same time when the old month is sliding out
        //   and the new month sliding in, so we return a separate flow every time
        return uiState.map { it.query }.distinctUntilChanged().flatMapLatest { query ->
            diaryCalendarRepository.getDiaryCalendarByMonth(month.year,
                month.monthValue, query)
        }.map {
            it.groupBy { it.diaryCalendarEntity.day }
                .mapValues { if (it.value.size > 0) it.value[0] else null }
        }
    }

    fun saveDateImageUri(month: YearMonth, day: Int, uri: Uri?) {
        myCoroutineScope.launch {
            Log.d(TAG, "viewModel saveDateImageUri  month $month,  day $day,  uri $uri")
            diaryCalendarRepository.insertOrUpdate(month, day, uri)
        }
    }

}