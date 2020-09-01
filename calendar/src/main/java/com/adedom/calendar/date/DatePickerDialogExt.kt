package com.adedom.calendar.date

import androidx.appcompat.app.AppCompatActivity
import java.util.*

fun AppCompatActivity.datePickerDialog(callback: DatePickerDialog.OnDateSetListener) {
    val calendar = Calendar.getInstance()
    DatePickerDialog.newInstance(
        callback,
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show(supportFragmentManager, null)
}
