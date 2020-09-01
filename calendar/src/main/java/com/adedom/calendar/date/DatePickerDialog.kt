package com.adedom.calendar.date

import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.text.format.DateUtils
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.adedom.calendar.HapticFeedbackController
import com.adedom.calendar.R
import com.adedom.calendar.TypefaceHelper
import com.adedom.calendar.Utils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class DatePickerDialog : DialogFragment(), DatePickerController {

    private val mCalendar = Calendar.getInstance()
    private lateinit var mCallBack: OnDateSetListener
    private val mListeners = HashSet<OnDateChangedListener>()

    private lateinit var mAnimator: AccessibleDateAnimator

    private lateinit var mMonthAndDayView: LinearLayout
    private lateinit var mSelectedMonthTextView: TextView
    private lateinit var mYearView: TextView
    private lateinit var mDayPickerView: DayPickerView
    private lateinit var mYearPickerView: YearPickerView

    private var mCurrentView = UNINITIALIZED

    private var mWeekStart = mCalendar.firstDayOfWeek
    private var mMinYear = DEFAULT_START_YEAR
    private var mMaxYear = DEFAULT_END_YEAR
    private lateinit var mMinDate: Calendar
    private lateinit var mMaxDate: Calendar
    private lateinit var highlightedDays: Array<Calendar>
    private lateinit var selectableDays: Array<Calendar>
    private var mAccentColor = -1

    private var mHapticFeedbackController: HapticFeedbackController? = null

    private var mDelayAnimation = true

    // Accessibility strings.
    private lateinit var mDayPickerDescription: String
    private lateinit var mSelectDay: String
    private lateinit var mYearPickerDescription: String
    private lateinit var mSelectYear: String

    interface OnDateSetListener {
        fun onDateSet(view: DatePickerDialog, year: Int, monthOfYear: Int, dayOfMonth: Int)
    }

    interface OnDateChangedListener {
        fun onDateChanged()
    }

    fun initialize(
        callBack: OnDateSetListener,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ) {
        mCallBack = callBack
        mCalendar.set(Calendar.YEAR, year)
        mCalendar.set(Calendar.MONTH, monthOfYear)
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
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

        mMonthAndDayView = view.findViewById<View>(R.id.date_picker_month_and_day) as LinearLayout
        mSelectedMonthTextView = view.findViewById<View>(R.id.date_picker_month) as TextView
        mYearView = view.findViewById<View>(R.id.date_picker_year) as TextView

        mYearView.setOnClickListener { setCurrentView(YEAR_VIEW) }
        mMonthAndDayView.setOnClickListener { setCurrentView(MONTH_AND_DAY_VIEW) }

        var listPosition = -1

        val activity: Activity? = activity
        mDayPickerView = SimpleDayPickerView(activity, this)
        if (activity != null)
            mYearPickerView = YearPickerView(activity, this)

        val res = resources
        mDayPickerDescription = res.getString(R.string.mdtp_day_picker_description)
        mSelectDay = res.getString(R.string.mdtp_select_day)
        mYearPickerDescription = res.getString(R.string.mdtp_year_picker_description)
        mSelectYear = res.getString(R.string.mdtp_select_year)

        val bgColorResource: Int = R.color.mdtp_date_picker_view_animator
        val color = activity?.let { ContextCompat.getColor(it, bgColorResource) }
        color?.let { view.setBackgroundColor(it) }

        mAnimator = view.findViewById<View>(R.id.animator) as AccessibleDateAnimator
        mAnimator.addView(mDayPickerView)
        mAnimator.addView(mYearPickerView)
        mAnimator.setDateMillis(mCalendar.timeInMillis)
        // TODO: Replace with animation decided upon by the design team.
        val animation: Animation = AlphaAnimation(0.0f, 1.0f)
        animation.duration = ANIMATION_DURATION.toLong()
        mAnimator.inAnimation = animation
        // TODO: Replace with animation decided upon by the design team.
        val animation2: Animation = AlphaAnimation(1.0f, 0.0f)
        animation2.duration = ANIMATION_DURATION.toLong()
        mAnimator.outAnimation = animation2

        val okButton = view.findViewById<View>(R.id.ok) as Button
        okButton.setOnClickListener {
            mCallBack.onDateSet(
                this@DatePickerDialog, mCalendar[Calendar.YEAR],
                mCalendar[Calendar.MONTH], mCalendar[Calendar.DAY_OF_MONTH]
            )
            dismiss()
        }
        okButton.typeface = TypefaceHelper[activity, "Roboto-Medium"]

        val cancelButton = view.findViewById<View>(R.id.cancel) as Button
        cancelButton.setOnClickListener {
            if (dialog != null) dialog?.cancel()
        }
        cancelButton.typeface = TypefaceHelper[activity, "Roboto-Medium"]
        cancelButton.visibility = if (isCancelable) View.VISIBLE else View.GONE

        if (mAccentColor == -1) {
            mAccentColor = getActivity()?.let { Utils.getAccentColorFromThemeIfAvailable(it) } ?: 0
        }
        view.findViewById<View>(R.id.day_picker_selected_date_layout)
            .setBackgroundColor(mAccentColor)
        okButton.setTextColor(mAccentColor)
        cancelButton.setTextColor(mAccentColor)

        updateDisplay(false)
        setCurrentView(MONTH_AND_DAY_VIEW)

        if (listPosition != -1) {
            mDayPickerView.postSetSelection(listPosition)
        }

        mHapticFeedbackController = activity?.let { HapticFeedbackController(it) }
        return view
    }

    override fun onResume() {
        super.onResume()
        mHapticFeedbackController?.start()
    }

    override fun onPause() {
        super.onPause()
        mHapticFeedbackController?.stop()
        dismiss()
    }

    private fun setCurrentView(viewIndex: Int) {
        val millis = mCalendar.timeInMillis

        val pulseAnimator: ObjectAnimator
        when (viewIndex) {
            MONTH_AND_DAY_VIEW -> {
                pulseAnimator = Utils.getPulseAnimator(mMonthAndDayView, 0.9f, 1.05f)
                if (mDelayAnimation) {
                    pulseAnimator.startDelay = ANIMATION_DELAY.toLong()
                    mDelayAnimation = false
                }
                mDayPickerView.onDateChanged()
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
                    pulseAnimator.startDelay = ANIMATION_DELAY.toLong()
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

                val yearString: CharSequence = YEAR_FORMAT.format(millis)
                mAnimator.contentDescription = "$mYearPickerDescription: $yearString"
                Utils.tryAccessibilityAnnounce(mAnimator, mSelectYear)
            }
        }
    }

    private fun updateDisplay(announce: Boolean) {
        mSelectedMonthTextView.text = mCalendar.getDisplayName(
            Calendar.MONTH, Calendar.SHORT,
            Locale.getDefault()
        )?.toUpperCase(Locale.getDefault())
        mYearView.text = YEAR_FORMAT.format(mCalendar.time)

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

    fun setAccentColor(accentColor: Int) {
        mAccentColor = accentColor
    }

    override fun getAccentColor() = mAccentColor

    fun setHighlightedDays(highlightedDays: Array<Calendar>) {
        Arrays.sort(highlightedDays)
        this.highlightedDays = highlightedDays
    }

    override fun getHighlightedDays() = highlightedDays

    fun setSelectableDays(selectableDays: Array<Calendar>) {
        Arrays.sort(selectableDays)
        this.selectableDays = selectableDays
    }

    override fun getSelectableDays() = selectableDays

    private fun adjustDayInMonthIfNeeded(calendar: Calendar) {
        val day = calendar[Calendar.DAY_OF_MONTH]
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (day > daysInMonth) {
            calendar[Calendar.DAY_OF_MONTH] = daysInMonth
        }
        setToNearestDate(calendar)
    }

    override fun onYearSelected(year: Int) {
        mCalendar[Calendar.YEAR] = year
        adjustDayInMonthIfNeeded(mCalendar)
        updatePickers()
        setCurrentView(MONTH_AND_DAY_VIEW)
        updateDisplay(true)
    }

    override fun onDayOfMonthSelected(year: Int, month: Int, day: Int) {
        mCalendar[Calendar.YEAR] = year
        mCalendar[Calendar.MONTH] = month
        mCalendar[Calendar.DAY_OF_MONTH] = day
        updatePickers()
        updateDisplay(true)
    }

    private fun updatePickers() {
        for (listener in mListeners) listener.onDateChanged()
    }

    override fun getSelectedDay(): MonthAdapter.CalendarDay {
        return MonthAdapter.CalendarDay(mCalendar)
    }

    override fun getMinYear(): Int {
        if (selectableDays != null) return selectableDays[0][Calendar.YEAR]
        // Ensure no years can be selected outside of the given minimum date
        return if (mMinDate[Calendar.YEAR] > mMinYear) mMinDate[Calendar.YEAR] else mMinYear
    }

    override fun getMaxYear(): Int {
        if (selectableDays != null)
            return selectableDays[selectableDays.size - 1][Calendar.YEAR]
        // Ensure no years can be selected outside of the given maximum date
        return if (mMaxDate[Calendar.YEAR] < mMaxYear) mMaxDate[Calendar.YEAR] else mMaxYear
    }

    override fun isOutOfRange(year: Int, month: Int, day: Int) = false

    private fun isBeforeMin(year: Int, month: Int, day: Int): Boolean {
        if (year < mMinDate[Calendar.YEAR]) {
            return true
        } else if (year > mMinDate[Calendar.YEAR]) {
            return false
        }

        if (month < mMinDate[Calendar.MONTH]) {
            return true
        } else if (month > mMinDate[Calendar.MONTH]) {
            return false
        }

        return if (day < mMinDate[Calendar.DAY_OF_MONTH]) {
            true
        } else {
            false
        }
    }

    private fun isBeforeMin(calendar: Calendar): Boolean {
        return isBeforeMin(
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
    }

    private fun isAfterMax(year: Int, month: Int, day: Int): Boolean {
        if (year > mMaxDate[Calendar.YEAR]) {
            return true
        } else if (year < mMaxDate[Calendar.YEAR]) {
            return false
        }

        if (month > mMaxDate[Calendar.MONTH]) {
            return true
        } else if (month < mMaxDate[Calendar.MONTH]) {
            return false
        }

        return if (day > mMaxDate[Calendar.DAY_OF_MONTH]) {
            true
        } else {
            false
        }
    }

    private fun isAfterMax(calendar: Calendar): Boolean {
        return isAfterMax(
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
    }

    private fun setToNearestDate(calendar: Calendar) {
        if (selectableDays != null) {
            var distance = Int.MAX_VALUE
            for (c in selectableDays) {
                val newDistance = abs(calendar.compareTo(c))
                if (newDistance < distance) distance = newDistance else {
                    calendar.timeInMillis = c.timeInMillis
                    break
                }
            }
            return
        }

        if (isBeforeMin(calendar)) {
            calendar.timeInMillis = mMinDate.timeInMillis
            return
        }

        if (isAfterMax(calendar)) {
            calendar.timeInMillis = mMaxDate.timeInMillis
            return
        }
    }

    override fun getFirstDayOfWeek() = mWeekStart

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

        private const val ANIMATION_DURATION = 300
        private const val ANIMATION_DELAY = 500

        private val YEAR_FORMAT = SimpleDateFormat("yyyy", Locale.getDefault())
        private val DAY_FORMAT = SimpleDateFormat("dd", Locale.getDefault())

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
