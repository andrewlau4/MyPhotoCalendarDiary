package com.andsoftapps.db

import android.content.Context
import android.net.Uri
import androidx.room.Room
import com.andsoftapps.ApplicationIOScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

private const val DATABASE_NAME = "diarycalendar-database"

@Singleton
class DiaryCalendarRepository
@Inject constructor(
    @ApplicationContext context: Context,
    @ApplicationIOScope private val myCoroutineScope: CoroutineScope
) {

    private val database: DiaryCalendarDatabase = Room
        .databaseBuilder(context.applicationContext,
            DiaryCalendarDatabase::class.java,
            DATABASE_NAME
            )
        .build()

    suspend fun insertOrUpdate(month: YearMonth, day: Int, uri: Uri?) {
        database.diaryCalendarDao().insertOrUpdate(month, day, uri)
    }

    suspend fun insertOrUpdate(year: Int, month: Int, day: Int, userDiary: String?) {
        database.diaryCalendarDao().insertOrUpdate(year, month, day, userDiary)
    }

    fun getDiaryCalendarByMonth(year: Int, month: Int, query: String?): Flow<List<DiaryCalendarEntityWithQueryResult>> {
        //added "*   and  *" and escape the " with "" within the string
        val queryString = query?.run {
            trim().split(regex = Regex("\\s+"))
                .map { "\"*" + replace("\"", "\"\"") + "*\"" }
                .joinToString(" OR ") { it }
        } ?: ""

        return database.diaryCalendarDao().getDiaryCalendarByMonthWithQuery(year, month, queryString)
    }

}