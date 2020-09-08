package com.adedom.calendar.date

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import java.util.*

fun AppCompatActivity.datePickerDialog(
    locale: Locale = DatePickerDialog.LOCALE_EN,
    callback: DatePickerDialog.OnDateSetListener,
    year: Int = Calendar.getInstance().get(Calendar.YEAR),
    monthOfYear: Int = Calendar.getInstance().get(Calendar.MONTH),
    dayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    accentColor: Int = Color.parseColor("#ffce55"),
    isTextFullDate: Boolean = true,
) = DatePickerDialog.newInstance(
    callback,
    year,
    monthOfYear,
    dayOfMonth
).apply {
    setLocale(locale)
    setAccentColor(accentColor)
    setTextFullDateVisibility(isTextFullDate)
}.show(supportFragmentManager, null)
