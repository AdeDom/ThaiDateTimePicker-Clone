package com.adedom.thaidatetimepicker_clone

import android.os.Bundle
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

    override fun onDateSet(date: String) {
        tvCalendar.text = date
    }

}
