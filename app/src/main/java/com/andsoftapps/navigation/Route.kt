package com.andsoftapps.navigation

import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.*

sealed class Route(val route: String) {

    object Home: Route("DiaryCalendarMain")

    object Detail: Route("DiaryDailyDetail/{date}") {

        fun createRoute(date: Date) = "DiaryDailyDetail/${date.toString()}"

        fun createRoute(month: YearMonth, days: Int): String {
            fun Int.padZero(): String {
                return toString().padStart(2, '0')
            }

            return "DiaryDailyDetail/${"" + month.year + "-" + (month.monthValue).padZero() + "-" + days.padZero() }"
        }

        fun retrieveFromRoute(date: String): Pair<YearMonth, Int> {
            val match = Regex("(\\d{4})-(\\d{2})-(\\d{2})").find(date)
            val (year, month, day) = match?.destructured!!
            return Pair(YearMonth.of(year.toInt(), month.toInt()), day.toInt())
        }

    }
}