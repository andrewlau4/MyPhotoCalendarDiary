package com.andsoftapps.db

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryCalendarDao {

    @Query("Select * from diarycalendar")
    fun getAllDiaryCalendar(): Flow<List<DiaryCalendarEntity>>

}