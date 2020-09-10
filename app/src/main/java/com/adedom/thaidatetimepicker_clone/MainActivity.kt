package com.adedom.thaidatetimepicker_clone

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adedom.calendar.date.DatePickerDialog
import com.adedom.calendar.date.datePickerDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btAnnoDomini.setOnClickListener {
            datePickerDialog(
                this,
                locale = DatePickerDialog.LOCALE_EN,
            )
        }

        btBuddhistEra.setOnClickListener {
            datePickerDialog(
                this,
                locale = DatePickerDialog.LOCALE_TH,
            )
        }

        btAccentColor.setOnClickListener {
            datePickerDialog(
                this,
                accentColor = Color.parseColor("#03DAC5"),
            )
        }

        btMinDate.setOnClickListener {
            datePickerDialog(
                this,
                minDate = Calendar.getInstance()
            )
        }

        btMaxDate.setOnClickListener {
            datePickerDialog(
                this,
                maxDate = Calendar.getInstance()
            )
        }

        btHideTitleLabelFullDate.setOnClickListener {
            datePickerDialog(
                this,
                isTitleLabelFullDate = false,
            )
        }

        btGoDateStartIntern.setOnClickListener {
            datePickerDialog(
                this,
                year = 2020,
                monthOfYear = 2,
                dayOfMonth = 17,
            )
        }
    }

    override fun onDateSet(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val date = "$dayOfMonth/$monthOfYear/$year"
        tvCalendar.text = date
    }

    override fun onPeriodDate(diff: Long) {
        tvPeriodDate.text = diff.toString()
    }

}
