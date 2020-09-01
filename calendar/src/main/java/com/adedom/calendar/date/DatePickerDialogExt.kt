package com.adedom.calendar.date

import androidx.appcompat.app.AppCompatActivity
import java.util.*

fun AppCompatActivity.chocoCardCalendarDialog(callBack: DatePickerDialog.OnDateSetListener) {
    val calendar = Calendar.getInstance()
    DatePickerDialog.newInstance(
        callBack,
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        setHighlightedDays(arrayOf(calendar))
        setSelectableDays(arrayOf(calendar))
    }.show(supportFragmentManager, null)
}
