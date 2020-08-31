package com.adedom.library.datev2

import androidx.appcompat.app.AppCompatActivity
import java.util.*

fun AppCompatActivity.chocoCardCalendarDialog(callBack: com.adedom.library.date.DatePickerDialog.OnDateSetListener) {
    val calendar = Calendar.getInstance()
    DatePickerDialog.newInstance(
        callBack,
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        highlightedDays = arrayOf(calendar)
        selectableDays = arrayOf(calendar)
    }.show(supportFragmentManager, null)
}
