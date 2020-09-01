package com.adedom.thaidatetimepicker_clone

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.adedom.calendar.date.chocoCardCalendarDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView.setOnClickListener {
            chocoCardCalendarDialog {
                Toast.makeText(baseContext, it, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
