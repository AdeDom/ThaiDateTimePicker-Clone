package com.adedom.thaidatetimepicker_clone

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adedom.calendar.date.DatePickerDialog
import com.adedom.calendar.date.datePickerDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.setOnClickListener {
            datePickerDialog(object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(
                    view: DatePickerDialog,
                    year: Int,
                    monthOfYear: Int,
                    dayOfMonth: Int
                ) {
                    val date = "$dayOfMonth/${monthOfYear.plus(1)}/$year"
                    Toast.makeText(baseContext, date, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
