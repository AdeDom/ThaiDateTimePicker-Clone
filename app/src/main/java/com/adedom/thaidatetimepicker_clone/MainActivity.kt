package com.adedom.thaidatetimepicker_clone

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adedom.calendar.date.DatePickerDialog
import com.adedom.calendar.date.datePickerDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btAnnoDomini.setOnClickListener {
            datePickerDialog(DatePickerDialog.LOCALE_EN, this)
        }

        btBuddhistEra.setOnClickListener {
            datePickerDialog(DatePickerDialog.LOCALE_TH, this)
        }
    }

    override fun onDateSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val date = "$dayOfMonth/$monthOfYear/$year"
        tvCalendar.text = date
    }

    override fun onPeriodDate(diff: Long) {
        Toast.makeText(baseContext, diff.toString(), Toast.LENGTH_SHORT).show()
    }

}
