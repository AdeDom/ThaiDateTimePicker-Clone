package com.adedom.calendar.date

import android.content.Context
import android.util.AttributeSet

class SimpleDayPickerView : DayPickerView {

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, controller: DatePickerController?) : super(context, controller)

    override fun createMonthAdapter(
        context: Context?,
        controller: DatePickerController?
    ): MonthAdapter {
        return SimpleMonthAdapter(context, controller)
    }

}
