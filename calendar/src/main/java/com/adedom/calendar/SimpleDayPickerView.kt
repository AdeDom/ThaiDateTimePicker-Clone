package com.adedom.calendar

import android.content.Context
import android.util.AttributeSet
import java.util.*

internal class SimpleDayPickerView : DayPickerView {

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, controller: DatePickerController?, locale: Locale) : super(context, controller, locale)

    override fun createMonthAdapter(
        context: Context?,
        controller: DatePickerController?,
        locale: Locale
    ): MonthAdapter {
        return SimpleMonthAdapter(context, controller, locale)
    }

}
