package com.andsoftapps.db

import android.net.Uri
import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

private val TAG = "DiaryCalendarDao"
@Dao
interface DiaryCalendarDao {

    @Query("Select * from diarycalendar")
    fun getAllDiaryCalendar(): Flow<List<DiaryCalendarEntity>>

    @Query("Select diarycalendar.*," +
            " CASE WHEN " +
            "fts.id" +
            " IS NOT NULL THEN " +
            " TRUE " +
            "ELSE FALSE END as queryResult" +
            " from diarycalendar left join diarycalendar_fts as fts " +
            "on fts.id = diarycalendar.id and fts.diarycalendar_fts MATCH :query " +
            "where year = :year and month = :month"
    )
    fun getDiaryCalendarByMonthWithQuery(year: Int, month: Int, query: String = "*"): Flow<List<DiaryCalendarEntityWithQueryResult>>

    @Query("Select * from diarycalendar where year = :year and month = :month and day = :day")
    suspend fun getDiaryCalendarByDate(year: Int, month: Int, day: Int): DiaryCalendarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(monthCalendarEntity: DiaryCalendarEntity): Long

    suspend fun insertOrUpdate(yearMonth: YearMonth, day: Int, uri: Uri?) {
        var diaryCalendarEntity = getDiaryCalendarByDate(yearMonth.year, yearMonth.monthValue, day)
        if (diaryCalendarEntity == null) {
            diaryCalendarEntity = DiaryCalendarEntity(null, yearMonth.year, yearMonth.monthValue, day, uri.toString(), null)
        } else {
            diaryCalendarEntity.imagePath = uri.toString()
        }

        insert(diaryCalendarEntity)
    }

    suspend fun insertOrUpdate(year: Int, month: Int, day: Int, userDiary: String?) {
        var diaryCalendarEntity = getDiaryCalendarByDate(year, month, day)
        if (diaryCalendarEntity == null) {
            diaryCalendarEntity = DiaryCalendarEntity(null, year, month, day, null, userDiary)
        } else {
            diaryCalendarEntity.userDiary = userDiary
        }

        insert(diaryCalendarEntity)
    }
}