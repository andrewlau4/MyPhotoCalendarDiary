package com.andsoftapps.utils

import java.time.YearMonth
import java.time.format.DateTimeFormatter


val YEAR_MONTH_FORMATED_STRING = DateTimeFormatter.ofPattern("MMMM yyyy")

data class ValueHolder<T>(var value: T?)

operator fun YearMonth.plus(monthToAdd: Int): YearMonth {
    return plusMonths(monthToAdd.toLong())
}

val YearMonth.firstDayOfWeek
    get() = atDay(1).dayOfWeek.value % 7
val YearMonth.totalDaysInMonthPlusLeftOver
    get() = lengthOfMonth() + firstDayOfWeek


