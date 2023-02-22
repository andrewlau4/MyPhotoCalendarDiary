package com.andsoftapps.db

import android.net.Uri
import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

//how to debug db
//https://stackoverflow.com/questions/64176567/why-there-is-no-android-database-inspector-tools-in-android-studio-4-2-canary-13
//https://medium.com/androiddevelopers/database-inspector-9e91aa265316

private val TAG = "DiaryCalendarDao"
@Dao
interface DiaryCalendarDao {

    @Query("Select * from diarycalendar")
    fun getAllDiaryCalendar(): Flow<List<DiaryCalendarEntity>>

    //using the Database Inspector to debug this query, see https://medium.com/androiddevelopers/database-inspector-9e91aa265316
    //it seems on some device, i cannot use MATCH when joining a FTS table with a non FTS table
    //but the 'with' statement seems to work, so change below to use 'with' statement
    @Query(
        //this is valid syntax but not working on some device
//        """Select diarycalendar.*,
//             CASE WHEN
//            fts.id
//             IS NOT NULL THEN
//             1
//            ELSE 0 END as queryResult
//             from diarycalendar left join diarycalendar_fts as fts
//            on fts.id = diarycalendar.id
//            where year = :year and month = :month
//              and fts.diarycalendar_fts MATCH :query  """

        """ with fts as (select id from diarycalendar_fts where userDiary MATCH :query)
            Select diarycalendar.*, 
             CASE WHEN 
            fts.id 
             IS NOT NULL THEN 
             1 
            ELSE 0 END as queryResult 
             from diarycalendar left join fts 
            on fts.id = diarycalendar.id 
            where year = :year and month = :month """
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