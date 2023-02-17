package com.andsoftapps.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.andsoftapps.ApplicationIOScope
import com.andsoftapps.db.DiaryCalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class DiaryDetailsUIState(val month: YearMonth? = null,
                               val day: Int? = null,
                               val uri: String? = null,
                               val userDiary: TextFieldValue = TextFieldValue("")
)

@HiltViewModel
class DiaryDetailViewModel
@Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    @ApplicationIOScope private val myCoroutineScope: CoroutineScope,
    private val diaryCalendarRepository: DiaryCalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryDetailsUIState())
    val uiState: StateFlow<DiaryDetailsUIState> = _uiState.asStateFlow()

    fun setCurrentDay(month: YearMonth, day: Int) {
        _uiState.update { DiaryDetailsUIState(month, day) }

        myCoroutineScope.launch {
            val monthEntry = diaryCalendarRepository.getDailyEntry(month.year, month.monthValue, day)
            if (monthEntry != null) {
                _uiState.update {
                    DiaryDetailsUIState(
                        month = month,
                        day = day,
                        uri = monthEntry.imagePath,
                        userDiary = TextFieldValue(monthEntry.userDiary ?: "")
                    )
                }
            }
        }
    }

    fun setUserDiary(userDiaryNew: TextFieldValue) {
        val state = _uiState.value
        with (state) {
            if (month != null && day != null) {
                diaryCalendarRepository.delaySaveUserNotes(
                    year = month.year,
                    month = month.monthValue,
                    day = day,
                    userDiary = userDiaryNew.text
                )
            }
        }

        _uiState.update {
            it.copy(userDiary = userDiaryNew.copy())
        }
    }

}