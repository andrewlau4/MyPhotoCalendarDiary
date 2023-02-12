package com.andsoftapps.db

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Keep
data class DiaryCalendarEntityWithQueryResult(
    @Embedded
    val diaryCalendarEntity: DiaryCalendarEntity,

    @ColumnInfo(name = "queryResult")
    val queryResult: Boolean?
)

@Keep
@Entity(tableName = "diarycalendar")
data class DiaryCalendarEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    var year: Int,

    var month: Int,

    var day: Int,

    var imagePath: String? = null,

    var userDiary: String? = null
)

@Entity(tableName = "diarycalendar_fts")
@Fts4(contentEntity = DiaryCalendarEntity::class)
class DiaryCalendarEntityFTS (

    var id: Int,

    @ColumnInfo(name = "userNotes")
    var userNotes: String?
)

