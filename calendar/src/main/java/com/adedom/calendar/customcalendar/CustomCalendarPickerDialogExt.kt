package com.adedom.calendar.customcalendar

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import java.util.*

fun AppCompatActivity.customCalendarPickerDialog(
    callback: CustomCalendarPickerDialog.OnDateSetListener,
    year: Int = Calendar.getInstance().get(Calendar.YEAR),
    monthOfYear: Int = Calendar.getInstance().get(Calendar.MONTH),
    dayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    locale: Locale = CustomCalendarPickerDialog.LOCALE_EN,
    accentColor: Int = Color.parseColor("#ffce55"),
    isTitleLabelFullDate: Boolean = true,
    minDate: Calendar? = null,
    maxDate: Calendar? = null,
) = CustomCalendarPickerDialog.newInstance(
    callback,
    year,
    monthOfYear,
    dayOfMonth
).apply {
    setLocale(locale)
    setAccentColor(accentColor)
    setTitleLabelFullDate(isTitleLabelFullDate)
    if (minDate != null) setMinDate(minDate)
    if (maxDate != null) setMaxDate(maxDate)
}.show(supportFragmentManager, null)
