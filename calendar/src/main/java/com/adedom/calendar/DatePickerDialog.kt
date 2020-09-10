package com.adedom.calendar

import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import java.text.SimpleDateFormat
import java.util.*

class DatePickerDialog : DialogFragment(), DatePickerController {

    private val mCalendar = Calendar.getInstance()
    private lateinit var mInitializeDate: DateItem
    private lateinit var mCallBack: OnDateSetListener
    private var mLocale: Locale = LOCALE_EN
    private val mListeners = HashSet<OnDateChangedListener>()

    private lateinit var mAnimator: AccessibleDateAnimator

    private lateinit var mTvFullDate: TextView
    private lateinit var mMonthAndDayView: LinearLayout
    private lateinit var mSelectedMonthTextView: TextView
    private lateinit var mYearView: TextView
    private var mDayPickerView: DayPickerView? = null
    private lateinit var mYearPickerView: YearPickerView

    private var mCurrentView = UNINITIALIZED

    private var mAccentColor = -1

    private var mDelayAnimation = true

    // Accessibility strings.
    private lateinit var mDayPickerDescription: String
    private lateinit var mSelectDay: String
    private lateinit var mYearPickerDescription: String
    private lateinit var mSelectYear: String

    private var mIsFullDateVisibility = true
    private val highlightedDays: Array<Calendar>? = null
    private val selectableDays: Array<Calendar>? = null
    private var mMinDate: Calendar? = null
    private var mMaxDate: Calendar? = null

    interface OnDateSetListener {
        fun onDateSet(year: Int, monthOfYear: Int, dayOfMonth: Int)
        fun onPeriodDate(diff: Long)
    }

    interface OnDateChangedListener {
        fun onDateChanged()
    }

    private fun initialize(
        callBack: OnDateSetListener,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ) {
        mCallBack = callBack
        mCalendar.set(Calendar.YEAR, year)
        mCalendar.set(Calendar.MONTH, monthOfYear)
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        mInitializeDate = DateItem(year, monthOfYear, dayOfMonth)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: Activity? = activity
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        mCurrentView = UNINITIALIZED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        val view: View = inflater.inflate(R.layout.calendar_date_picker_dialog, container)

        mTvFullDate = view.findViewById<TextView>(R.id.tv_full_date).apply {
            visibility = if (mIsFullDateVisibility) View.VISIBLE else View.GONE
        }

        mMonthAndDayView = view.findViewById<LinearLayout>(R.id.date_picker_month_and_day).apply {
            setOnClickListener { setCurrentView(MONTH_AND_DAY_VIEW) }
        }

        mSelectedMonthTextView = view.findViewById(R.id.date_picker_month) as TextView

        mYearView = view.findViewById<TextView>(R.id.date_picker_year).apply {
            setOnClickListener { setCurrentView(YEAR_VIEW) }
        }

        activity?.let {
            mDayPickerView = SimpleDayPickerView(it, this, mLocale)
            mYearPickerView = YearPickerView(it, this, mLocale)

            if (mAccentColor == -1) {
                mAccentColor = Utils.getAccentColorFromThemeIfAvailable(it)
            }

            val bgColorResource: Int = R.color.calendar_date_picker_view_animator
            val color = ContextCompat.getColor(it, bgColorResource)
            view.setBackgroundColor(color)
        }

        mDayPickerDescription = resources.getString(R.string.calendar_day_picker_description)
        mSelectDay = resources.getString(R.string.calendar_select_day)
        mYearPickerDescription = resources.getString(R.string.calendar_year_picker_description)
        mSelectYear = resources.getString(R.string.calendar_select_year)

        mAnimator = view.findViewById<AccessibleDateAnimator>(R.id.animator).apply {
            addView(mDayPickerView)
            addView(mYearPickerView)
            setDateMillis(mCalendar.timeInMillis)

            val animation: Animation = AlphaAnimation(0.0f, 1.0f)
            animation.duration = ANIMATION_DURATION
            inAnimation = animation

            val animation2: Animation = AlphaAnimation(1.0f, 0.0f)
            animation2.duration = ANIMATION_DURATION
            outAnimation = animation2
        }

        view.findViewById<Button>(R.id.ok).apply {
            setOnClickListener {
                val selectDate = DateUtil.getDatePicker(mLocale, mCalendar)
                mCallBack.onDateSet(selectDate.year, selectDate.monthOfYear, selectDate.dayOfMonth)

                val initialize = DateItem(
                    mInitializeDate.year,
                    mInitializeDate.monthOfYear,
                    mInitializeDate.dayOfMonth
                )
                val dateDiff = DateUtil.getPeriodDateDiff(mLocale, initialize, selectDate)
                mCallBack.onPeriodDate(dateDiff)

                dismiss()
            }
            typeface = TypefaceHelper[activity, "Roboto-Medium"]
            setTextColor(mAccentColor)
            text = DateUtil.getTextOkFromLocale(context, mLocale)
        }

        view.findViewById<Button>(R.id.cancel).apply {
            setOnClickListener {
                dialog?.cancel()
            }
            typeface = TypefaceHelper[activity, "Roboto-Medium"]
            setTextColor(mAccentColor)
            text = DateUtil.getTextCancelFromLocale(context, mLocale)
        }

        view.findViewById<LinearLayout>(R.id.day_picker_selected_date_layout).apply {
            setBackgroundColor(mAccentColor)
        }

        updateDisplay(false)
        setCurrentView(MONTH_AND_DAY_VIEW)

        return view
    }

    private fun setCurrentView(viewIndex: Int) {
        val millis = mCalendar.timeInMillis

        val pulseAnimator: ObjectAnimator
        when (viewIndex) {
            MONTH_AND_DAY_VIEW -> {
                pulseAnimator = Utils.getPulseAnimator(mMonthAndDayView, 0.9f, 1.05f)
                if (mDelayAnimation) {
                    pulseAnimator.startDelay = ANIMATION_DELAY
                    mDelayAnimation = false
                }
                mDayPickerView?.onDateChanged()
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.isSelected = true
                    mYearView.isSelected = false
                    mAnimator.displayedChild = MONTH_AND_DAY_VIEW
                    mCurrentView = viewIndex
                }
                pulseAnimator.start()

                val flags = DateUtils.FORMAT_SHOW_DATE
                val dayString = DateUtils.formatDateTime(activity, millis, flags)
                mAnimator.contentDescription = "$mDayPickerDescription: $dayString"
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectDay)
            }
            YEAR_VIEW -> {
                pulseAnimator = Utils.getPulseAnimator(mYearView, 0.85f, 1.1f)
                if (mDelayAnimation) {
                    pulseAnimator.startDelay = ANIMATION_DELAY
                    mDelayAnimation = false
                }
                mYearPickerView.onDateChanged()
                if (mCurrentView != viewIndex) {
                    mMonthAndDayView.isSelected = false
                    mYearView.isSelected = true
                    mAnimator.displayedChild = YEAR_VIEW
                    mCurrentView = viewIndex
                }
                pulseAnimator.start()

                val yearString: CharSequence =
                    SimpleDateFormat(DateUtil.YEAR_FORMAT, mLocale).format(millis)
                mAnimator.contentDescription = "$mYearPickerDescription: $yearString"
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear)
            }
        }
    }

    private fun updateDisplay(announce: Boolean) {
        val fullDate = SimpleDateFormat(DateUtil.FULL_DATE_FORMAT, mLocale).format(mCalendar.time)
            .replace(".", "") + DateUtil.getLocaleYear(mLocale, mCalendar)
        mTvFullDate.text = fullDate
        mSelectedMonthTextView.text =
            SimpleDateFormat(DateUtil.MONTH_FORMAT, mLocale).format(mCalendar.time)
        mYearView.text = DateUtil.getLocaleYear(mLocale, mCalendar)

        // Accessibility.
        val millis = mCalendar.timeInMillis
        mAnimator.setDateMillis(millis)
        var flags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_YEAR
        val monthAndDayText = DateUtils.formatDateTime(activity, millis, flags)
        mMonthAndDayView.contentDescription = monthAndDayText

        if (announce) {
            flags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
            val fullDateText = DateUtils.formatDateTime(activity, millis, flags)
            Utils.tryAccessibilityAnnounce(mAnimator, fullDateText)
        }
    }

    fun setLocale(locale: Locale) {
        mLocale = locale
    }

    fun setAccentColor(@ColorInt color: Int) {
        mAccentColor = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color))
    }

    fun setTitleLabelFullDate(visibility: Boolean) {
        mIsFullDateVisibility = visibility
    }

    fun setMinDate(calendar: Calendar) {
        mMinDate = calendar
        mDayPickerView?.onChange()
    }

    fun setMaxDate(calendar: Calendar) {
        mMaxDate = calendar
        mDayPickerView?.onChange()
    }

    override fun getAccentColor() = mAccentColor

    override fun getHighlightedDays(): Array<Calendar> {
        return highlightedDays ?: arrayOf(Calendar.getInstance())
    }

    override fun getSelectableDays(): Array<Calendar> {
        return selectableDays ?: arrayOf(Calendar.getInstance())
    }

    override fun onYearSelected(year: Int) {
        mCalendar[Calendar.YEAR] = year

        DateUtil.adjustDayInMonthIfNeeded(mCalendar)
        if (mMinDate != null && mMaxDate != null)
            DateUtil.setToNearestDate(selectableDays, mMinDate!!, mMaxDate!!, mCalendar)
        for (listener in mListeners) listener.onDateChanged()

        setCurrentView(MONTH_AND_DAY_VIEW)
        updateDisplay(true)
    }

    override fun onDayOfMonthSelected(year: Int, month: Int, day: Int) {
        mCalendar[Calendar.YEAR] = year
        mCalendar[Calendar.MONTH] = month
        mCalendar[Calendar.DAY_OF_MONTH] = day
        for (listener in mListeners) listener.onDateChanged()
        updateDisplay(true)
    }

    override fun getSelectedDay() = MonthAdapter.CalendarDay(mCalendar)

    override fun getMinYear(): Int {
        if (selectableDays != null) return selectableDays[0].get(Calendar.YEAR)
        return if (mMinDate != null && mMinDate!![Calendar.YEAR] > DEFAULT_START_YEAR) mMinDate!![Calendar.YEAR] else DEFAULT_START_YEAR
    }

    override fun getMaxYear(): Int {
        if (selectableDays != null) return selectableDays[selectableDays.size - 1][Calendar.YEAR]
        return if (mMaxDate != null && mMaxDate!![Calendar.YEAR] < DEFAULT_END_YEAR) mMaxDate!![Calendar.YEAR] else DEFAULT_END_YEAR
    }

    override fun isOutOfRange(year: Int, month: Int, day: Int): Boolean {
        if (selectableDays != null) {
            return !DateUtil.isSelectable(selectableDays, year, month, day)
        }

        if (DateUtil.isBeforeMin(mMinDate, year, month, day)) {
            return true
        } else if (DateUtil.isAfterMax(mMaxDate, year, month, day)) {
            return true
        }

        return false
    }

    override fun getFirstDayOfWeek() = mCalendar.firstDayOfWeek

    override fun registerOnDateChangedListener(listener: OnDateChangedListener) {
        mListeners.add(listener)
    }

    override fun unregisterOnDateChangedListener(listener: OnDateChangedListener) {
        mListeners.remove(listener)
    }

    companion object {
        private const val UNINITIALIZED = -1
        private const val MONTH_AND_DAY_VIEW = 0
        private const val YEAR_VIEW = 1

        private const val DEFAULT_START_YEAR = 1900
        private const val DEFAULT_END_YEAR = 2100

        private const val ANIMATION_DURATION: Long = 300
        private const val ANIMATION_DELAY: Long = 500

        val LOCALE_EN = Locale("en", "EN")
        val LOCALE_TH = Locale("th", "TH")

        fun newInstance(
            callback: OnDateSetListener,
            year: Int,
            monthOfYear: Int,
            dayOfMonth: Int,
        ): DatePickerDialog {
            val ret = DatePickerDialog()
            ret.initialize(callback, year, monthOfYear, dayOfMonth)
            return ret
        }
    }

}
