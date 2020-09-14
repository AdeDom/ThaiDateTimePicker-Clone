package com.adedom.thaidatetimepicker_clone

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adedom.calendar.customcalendar.CustomCalendarPickerDialog
import com.adedom.calendar.customcalendar.customCalendarPickerDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), CustomCalendarPickerDialog.OnDateSetListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btAnnoDomini.setOnClickListener {
            customCalendarPickerDialog(
                this,
                locale = CustomCalendarPickerDialog.LOCALE_EN,
            )
        }

        btBuddhistEra.setOnClickListener {
            customCalendarPickerDialog(
                this,
                locale = CustomCalendarPickerDialog.LOCALE_TH,
            )
        }

        btAccentColor.setOnClickListener {
            customCalendarPickerDialog(
                this,
                accentColor = Color.parseColor("#03DAC5"),
            )
        }

        btMinDate.setOnClickListener {
            customCalendarPickerDialog(
                this,
                minDate = Calendar.getInstance()
            )
        }

        btMaxDate.setOnClickListener {
            customCalendarPickerDialog(
                this,
                maxDate = Calendar.getInstance()
            )
        }

        btHideTitleLabelFullDate.setOnClickListener {
            customCalendarPickerDialog(
                this,
                isTitleLabelFullDate = false,
            )
        }

        btGoDateStartIntern.setOnClickListener {
            customCalendarPickerDialog(
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
