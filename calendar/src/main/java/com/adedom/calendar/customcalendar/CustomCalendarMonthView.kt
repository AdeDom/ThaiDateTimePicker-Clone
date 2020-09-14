package com.adedom.calendar.customcalendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper
import com.adedom.calendar.R
import com.adedom.calendar.customcalendar.CustomCalendarUtil.getDayLabel
import com.adedom.calendar.customcalendar.CustomCalendarUtil.getMonthAndYear
import com.adedom.calendar.customcalendar.CustomCalendarMonthAdapter.Companion.CalendarDay
import java.security.InvalidParameterException
import java.util.*

abstract class CustomCalendarMonthView : View {

    protected var mController: CustomCalendarPickerController
    private var mLocale: Locale

    protected var mEdgePadding = 0

    private var mDayOfWeekTypeface: String? = null
    private var mMonthTitleTypeface: String? = null

    protected lateinit var mMonthNumPaint: Paint
    protected lateinit var mMonthTitlePaint: Paint
    internal lateinit var mSelectedCirclePaint: Paint
    protected lateinit var mMonthDayLabelPaint: Paint

    private var mFormatter: Formatter? = null
    private var mStringBuilder: StringBuilder? = null

    protected var mFirstJulianDay = -1
    protected var mFirstMonth = -1
    protected var mLastMonth = -1
    protected var mMonth = 0
    protected var mYear = 0
    protected var mWidth = 0
    protected var mRowHeight = DEFAULT_HEIGHT
    protected var mHasToday = false
    protected var mSelectedDay = -1
    protected var mToday = DEFAULT_SELECTED_DAY
    protected var mWeekStart = DEFAULT_WEEK_START
    protected var mNumDays = DEFAULT_NUM_DAYS
    protected var mNumCells = mNumDays
    protected var mSelectedLeft = -1
    protected var mSelectedRight = -1

    private var mCalendar: Calendar
    protected var mDayLabelCalendar: Calendar
    private var mTouchHelper: MonthViewTouchHelper

    protected var mNumRows = DEFAULT_NUM_ROWS

    protected var mOnDayClickListener: OnDayClickListener? = null

    private var mLockAccessibilityDelegate = false

    protected var mDayTextColor = 0
    protected var mSelectedDayTextColor = 0
    protected var mMonthDayTextColor = 0
    protected var mTodayNumberColor = 0
    protected var mHighlightedDayTextColor = 0
    protected var mDisabledDayTextColor = 0
    protected var mMonthTitleColor = 0

    constructor(
        context: Context?,
        attr: AttributeSet?,
        controller: CustomCalendarPickerController,
        locale: Locale,
    ) : super(context, attr) {
        mLocale = locale

        mController = controller
        val res = context?.resources

        mDayLabelCalendar = Calendar.getInstance()
        mCalendar = Calendar.getInstance()

        mDayOfWeekTypeface = res?.getString(R.string.custom_calendar_day_of_week_label_typeface)
        mMonthTitleTypeface = res?.getString(R.string.custom_calendar_sans_serif)

        context?.let {
            mDayTextColor = ContextCompat.getColor(it, R.color.custom_calendar_date_picker_text_normal)
            mMonthDayTextColor = ContextCompat.getColor(it, R.color.custom_calendar_date_picker_month_day)
            mDisabledDayTextColor =
                ContextCompat.getColor(it, R.color.custom_calendar_date_picker_text_disabled)
            mHighlightedDayTextColor =
                ContextCompat.getColor(it, R.color.custom_calendar_date_picker_text_highlighted)

            mSelectedDayTextColor = ContextCompat.getColor(it, R.color.custom_calendar_white)
            mTodayNumberColor = mController.getAccentColor()
            mMonthTitleColor = ContextCompat.getColor(it, R.color.custom_calendar_white)
        }

        context?.resources?.let {
            MINI_DAY_NUMBER_TEXT_SIZE = it.getDimensionPixelSize(R.dimen.custom_calendar_day_number_size)
            MONTH_LABEL_TEXT_SIZE = it.getDimensionPixelSize(R.dimen.custom_calendar_month_label_size)
            MONTH_DAY_LABEL_TEXT_SIZE =
                it.getDimensionPixelSize(R.dimen.custom_calendar_month_day_label_text_size)
            MONTH_HEADER_SIZE =
                it.getDimensionPixelOffset(R.dimen.custom_calendar_month_list_item_header_height)
            DAY_SELECTED_CIRCLE_SIZE =
                it.getDimensionPixelSize(R.dimen.custom_calendar_day_number_select_circle_radius)

            mRowHeight =
                (it.getDimensionPixelOffset(R.dimen.custom_calendar_date_picker_view_animator_height) - getMonthHeaderSize()) / MAX_NUM_ROWS
        }

        mStringBuilder = StringBuilder(50)
        mFormatter = Formatter(mStringBuilder, Locale.getDefault())

        mTouchHelper = getMonthViewTouchHelper()
        ViewCompat.setAccessibilityDelegate(this, mTouchHelper)
        ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES)
        mLockAccessibilityDelegate = true

        initView()
    }

    fun setDatePickerController(controller: CustomCalendarPickerController) {
        mController = controller
    }

    protected fun getMonthViewTouchHelper(): MonthViewTouchHelper {
        return MonthViewTouchHelper(this)
    }

    override fun setAccessibilityDelegate(delegate: AccessibilityDelegate?) {
        if (!mLockAccessibilityDelegate) {
            super.setAccessibilityDelegate(delegate)
        }
    }

    fun setOnDayClickListener(listener: OnDayClickListener?) {
        mOnDayClickListener = listener
    }

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        return if (mTouchHelper.dispatchHoverEvent(event)) {
            true
        } else super.dispatchHoverEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                val day = getDayFromLocation(event.x, event.y)
                if (day >= 0) {
                    onDayClick(day)
                }
            }
        }
        return true
    }

    protected fun initView() {
        mMonthTitlePaint = Paint()
        mMonthTitlePaint.isFakeBoldText = true
        mMonthTitlePaint.isAntiAlias = true
        mMonthTitlePaint.textSize = MONTH_LABEL_TEXT_SIZE.toFloat()
        mMonthTitlePaint.typeface = Typeface.create(mMonthTitleTypeface, Typeface.BOLD)
        mMonthTitlePaint.color = mDayTextColor
        mMonthTitlePaint.textAlign = Align.CENTER
        mMonthTitlePaint.style = Paint.Style.FILL

        mSelectedCirclePaint = Paint()
        mSelectedCirclePaint.isFakeBoldText = true
        mSelectedCirclePaint.isAntiAlias = true
        mSelectedCirclePaint.color = mTodayNumberColor
        mSelectedCirclePaint.textAlign = Align.CENTER
        mSelectedCirclePaint.style = Paint.Style.FILL
        mSelectedCirclePaint.alpha = SELECTED_CIRCLE_ALPHA

        mMonthDayLabelPaint = Paint()
        mMonthDayLabelPaint.isAntiAlias = true
        mMonthDayLabelPaint.textSize = MONTH_DAY_LABEL_TEXT_SIZE.toFloat()
        mMonthDayLabelPaint.color = mMonthDayTextColor
        mMonthDayLabelPaint.typeface = CustomCalendarUtil[context, "Roboto-Medium"]
        mMonthDayLabelPaint.style = Paint.Style.FILL
        mMonthDayLabelPaint.textAlign = Align.CENTER
        mMonthDayLabelPaint.isFakeBoldText = true

        mMonthNumPaint = Paint()
        mMonthNumPaint.isAntiAlias = true
        mMonthNumPaint.textSize = MINI_DAY_NUMBER_TEXT_SIZE.toFloat()
        mMonthNumPaint.style = Paint.Style.FILL
        mMonthNumPaint.textAlign = Align.CENTER
        mMonthNumPaint.isFakeBoldText = false
    }

    override fun onDraw(canvas: Canvas) {
        drawMonthTitle(canvas)
        drawMonthDayLabels(canvas)
        drawMonthNums(canvas)
    }

    private var mDayOfWeekStart = 0

    fun setMonthParams(params: HashMap<String, Int>) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw InvalidParameterException("You must specify month and year for this view")
        }
        tag = params

        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mRowHeight = params[VIEW_PARAMS_HEIGHT]!!
            if (mRowHeight < MIN_HEIGHT) {
                mRowHeight = MIN_HEIGHT
            }
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_DAY)) {
            mSelectedDay = params[VIEW_PARAMS_SELECTED_DAY]!!
        }

        mMonth = params[VIEW_PARAMS_MONTH]!!
        mYear = params[VIEW_PARAMS_YEAR]!!

        val today = Calendar.getInstance()
        mHasToday = false
        mToday = -1

        mCalendar[Calendar.MONTH] = mMonth
        mCalendar[Calendar.YEAR] = mYear
        mCalendar[Calendar.DAY_OF_MONTH] = 1
        mDayOfWeekStart = mCalendar[Calendar.DAY_OF_WEEK]

        mWeekStart = if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            params[VIEW_PARAMS_WEEK_START]!!
        } else {
            mCalendar.firstDayOfWeek
        }

        mNumCells = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 0 until mNumCells) {
            val day = i + 1
            if (sameDay(day, today)) {
                mHasToday = true
                mToday = day
            }
        }
        mNumRows = calculateNumRows()

        mTouchHelper.invalidateRoot()
    }

    fun setSelectedDay(day: Int) {
        mSelectedDay = day
    }

    fun reuse() {
        mNumRows = DEFAULT_NUM_ROWS
        requestLayout()
    }

    private fun calculateNumRows(): Int {
        val offset = findDayOffset()
        val dividend = (offset + mNumCells) / mNumDays
        val remainder = (offset + mNumCells) % mNumDays
        return dividend + if (remainder > 0) 1 else 0
    }

    private fun sameDay(day: Int, today: Calendar): Boolean {
        return mYear == today[Calendar.YEAR] &&
                mMonth == today[Calendar.MONTH] &&
                day == today[Calendar.DAY_OF_MONTH]
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            mRowHeight * mNumRows + getMonthHeaderSize() + 5
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = w

        mTouchHelper.invalidateRoot()
    }

    fun getMonth(): Int {
        return mMonth
    }

    fun getYear(): Int {
        return mYear
    }

    protected fun getMonthHeaderSize(): Int {
        return MONTH_HEADER_SIZE
    }

    private fun getMonthAndYearString(): String {
        return getMonthAndYear(mCalendar, mLocale)
    }

    protected fun drawMonthTitle(canvas: Canvas) {
        val x = (mWidth + 2 * mEdgePadding) / 2
        val y = (getMonthHeaderSize() - MONTH_DAY_LABEL_TEXT_SIZE) / 2
        canvas.drawText(getMonthAndYearString(), x.toFloat(), y.toFloat(), mMonthTitlePaint)
    }

    protected fun drawMonthDayLabels(canvas: Canvas) {
        val y = getMonthHeaderSize() - (MONTH_DAY_LABEL_TEXT_SIZE / 2)
        val dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2)

        for (i in 0 until mNumDays) {
            val x = (2 * i + 1) * dayWidthHalf + mEdgePadding

            val calendarDay = (i + mWeekStart) % mNumDays
            mDayLabelCalendar[Calendar.DAY_OF_WEEK] = calendarDay

            val dayLabel = getDayLabel(mDayLabelCalendar, mLocale)
            canvas.drawText(dayLabel, x.toFloat(), y.toFloat(), mMonthDayLabelPaint)
        }
    }

    protected fun drawMonthNums(canvas: Canvas?) {
        var y =
            (((mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2) - DAY_SEPARATOR_WIDTH) + getMonthHeaderSize()
        val dayWidthHalf = (mWidth - mEdgePadding * 2) / (mNumDays * 2.0f)
        var j = findDayOffset()
        for (dayNumber in 1..mNumCells) {
            val x = ((2 * j + 1) * dayWidthHalf + mEdgePadding).toInt()

            val yRelativeToDay = (mRowHeight + MINI_DAY_NUMBER_TEXT_SIZE) / 2 - DAY_SEPARATOR_WIDTH

            val startX = (x - dayWidthHalf).toInt()
            val stopX = (x + dayWidthHalf).toInt()
            val startY = y - yRelativeToDay
            val stopY = startY + mRowHeight

            drawMonthDay(
                canvas,
                mYear,
                mMonth,
                dayNumber,
                x,
                y,
                startX,
                stopX,
                startY,
                stopY
            )

            j++
            if (j == mNumDays) {
                j = 0
                y += mRowHeight
            }
        }
    }

    abstract fun drawMonthDay(
        canvas: Canvas?, year: Int, month: Int, day: Int,
        x: Int, y: Int, startX: Int, stopX: Int, startY: Int, stopY: Int
    )

    protected fun findDayOffset(): Int {
        return (if (mDayOfWeekStart < mWeekStart) mDayOfWeekStart + mNumDays else mDayOfWeekStart) - mWeekStart
    }

    fun getDayFromLocation(x: Float, y: Float): Int {
        val day = getInternalDayFromLocation(x, y)
        return if (day < 1 || day > mNumCells) -1 else day
    }

    protected fun getInternalDayFromLocation(x: Float, y: Float): Int {
        val dayStart = mEdgePadding
        if (x < dayStart || x > mWidth - mEdgePadding) {
            return -1
        }

        val row = (y - getMonthHeaderSize()).toInt() / mRowHeight
        val column = ((x - dayStart) * mNumDays / (mWidth - dayStart - mEdgePadding)).toInt()

        var day = column - findDayOffset() + 1
        day += row * mNumDays
        return day
    }

    private fun onDayClick(day: Int) {
        if (mController.isOutOfRange(mYear, mMonth, day)) {
            return
        }

        if (mOnDayClickListener != null) {
            mOnDayClickListener?.onDayClick(
                this,
                CalendarDay(mYear, mMonth, day)
            )
        }

        mTouchHelper.sendEventForVirtualView(day, AccessibilityEvent.TYPE_VIEW_CLICKED)
    }

    protected fun isHighlighted(year: Int, month: Int, day: Int): Boolean {
        val highlightedDays = mController.getHighlightedDays() ?: return false
        for (c in highlightedDays) {
            if (year < c[Calendar.YEAR]) break
            if (year > c[Calendar.YEAR]) continue
            if (month < c[Calendar.MONTH]) break
            if (month > c[Calendar.MONTH]) continue
            if (day < c[Calendar.DAY_OF_MONTH]) break
            if (day > c[Calendar.DAY_OF_MONTH]) continue
            return true
        }
        return false
    }

    fun getAccessibilityFocus(): CalendarDay? {
        val day = mTouchHelper.focusedVirtualView
        return if (day >= 0) {
            CalendarDay(mYear, mMonth, day)
        } else null
    }

    fun clearAccessibilityFocus() {
        mTouchHelper.clearFocusedVirtualView()
    }

    fun restoreAccessibilityFocus(day: CalendarDay): Boolean {
        if (day.year != mYear || day.month != mMonth || day.day > mNumCells) {
            return false
        }
        mTouchHelper.focusedVirtualView = day.day
        return true
    }

    protected inner class MonthViewTouchHelper : ExploreByTouchHelper {

        private val mTempRect = Rect()
        private val mTempCalendar = Calendar.getInstance()

        constructor(host: View) : super(host)

        fun setFocusedVirtualView(virtualViewId: Int) {
            getAccessibilityNodeProvider(this@CustomCalendarMonthView).performAction(
                virtualViewId,
                AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS,
                null
            )
        }

        fun clearFocusedVirtualView() {
            val focusedVirtualView = focusedVirtualView
            if (focusedVirtualView != INVALID_ID) {
                getAccessibilityNodeProvider(this@CustomCalendarMonthView).performAction(
                    focusedVirtualView,
                    AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS,
                    null
                )
            }
        }

        override fun getVirtualViewAt(x: Float, y: Float): Int {
            val day = getDayFromLocation(x, y)
            return if (day >= 0) day else INVALID_ID
        }

        override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
            for (day in 1..mNumCells) {
                virtualViewIds.add(day)
            }
        }

        override fun onPopulateEventForVirtualView(virtualViewId: Int, event: AccessibilityEvent) {
            event.contentDescription = getItemDescription(virtualViewId)
        }

        override fun onPopulateNodeForVirtualView(
            virtualViewId: Int,
            node: AccessibilityNodeInfoCompat
        ) {
            getItemBounds(virtualViewId, mTempRect)

            node.contentDescription = getItemDescription(virtualViewId)
            node.setBoundsInParent(mTempRect)
            node.addAction(AccessibilityNodeInfo.ACTION_CLICK)

            if (virtualViewId == mSelectedDay) {
                node.isSelected = true
            }
        }

        override fun onPerformActionForVirtualView(
            virtualViewId: Int,
            action: Int,
            arguments: Bundle?
        ): Boolean {
            when (action) {
                AccessibilityNodeInfo.ACTION_CLICK -> {
                    onDayClick(virtualViewId)
                    return true
                }
            }

            return false
        }

        protected fun getItemBounds(day: Int, rect: Rect) {
            val offsetX = mEdgePadding
            val offsetY = getMonthHeaderSize()
            val cellHeight = mRowHeight
            val cellWidth = (mWidth - (2 * mEdgePadding)) / mNumDays
            val index = (day - 1) + findDayOffset()
            val row = index / mNumDays
            val column = index % mNumDays
            val x = offsetX + (column * cellWidth)
            val y = offsetY + (row * cellHeight)

            rect[x, y, x + cellWidth] = y + cellHeight
        }

        protected fun getItemDescription(day: Int): CharSequence {
            mTempCalendar[mYear, mMonth] = day
            val date = DateFormat.format(DATE_FORMAT, mTempCalendar.timeInMillis)

            return if (day == mSelectedDay) {
                context.getString(R.string.custom_calendar_item_is_selected, date)
            } else date
        }

    }

    interface OnDayClickListener {
        fun onDayClick(view: CustomCalendarMonthView?, day: CalendarDay?)
    }

    companion object {
        private const val TAG = "MonthView"

        const val VIEW_PARAMS_HEIGHT = "height"

        const val VIEW_PARAMS_MONTH = "month"

        const val VIEW_PARAMS_YEAR = "year"

        const val VIEW_PARAMS_SELECTED_DAY = "selected_day"

        const val VIEW_PARAMS_WEEK_START = "week_start"

        const val VIEW_PARAMS_NUM_DAYS = "num_days"

        const val VIEW_PARAMS_FOCUS_MONTH = "focus_month"

        const val VIEW_PARAMS_SHOW_WK_NUM = "show_wk_num"

        protected var DEFAULT_HEIGHT = 32
        protected var MIN_HEIGHT = 10
        protected const val DEFAULT_SELECTED_DAY = -1
        protected const val DEFAULT_WEEK_START = Calendar.SUNDAY
        protected const val DEFAULT_NUM_DAYS = 7
        protected const val DEFAULT_SHOW_WK_NUM = 0
        protected const val DEFAULT_FOCUS_MONTH = -1
        protected const val DEFAULT_NUM_ROWS = 6
        protected const val MAX_NUM_ROWS = 6
        private const val SELECTED_CIRCLE_ALPHA = 255
        protected var DAY_SEPARATOR_WIDTH = 1

        internal var MINI_DAY_NUMBER_TEXT_SIZE = 0
        protected var MONTH_LABEL_TEXT_SIZE = 0
        protected var MONTH_DAY_LABEL_TEXT_SIZE = 0
        protected var MONTH_HEADER_SIZE = 0
        internal var DAY_SELECTED_CIRCLE_SIZE = 0
        protected var mScale = 0f

        private const val DATE_FORMAT = "dd MMMM yyyy"
    }

}
