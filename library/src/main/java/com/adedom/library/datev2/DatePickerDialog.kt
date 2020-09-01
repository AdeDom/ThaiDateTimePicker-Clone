package com.adedom.library.datev2

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.adedom.library.R
import com.adedom.library.Utils
import com.adedom.library.date.DatePickerController
import com.adedom.library.date.DayPickerView
import com.adedom.library.date.MonthAdapter.CalendarDay
import com.adedom.library.util.HapticFeedbackController
import com.adedom.library.util.TypefaceHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class DatePickerDialog : DialogFragment(), View.OnClickListener, DatePickerController {

    private val mCalendar = Calendar.getInstance()
    private lateinit var mCallBack: com.adedom.library.date.DatePickerDialog.OnDateSetListener
    private val mListeners = HashSet<com.adedom.library.date.DatePickerDialog.OnDateChangedListener>()
    private lateinit var mOnCancelListener: DialogInterface.OnCancelListener
    private lateinit var mOnDismissListener: DialogInterface.OnDismissListener

    private lateinit var mAnimator: AccessibleDateAnimator

    private lateinit var mMonthAndDayView: LinearLayout
    private lateinit var mSelectedMonthTextView: TextView
    private lateinit var mSelectedDayTextView: TextView
    private lateinit var mYearView: TextView
    private lateinit var mDayPickerView: DayPickerView
    private lateinit var mYearPickerView: YearPickerView

    private var mCurrentView = UNINITIALIZED

    private var mWeekStart = mCalendar.firstDayOfWeek
    private var mMinYear = DEFAULT_START_YEAR
    private var mMaxYear = DEFAULT_END_YEAR
    private var mTitle: String? = null
    private lateinit var mMinDate: Calendar
    private lateinit var mMaxDate: Calendar
    private lateinit var highlightedDays: Array<Calendar>
    private lateinit var selectableDays: Array<Calendar>
    private var mThemeDark = false
    private var mAccentColor = -1
    private var mVibrate = true
    private var mDismissOnPause = false
    private var mDefaultView = MONTH_AND_DAY_VIEW

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
        callBack: com.adedom.library.date.DatePickerDialog.OnDateSetListener,
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
        if (savedInstanceState != null) {
            mCalendar[Calendar.YEAR] = savedInstanceState.getInt(KEY_SELECTED_YEAR)
            mCalendar[Calendar.MONTH] = savedInstanceState.getInt(KEY_SELECTED_MONTH)
            mCalendar[Calendar.DAY_OF_MONTH] = savedInstanceState.getInt(KEY_SELECTED_DAY)
            mDefaultView = savedInstanceState.getInt(KEY_DEFAULT_VIEW)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_YEAR, mCalendar[Calendar.YEAR])
        outState.putInt(KEY_SELECTED_MONTH, mCalendar[Calendar.MONTH])
        outState.putInt(KEY_SELECTED_DAY, mCalendar[Calendar.DAY_OF_MONTH])
        outState.putInt(KEY_WEEK_START, mWeekStart)
        outState.putInt(KEY_YEAR_START, mMinYear)
        outState.putInt(KEY_YEAR_END, mMaxYear)
        outState.putInt(KEY_CURRENT_VIEW, mCurrentView)
        var listPosition = -1
        if (mCurrentView == MONTH_AND_DAY_VIEW) {
            listPosition = mDayPickerView.mostVisiblePosition
        } else if (mCurrentView == YEAR_VIEW) {
            listPosition = mYearPickerView.firstVisiblePosition
            outState.putInt(KEY_LIST_POSITION_OFFSET, mYearPickerView.getFirstPositionOffset())
        }
        outState.putInt(KEY_LIST_POSITION, listPosition)
        outState.putSerializable(KEY_MIN_DATE, mMinDate)
        outState.putSerializable(KEY_MAX_DATE, mMaxDate)
        outState.putSerializable(KEY_HIGHLIGHTED_DAYS, highlightedDays)
        outState.putSerializable(KEY_SELECTABLE_DAYS, selectableDays)
        outState.putBoolean(KEY_THEME_DARK, mThemeDark)
        outState.putInt(KEY_ACCENT, mAccentColor)
        outState.putBoolean(KEY_VIBRATE, mVibrate)
        outState.putBoolean(KEY_DISMISS, mDismissOnPause)
        outState.putInt(KEY_DEFAULT_VIEW, mDefaultView)
        outState.putString(KEY_TITLE, mTitle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        val view: View = inflater.inflate(R.layout.mdtp_date_picker_dialog, container)

        Log.d(TAG, "onCreateView: $view")

        mMonthAndDayView = view.findViewById<View>(R.id.date_picker_month_and_day) as LinearLayout
        mMonthAndDayView.setOnClickListener(this)
        mSelectedMonthTextView = view.findViewById<View>(R.id.date_picker_month) as TextView
        mSelectedDayTextView = view.findViewById<View>(R.id.date_picker_day) as TextView
        mYearView = view.findViewById<View>(R.id.date_picker_year) as TextView
        mYearView.setOnClickListener(this)

        var listPosition = -1
        var listPositionOffset = 0
        var currentView = mDefaultView
        if (savedInstanceState != null) {
            mWeekStart = savedInstanceState.getInt(KEY_WEEK_START)
            mMinYear = savedInstanceState.getInt(KEY_YEAR_START)
            mMaxYear = savedInstanceState.getInt(KEY_YEAR_END)
            currentView = savedInstanceState.getInt(KEY_CURRENT_VIEW)
            listPosition = savedInstanceState.getInt(KEY_LIST_POSITION)
            listPositionOffset = savedInstanceState.getInt(KEY_LIST_POSITION_OFFSET)
            mMinDate = savedInstanceState.getSerializable(KEY_MIN_DATE) as Calendar
            mMaxDate = savedInstanceState.getSerializable(KEY_MAX_DATE) as Calendar
            highlightedDays =
                savedInstanceState.getSerializable(KEY_HIGHLIGHTED_DAYS) as Array<Calendar>
            selectableDays =
                savedInstanceState.getSerializable(KEY_SELECTABLE_DAYS) as Array<Calendar>
            mThemeDark = savedInstanceState.getBoolean(KEY_THEME_DARK)
            mAccentColor = savedInstanceState.getInt(KEY_ACCENT)
            mVibrate = savedInstanceState.getBoolean(KEY_VIBRATE)
            mDismissOnPause = savedInstanceState.getBoolean(KEY_DISMISS)
            mTitle = savedInstanceState.getString(KEY_TITLE)
        }

        val activity: Activity? = activity
        mDayPickerView = SimpleDayPickerView(activity, this)
        if (activity != null)
            mYearPickerView = YearPickerView(activity, this)

        val res = resources
        mDayPickerDescription = res.getString(R.string.mdtp_day_picker_description)
        mSelectDay = res.getString(R.string.mdtp_select_day)
        mYearPickerDescription = res.getString(R.string.mdtp_year_picker_description)
        mSelectYear = res.getString(R.string.mdtp_select_year)

        val bgColorResource: Int =
            if (mThemeDark) R.color.mdtp_date_picker_view_animator_dark_theme else R.color.mdtp_date_picker_view_animator
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
            tryVibrate()
            mCallBack.onDateSet(
                this@DatePickerDialog, mCalendar[Calendar.YEAR],
                mCalendar[Calendar.MONTH], mCalendar[Calendar.DAY_OF_MONTH]
            )
            dismiss()
        }
        okButton.typeface = TypefaceHelper[activity, "Roboto-Medium"]

        val cancelButton = view.findViewById<View>(R.id.cancel) as Button
        cancelButton.setOnClickListener {
            tryVibrate()
            if (dialog != null) dialog?.cancel()
        }
        cancelButton.typeface = TypefaceHelper[activity, "Roboto-Medium"]
        cancelButton.visibility = if (isCancelable) View.VISIBLE else View.GONE

        // If an accent color has not been set manually, get it from the context
        if (mAccentColor == -1) {
            mAccentColor = Utils.getAccentColorFromThemeIfAvailable(getActivity())
        }
        view.findViewById<View>(R.id.day_picker_selected_date_layout)
            .setBackgroundColor(mAccentColor)
        okButton.setTextColor(mAccentColor)
        cancelButton.setTextColor(mAccentColor)

        updateDisplay(false)
        setCurrentView(currentView)

        if (listPosition != -1) {
            if (currentView == MONTH_AND_DAY_VIEW) {
                mDayPickerView.postSetSelection(listPosition)
            } else if (currentView == YEAR_VIEW) {
                mYearPickerView.postSetSelectionFromTop(listPosition, listPositionOffset)
            }
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
        if (mDismissOnPause) dismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        mOnCancelListener.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mOnDismissListener.onDismiss(dialog)
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
        ).toUpperCase(Locale.getDefault())
        mSelectedDayTextView.text = DAY_FORMAT.format(mCalendar.time)
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

    fun vibrate(vibrate: Boolean) {
        mVibrate = vibrate
    }

    fun dismissOnPause(dismissOnPause: Boolean) {
        mDismissOnPause = dismissOnPause
    }

    fun setThemeDark(themeDark: Boolean) {
        mThemeDark = themeDark
    }

    override fun isThemeDark() = mThemeDark

    fun setAccentColor(accentColor: Int) {
        mAccentColor = accentColor
    }

    override fun getAccentColor() = mAccentColor

    fun showYearPickerFirst(yearPicker: Boolean) {
        mDefaultView = if (yearPicker) YEAR_VIEW else MONTH_AND_DAY_VIEW
    }

    fun setFirstDayOfWeek(startOfWeek: Int) {
        if (startOfWeek < Calendar.SUNDAY || startOfWeek > Calendar.SATURDAY) {
            throw IllegalArgumentException("Value must be between Calendar.SUNDAY and Calendar.SATURDAY")
        }
        mWeekStart = startOfWeek
        if (mDayPickerView != null) {
            mDayPickerView.onChange()
        }
    }

    fun setYearRange(startYear: Int, endYear: Int) {
        if (endYear < startYear) {
            throw java.lang.IllegalArgumentException("Year end must be larger than or equal to year start")
        }

        mMinYear = startYear
        mMaxYear = endYear
        mDayPickerView.onChange()
    }

    fun setMinDate(calendar: Calendar) {
        mMinDate = calendar
        mDayPickerView.onChange()
    }

    fun getMinDate() = mMinDate

    fun setMaxDate(calendar: Calendar) {
        mMaxDate = calendar
        mDayPickerView.onChange()
    }

    fun getMaxDate() = mMaxDate

    fun setHighlightedDays(highlightedDays: Array<Calendar>) {
        // Sort the array to optimize searching over it later on
        Arrays.sort(highlightedDays)
        this.highlightedDays = highlightedDays
    }

    override fun getHighlightedDays() = highlightedDays

    fun setSelectableDays(selectableDays: Array<Calendar>) {
        // Sort the array to optimize searching over it later on
        Arrays.sort(selectableDays)
        this.selectableDays = selectableDays
    }

    override fun getSelectableDays() = selectableDays

    fun setTitle(title: String) {
        mTitle = title
    }

    fun setOnDateSetListener(listener: com.adedom.library.date.DatePickerDialog.OnDateSetListener) {
        mCallBack = listener
    }

    fun setOnCancelListener(onCancelListener: DialogInterface.OnCancelListener) {
        mOnCancelListener = onCancelListener
    }

    fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener) {
        mOnDismissListener = onDismissListener
    }

    private fun adjustDayInMonthIfNeeded(calendar: Calendar) {
        val day = calendar[Calendar.DAY_OF_MONTH]
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (day > daysInMonth) {
            calendar[Calendar.DAY_OF_MONTH] = daysInMonth
        }
        setToNearestDate(calendar)
    }

    override fun onClick(v: View?) {
        tryVibrate()
        if (v?.id == R.id.date_picker_year) {
            setCurrentView(YEAR_VIEW)
        } else if (v?.id == R.id.date_picker_month_and_day) {
            setCurrentView(MONTH_AND_DAY_VIEW)
        }
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

    override fun getSelectedDay(): CalendarDay {
        return CalendarDay(mCalendar)
    }

    override fun getMinYear(): Int {
        if (selectableDays != null) return selectableDays[0][Calendar.YEAR]
        // Ensure no years can be selected outside of the given minimum date
        return if (mMinDate != null && mMinDate[Calendar.YEAR] > mMinYear) mMinDate[Calendar.YEAR] else mMinYear
    }

    override fun getMaxYear(): Int {
        if (selectableDays != null)
            return selectableDays[selectableDays.size - 1][Calendar.YEAR]
        // Ensure no years can be selected outside of the given maximum date
        return if (mMaxDate != null && mMaxDate[Calendar.YEAR] < mMaxYear) mMaxDate[Calendar.YEAR] else mMaxYear
    }

    override fun isOutOfRange(year: Int, month: Int, day: Int): Boolean {
        if (selectableDays != null) {
            return !isSelectable(year, month, day)
        }

        if (isBeforeMin(year, month, day)) {
            return true
        } else if (isAfterMax(year, month, day)) {
            return true
        }

        return false
    }

    fun isOutOfRange(calendar: Calendar): Boolean {
        return isOutOfRange(
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
    }

    private fun isSelectable(year: Int, month: Int, day: Int): Boolean {
        for (c in selectableDays) {
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

    private fun isBeforeMin(year: Int, month: Int, day: Int): Boolean {
        if (mMinDate == null) {
            return false
        }

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
        if (mMaxDate == null) {
            return false
        }

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

    override fun registerOnDateChangedListener(listener: com.adedom.library.date.DatePickerDialog.OnDateChangedListener?) {
        listener?.let { mListeners.add(it) }
    }

    override fun unregisterOnDateChangedListener(listener: com.adedom.library.date.DatePickerDialog.OnDateChangedListener?) {
        mListeners.remove(listener)
    }

    override fun tryVibrate() {
        if (mVibrate) mHapticFeedbackController?.tryVibrate()
    }

    companion object {
        private const val TAG = "DatePickerDialog"

        private const val UNINITIALIZED = -1
        private const val MONTH_AND_DAY_VIEW = 0
        private const val YEAR_VIEW = 1

        private const val KEY_SELECTED_YEAR = "year"
        private const val KEY_SELECTED_MONTH = "month"
        private const val KEY_SELECTED_DAY = "day"
        private const val KEY_LIST_POSITION = "list_position"
        private const val KEY_WEEK_START = "week_start"
        private const val KEY_YEAR_START = "year_start"
        private const val KEY_YEAR_END = "year_end"
        private const val KEY_CURRENT_VIEW = "current_view"
        private const val KEY_LIST_POSITION_OFFSET = "list_position_offset"
        private const val KEY_MIN_DATE = "min_date"
        private const val KEY_MAX_DATE = "max_date"
        private const val KEY_HIGHLIGHTED_DAYS = "highlighted_days"
        private const val KEY_SELECTABLE_DAYS = "selectable_days"
        private const val KEY_THEME_DARK = "theme_dark"
        private const val KEY_ACCENT = "accent"
        private const val KEY_VIBRATE = "vibrate"
        private const val KEY_DISMISS = "dismiss"
        private const val KEY_DEFAULT_VIEW = "default_view"
        private const val KEY_TITLE = "title"

        private const val DEFAULT_START_YEAR = 1900
        private const val DEFAULT_END_YEAR = 2100

        private const val ANIMATION_DURATION = 300
        private const val ANIMATION_DELAY = 500

        private val YEAR_FORMAT = SimpleDateFormat("yyyy", Locale.getDefault())
        private val DAY_FORMAT = SimpleDateFormat("dd", Locale.getDefault())

        fun newInstance(
            callback: com.adedom.library.date.DatePickerDialog.OnDateSetListener,
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
