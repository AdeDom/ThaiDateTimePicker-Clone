package com.adedom.library.date

import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.chocoCardCalendarDialog(work: (String) -> Unit) {
    DatePickerDialog.newInstance {
        work.invoke(it)
    }.show(supportFragmentManager, null)
}
