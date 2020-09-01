package com.adedom.calendar.date

import android.content.Context

class SimpleMonthAdapter(
    context: Context?, controller: DatePickerController?
) : MonthAdapter(context, controller) {

    override fun createMonthView(context: Context?): MonthView {
        return SimpleMonthView(context, null, mController)
    }

}
