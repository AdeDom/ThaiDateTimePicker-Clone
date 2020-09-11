package com.adedom.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.AbsListView
import android.widget.ListView
import com.adedom.calendar.DateUtil.Companion.tryAccessibilityAnnounce
import com.adedom.calendar.MonthAdapter.Companion.CalendarDay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

abstract class DayPickerView : ListView, AbsListView.OnScrollListener,
    DatePickerDialog.OnDateChangedListener {

    protected var mNumWeeks = 6
    protected var mShowWeekNumber = false
    protected var mDaysPerWeek = 7

    protected var mFriction = 1.0f
    protected var mContext: Context? = null
    protected var mHandler: Handler? = null

    protected var mSelectedDay = CalendarDay()
    protected var mAdapter: MonthAdapter? = null

    protected var mTempDay = CalendarDay()

    protected var mFirstDayOfWeek = 0

    protected var mPrevMonthName: CharSequence? = null

    protected var mCurrentMonthDisplayed = 0

    protected var mPreviousScrollPosition: Long = 0

    protected var mPreviousScrollState = OnScrollListener.SCROLL_STATE_IDLE

    protected var mCurrentScrollState = OnScrollListener.SCROLL_STATE_IDLE

    private lateinit var mController: DatePickerController
    private lateinit var mLocale: Locale
    private var mPerformingScroll = false

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    internal constructor(
        context: Context?, controller: DatePickerController, locale: Locale
    ) : super(context) {
        init(context)
        setController(controller, locale)
    }

    internal fun setController(controller: DatePickerController, locale: Locale) {
        mController = controller
        mLocale = locale
        mController.registerOnDateChangedListener(this)
        refreshAdapter()
        onDateChanged()
    }

    fun init(context: Context?) {
        mHandler = Handler()
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        isDrawSelectorOnTop = false

        mContext = context
        setUpListView()
    }

    fun onChange() {
        refreshAdapter()
    }

    protected fun refreshAdapter() {
        if (mAdapter == null) {
            mAdapter = createMonthAdapter(context, mController, mLocale)
        } else {
            mAdapter?.setSelectedDay(mSelectedDay)
        }
        adapter = mAdapter
    }

    internal abstract fun createMonthAdapter(
        context: Context?,
        controller: DatePickerController,
        locale: Locale,
    ): MonthAdapter

    protected fun setUpListView() {
        cacheColorHint = 0
        divider = null
        itemsCanFocus = true
        isFastScrollEnabled = false
        isVerticalScrollBarEnabled = false
        setOnScrollListener(this)
        setFadingEdgeLength(0)
        setFriction(ViewConfiguration.getScrollFriction() * mFriction)
    }

    fun goTo(
        day: CalendarDay,
        animate: Boolean,
        setSelected: Boolean,
        forceScroll: Boolean
    ): Boolean {

        if (setSelected) {
            mSelectedDay.set(day)
        }

        mTempDay.set(day)
        val position =
            (day.year - mController.getMinYear()) * MonthAdapter.MONTHS_IN_YEAR + day.month

        var child: View?
        var i = 0
        var top = 0
        do {
            child = getChildAt(i++)
            if (child == null) {
                break
            }
            top = child.top
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "child at " + (i - 1) + " has top " + top)
            }
        } while (top < 0)

        val selectedPosition: Int
        selectedPosition = child?.let { getPositionForView(it) } ?: 0

        if (setSelected) {
            mAdapter?.setSelectedDay(mSelectedDay)
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "GoTo position $position")
        }

        if (position != selectedPosition || forceScroll) {
            setMonthDisplayed(mTempDay)
            mPreviousScrollState = OnScrollListener.SCROLL_STATE_FLING
            if (animate) {
                smoothScrollToPositionFromTop(position, LIST_TOP_OFFSET, GOTO_SCROLL_DURATION)
                return true
            } else {
                postSetSelection(position)
            }
        } else if (setSelected) {
            setMonthDisplayed(mSelectedDay)
        }
        return false
    }

    fun postSetSelection(position: Int) {
        clearFocus()
        post { setSelection(position) }
        onScrollStateChanged(this, OnScrollListener.SCROLL_STATE_IDLE)
    }

    override fun onScroll(
        view: AbsListView,
        firstVisibleItem: Int,
        visibleItemCount: Int,
        totalItemCount: Int
    ) {
        val child = view.getChildAt(0) as MonthView? ?: return

        val currScroll = view.firstVisiblePosition * child.height - child.bottom.toLong()
        mPreviousScrollPosition = currScroll
        mPreviousScrollState = mCurrentScrollState
    }

    protected fun setMonthDisplayed(date: CalendarDay) {
        mCurrentMonthDisplayed = date.month
        invalidateViews()
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        mScrollStateChangedRunnable.doScrollStateChange(view, scrollState)
    }

    protected var mScrollStateChangedRunnable = ScrollStateRunnable()

    protected inner class ScrollStateRunnable : Runnable {
        private var mNewState = 0

        fun doScrollStateChange(view: AbsListView?, scrollState: Int) {
            mHandler?.removeCallbacks(this)
            mNewState = scrollState
            mHandler?.postDelayed(this, SCROLL_CHANGE_DELAY.toLong())
        }

        override fun run() {
            mCurrentScrollState = mNewState
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "new scroll state: $mNewState old state: $mPreviousScrollState")
            }

            if (mNewState == OnScrollListener.SCROLL_STATE_IDLE && mPreviousScrollState != OnScrollListener.SCROLL_STATE_IDLE && mPreviousScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                mPreviousScrollState = mNewState
                var i = 0
                var child = getChildAt(i)
                while (child != null && child.bottom <= 0) {
                    child = getChildAt(++i)
                }
                if (child == null) {
                    return
                }
                val firstPosition = firstVisiblePosition
                val lastPosition = lastVisiblePosition
                val scroll = firstPosition != 0 && lastPosition != count - 1
                val top = child.top
                val bottom = child.bottom
                val midpoint = height / 2
                if (scroll && top < LIST_TOP_OFFSET) {
                    if (bottom > midpoint) {
                        smoothScrollBy(top, GOTO_SCROLL_DURATION)
                    } else {
                        smoothScrollBy(bottom, GOTO_SCROLL_DURATION)
                    }
                }
            } else {
                mPreviousScrollState = mNewState
            }
        }
    }

    fun getMostVisiblePosition(): Int {
        val firstPosition = firstVisiblePosition
        val height = height

        var maxDisplayedHeight = 0
        var mostVisibleIndex = 0
        var i = 0
        var bottom = 0
        while (bottom < height) {
            val child = getChildAt(i) ?: break
            bottom = child.bottom
            val displayedHeight = min(bottom, height) - max(0, child.top)
            if (displayedHeight > maxDisplayedHeight) {
                mostVisibleIndex = i
                maxDisplayedHeight = displayedHeight
            }
            i++
        }
        return firstPosition + mostVisibleIndex
    }

    override fun onDateChanged() {
        goTo(
            mController.getSelectedDay(),
            animate = false,
            setSelected = true,
            forceScroll = true
        )
    }

    private fun findAccessibilityFocus(): CalendarDay? {
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is MonthView) {
                val focus = child.getAccessibilityFocus()
                if (focus != null) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        child.clearAccessibilityFocus()
                    }
                    return focus
                }
            }
        }

        return null
    }

    private fun restoreAccessibilityFocus(day: CalendarDay?): Boolean {
        if (day == null) {
            return false
        }

        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is MonthView) {
                if (child.restoreAccessibilityFocus(day)) {
                    return true
                }
            }
        }

        return false
    }

    override fun layoutChildren() {
        val focusedDay = findAccessibilityFocus()
        super.layoutChildren()
        if (mPerformingScroll) {
            mPerformingScroll = false
        } else {
            restoreAccessibilityFocus(focusedDay)
        }
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.itemCount = -1
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        if (Build.VERSION.SDK_INT >= 21) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD)
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)
        } else {
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        }
    }

    @SuppressLint("NewApi")
    override fun performAccessibilityAction(action: Int, arguments: Bundle): Boolean {
        if (action != AccessibilityNodeInfo.ACTION_SCROLL_FORWARD &&
            action != AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        ) {
            return super.performAccessibilityAction(action, arguments)
        }

        val firstVisiblePosition = firstVisiblePosition
        val month = firstVisiblePosition % 12
        val year = firstVisiblePosition / 12 + mController.getMinYear()
        val day = CalendarDay(year, month, 1)

        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) {
            day.month++
            if (day.month == 12) {
                day.month = 0
                day.year++
            }
        } else if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            val firstVisibleView = getChildAt(0)
            if (firstVisibleView != null && firstVisibleView.top >= -1) {
                day.month--
                if (day.month == -1) {
                    day.month = 11
                    day.year--
                }
            }
        }

        tryAccessibilityAnnounce(this, getMonthAndYearString(day))
        goTo(day, animate = true, setSelected = false, forceScroll = true)
        mPerformingScroll = true
        return true
    }

    companion object {
        private const val TAG = "MonthFragment"

        protected const val SCROLL_HYST_WEEKS = 2

        protected const val GOTO_SCROLL_DURATION = 250

        protected const val SCROLL_CHANGE_DELAY = 40

        const val DAYS_PER_WEEK = 7
        var LIST_TOP_OFFSET = -1

        private val YEAR_FORMAT = SimpleDateFormat("yyyy", Locale.getDefault())

        private fun getMonthAndYearString(day: CalendarDay): String {
            val cal = Calendar.getInstance()
            cal[day.year, day.month] = day.day
            var sbuf = ""
            sbuf += cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            sbuf += " "
            sbuf += YEAR_FORMAT.format(cal.time)
            return sbuf
        }
    }
}
