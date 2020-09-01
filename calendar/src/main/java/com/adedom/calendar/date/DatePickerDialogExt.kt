package com.adedom.calendar.date

import androidx.appcompat.app.AppCompatActivity
import java.util.*

fun AppCompatActivity.chocoCardCalendarDialog(work: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog.newInstance(
        object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(
                view: DatePickerDialog,
                year: Int,
                monthOfYear: Int,
                dayOfMonth: Int
            ) = work.invoke("$dayOfMonth/${monthOfYear.plus(1)}/$year")
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        setHighlightedDays(arrayOf(calendar))
        setSelectableDays(arrayOf(calendar))
    }.show(supportFragmentManager, null)
}
