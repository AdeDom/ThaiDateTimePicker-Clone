package com.adedom.calendar.customcalendar

import com.adedom.calendar.customcalendar.CustomCalendarMonthAdapter.Companion.CalendarDay
import java.util.*

interface CustomCalendarPickerController {

    fun onYearSelected(year: Int)

    fun onDayOfMonthSelected(year: Int, month: Int, day: Int)

    fun registerOnDateChangedListener(listener: CustomCalendarPickerDialog.OnDateChangedListener)

    fun unregisterOnDateChangedListener(listener: CustomCalendarPickerDialog.OnDateChangedListener)

    fun getSelectedDay(): CalendarDay

    fun getAccentColor(): Int

    fun getHighlightedDays(): Array<Calendar>

    fun getSelectableDays(): Array<Calendar>

    fun getFirstDayOfWeek(): Int

    fun getMinYear(): Int

    fun getMaxYear(): Int

    fun isOutOfRange(year: Int, month: Int, day: Int): Boolean

}
