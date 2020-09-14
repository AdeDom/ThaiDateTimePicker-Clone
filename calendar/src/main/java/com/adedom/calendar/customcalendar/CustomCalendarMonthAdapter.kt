package com.adedom.calendar.customcalendar

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import java.util.*

abstract class CustomCalendarMonthAdapter(
    private val mContext: Context?,
    protected val mController: CustomCalendarPickerController
) : BaseAdapter(), CustomCalendarMonthView.OnDayClickListener {

    private lateinit var mSelectedDay: CalendarDay

    fun setSelectedDay(day: CalendarDay) {
        mSelectedDay = day
        notifyDataSetChanged()
    }

    fun getSelectedDay(): CalendarDay {
        return mSelectedDay
    }

    private fun init() {
        mSelectedDay = CalendarDay(System.currentTimeMillis())
    }

    override fun getCount(): Int {
        return ((mController.getMaxYear() - mController.getMinYear()) + 1) * MONTHS_IN_YEAR
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    @SuppressLint("NewApi")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v: CustomCalendarMonthView
        var drawingParams: HashMap<String, Int>? = null
        if (convertView != null) {
            v = convertView as CustomCalendarMonthView
            drawingParams = v.tag as HashMap<String, Int>
        } else {
            v = createMonthView(mContext)
            val params = AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT,
                AbsListView.LayoutParams.MATCH_PARENT
            )
            v.layoutParams = params
            v.isClickable = true
            v.setOnDayClickListener(this)
        }
        if (drawingParams == null) {
            drawingParams = HashMap()
        }
        drawingParams.clear()

        val month = position % MONTHS_IN_YEAR
        val year = position / MONTHS_IN_YEAR + mController.getMinYear()

        var selectedDay = -1
        if (isSelectedDayInMonth(year, month)) {
            selectedDay = mSelectedDay.day
        }

        v.reuse()

        drawingParams[CustomCalendarMonthView.VIEW_PARAMS_SELECTED_DAY] = selectedDay
        drawingParams[CustomCalendarMonthView.VIEW_PARAMS_YEAR] = year
        drawingParams[CustomCalendarMonthView.VIEW_PARAMS_MONTH] = month
        drawingParams[CustomCalendarMonthView.VIEW_PARAMS_WEEK_START] = mController.getFirstDayOfWeek()
        v.setMonthParams(drawingParams)
        v.invalidate()
        return v
    }

    abstract fun createMonthView(context: Context?): CustomCalendarMonthView

    private fun isSelectedDayInMonth(year: Int, month: Int): Boolean {
        return mSelectedDay.year == year && mSelectedDay.month == month
    }

    override fun onDayClick(view: CustomCalendarMonthView?, day: CalendarDay?) {
        day?.let { onDayTapped(it) }
    }

    private fun onDayTapped(day: CalendarDay) {
        mController.onDayOfMonthSelected(day.year, day.month, day.day)
        setSelectedDay(day)
    }

    companion object {
        const val MONTHS_IN_YEAR = 12

        class CalendarDay {
            private lateinit var calendar: Calendar
            var year = 0
            var month = 0
            var day = 0

            constructor() {
                setTime(System.currentTimeMillis())
            }

            constructor(timeInMillis: Long) {
                setTime(timeInMillis)
            }

            constructor(calendar: Calendar) {
                year = calendar[Calendar.YEAR]
                month = calendar[Calendar.MONTH]
                day = calendar[Calendar.DAY_OF_MONTH]
            }

            constructor(year: Int, month: Int, day: Int) {
                setDay(year, month, day)
            }

            fun set(date: CalendarDay) {
                year = date.year
                month = date.month
                day = date.day
            }

            private fun setDay(year: Int, month: Int, day: Int) {
                this.year = year
                this.month = month
                this.day = day
            }

            private fun setTime(timeInMillis: Long) {
                calendar = Calendar.getInstance()
                calendar.timeInMillis = timeInMillis
                month = calendar[Calendar.MONTH]
                year = calendar[Calendar.YEAR]
                day = calendar[Calendar.DAY_OF_MONTH]
            }
        }
    }

    init {
        init()
        setSelectedDay(mController.getSelectedDay())
    }
}
