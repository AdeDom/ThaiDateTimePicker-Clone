package com.adedom.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import java.util.*

abstract class MonthAdapter(
    private val mContext: Context?,
    protected val mController: DatePickerController
) : BaseAdapter(), MonthView.OnDayClickListener {

    private var mSelectedDay: CalendarDay? = null

    fun setSelectedDay(day: CalendarDay?) {
        mSelectedDay = day
        notifyDataSetChanged()
    }

    fun getSelectedDay(): CalendarDay? {
        return mSelectedDay
    }

    protected fun init() {
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
        val v: MonthView
        var drawingParams: HashMap<String, Int>? = null
        if (convertView != null) {
            v = convertView as MonthView
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
            selectedDay = mSelectedDay!!.day
        }

        v.reuse()

        drawingParams[MonthView.VIEW_PARAMS_SELECTED_DAY] = selectedDay
        drawingParams[MonthView.VIEW_PARAMS_YEAR] = year
        drawingParams[MonthView.VIEW_PARAMS_MONTH] = month
        drawingParams[MonthView.VIEW_PARAMS_WEEK_START] = mController.getFirstDayOfWeek()
        v.setMonthParams(drawingParams)
        v.invalidate()
        return v
    }

    abstract fun createMonthView(context: Context?): MonthView

    private fun isSelectedDayInMonth(year: Int, month: Int): Boolean {
        return mSelectedDay?.year == year && mSelectedDay?.month == month
    }

    override fun onDayClick(view: MonthView?, day: CalendarDay?) {
        day?.let { onDayTapped(it) }
    }

    protected fun onDayTapped(day: CalendarDay?) {
        if (day != null)
            mController.onDayOfMonthSelected(day.year, day.month, day.day)
        setSelectedDay(day)
    }

    companion object {
        private const val TAG = "SimpleMonthAdapter"
        protected var WEEK_7_OVERHANG_HEIGHT = 7
        const val MONTHS_IN_YEAR = 12

        class CalendarDay {
            private var calendar: Calendar? = null
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

            fun setDay(year: Int, month: Int, day: Int) {
                this.year = year
                this.month = month
                this.day = day
            }

            private fun setTime(timeInMillis: Long) {
                if (calendar == null) {
                    calendar = Calendar.getInstance()
                }
                calendar?.timeInMillis = timeInMillis
                month = calendar!![Calendar.MONTH]
                year = calendar!![Calendar.YEAR]
                day = calendar!![Calendar.DAY_OF_MONTH]
            }
        }
    }

    init {
        init()
        setSelectedDay(mController.getSelectedDay())
    }
}
