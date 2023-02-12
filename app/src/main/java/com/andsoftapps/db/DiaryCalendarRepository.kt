package com.andsoftapps.db

import android.content.Context
import androidx.room.Room
import com.andsoftapps.ApplicationIOScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
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

}