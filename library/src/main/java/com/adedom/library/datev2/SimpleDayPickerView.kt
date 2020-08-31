package com.adedom.library.datev2

import android.content.Context
import android.util.AttributeSet
import com.adedom.library.date.DatePickerController
import com.adedom.library.date.DayPickerView
import com.adedom.library.date.MonthAdapter

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
