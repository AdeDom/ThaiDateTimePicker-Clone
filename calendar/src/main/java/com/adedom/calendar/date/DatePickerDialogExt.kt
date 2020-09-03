package com.adedom.calendar.date

import androidx.appcompat.app.AppCompatActivity
import java.util.*

fun AppCompatActivity.datePickerDialog(
    locale: Locale,
    callback: DatePickerDialog.OnDateSetListener,
    year: Int = Calendar.getInstance().get(Calendar.YEAR),
    monthOfYear: Int = Calendar.getInstance().get(Calendar.MONTH),
    dayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
) = DatePickerDialog.newInstance(
    locale,
    callback,
    year,
    monthOfYear,
    dayOfMonth
).show(supportFragmentManager, null)
