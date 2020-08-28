package com.adedom.thaidatetimepicker_clone

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adedom.library.date.DatePickerDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.setOnClickListener {
            val now: Calendar = Calendar.getInstance()
            val dpd = DatePickerDialog.newInstance(
                this@MainActivity,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
            )
            dpd.show(fragmentManager, "Datepickerdialog")
        }
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        Toast.makeText(baseContext, "$dayOfMonth/${monthOfYear + 1}/$year", Toast.LENGTH_SHORT)
            .show()
    }
}