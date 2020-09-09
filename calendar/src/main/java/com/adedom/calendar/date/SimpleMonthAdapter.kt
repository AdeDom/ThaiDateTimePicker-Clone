package com.adedom.calendar.date

import android.content.Context
import java.util.*

internal class SimpleMonthAdapter(
    context: Context?, controller: DatePickerController?, private val mLocale: Locale
) : MonthAdapter(context, controller) {

    override fun createMonthView(context: Context?): MonthView {
        return SimpleMonthView(context, null, mController, mLocale)
    }

}
