package com.andsoftapps.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DiaryCalendarEntity::class, DiaryCalendarEntityFTS::class], version = 1)
abstract class DiaryCalendarDatabase : RoomDatabase() {

    abstract fun diaryCalendarDao(): DiaryCalendarDao

}