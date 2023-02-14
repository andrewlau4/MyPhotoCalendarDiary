package com.andsoftapps.utils

import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class ValueHolder<T>(var value: T?)

operator fun YearMonth.plus(monthToAdd: Int): YearMonth {
    return plusMonths(monthToAdd.toLong())
}

val YEAR_MONTH_FORMATED_STRING = DateTimeFormatter.ofPattern("MMMM yyyy")
