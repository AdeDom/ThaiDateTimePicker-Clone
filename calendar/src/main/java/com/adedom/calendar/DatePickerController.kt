package com.adedom.calendar

import com.adedom.calendar.MonthAdapter.Companion.CalendarDay
import java.util.*

interface DatePickerController {

    fun onYearSelected(year: Int)

    fun onDayOfMonthSelected(year: Int, month: Int, day: Int)

    fun registerOnDateChangedListener(listener: DatePickerDialog.OnDateChangedListener)

    fun unregisterOnDateChangedListener(listener: DatePickerDialog.OnDateChangedListener)

    fun getSelectedDay(): CalendarDay

    fun getAccentColor(): Int

    fun getHighlightedDays(): Array<Calendar>

    fun getSelectableDays(): Array<Calendar>

    fun getFirstDayOfWeek(): Int

    fun getMinYear(): Int

    fun getMaxYear(): Int

    fun isOutOfRange(year: Int, month: Int, day: Int): Boolean

}
