package com.adedom.library.datev2

import androidx.appcompat.app.AppCompatActivity
import com.adedom.library.date.DatePickerDialog
import java.util.*

fun AppCompatActivity.chocoCardCalendarDialog(callBack: DatePickerDialog.OnDateSetListener) {
    val calendar = Calendar.getInstance()
    DatePickerDialog.newInstance(
        callBack,
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show(supportFragmentManager, null)
}
