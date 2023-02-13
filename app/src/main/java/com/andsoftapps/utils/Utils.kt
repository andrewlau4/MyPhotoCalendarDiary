package com.andsoftapps.utils

import java.time.YearMonth

data class ValueHolder<T>(var value: T?)

operator fun YearMonth.plus(monthToAdd: Int): YearMonth {
    return plusMonths(monthToAdd.toLong())
}