package com.adedom.library.datev2

import android.content.Context
import com.adedom.library.date.DatePickerController
import com.adedom.library.date.MonthAdapter
import com.adedom.library.date.MonthView
import com.adedom.library.date.SimpleMonthView

class SimpleMonthAdapter(
    context: Context?, controller: DatePickerController?
) : MonthAdapter(context, controller) {

    override fun createMonthView(context: Context?): MonthView {
        return SimpleMonthView(context, null, mController)
    }

}
