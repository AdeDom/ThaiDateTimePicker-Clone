package com.adedom.calendar

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.collection.SimpleArrayMap
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

internal object DateUtil {

    private const val BUDDHIST_OFFSET = 543
    private const val PULSE_ANIMATOR_DURATION = 544

    const val YEAR_FORMAT = "yyyy"
    const val MONTH_FORMAT = "MMMM"
    const val DATE_FORMAT = "d"
    const val DATE_NAME_FORMAT = "E"
    const val FULL_DATE_FORMAT = "E, d MMMM "

    private val cache = SimpleArrayMap<String, Typeface>()

    fun getLocaleYear(locale: Locale, calendar: Calendar): String {
        val year = SimpleDateFormat(YEAR_FORMAT, locale)
            .format(calendar.time).toInt()

        return if (locale == DatePickerDialog.LOCALE_TH) {
            year.plus(BUDDHIST_OFFSET).toString()
        } else {
            year.toString()
        }
    }

    fun getDatePicker(locale: Locale, calendar: Calendar): DateItem {
        val year: Int = calendar[Calendar.YEAR]
        val monthOfYear: Int = calendar[Calendar.MONTH].plus(1)
        val dayOfMonth: Int = calendar[Calendar.DAY_OF_MONTH]
        return if (locale == DatePickerDialog.LOCALE_TH) {
            DateItem(year.plus(BUDDHIST_OFFSET), monthOfYear, dayOfMonth)
        } else {
            DateItem(year, monthOfYear, dayOfMonth)
        }
    }

    fun getYearList(locale: Locale, minYear: Int, maxYear: Int): MutableList<String> {
        val years = ArrayList<String>()
        if (locale == DatePickerDialog.LOCALE_TH) {
            for (year in minYear.plus(BUDDHIST_OFFSET)..
                    maxYear.plus(BUDDHIST_OFFSET)) {
                years.add(year.toString())
            }
        } else {
            for (year in minYear..maxYear) {
                years.add(year.toString())
            }
        }
        return years
    }

    fun getYearFromText(locale: Locale, text: String): Int {
        val year = text.toInt()
        return if (locale == DatePickerDialog.LOCALE_TH) year.minus(BUDDHIST_OFFSET) else year
    }

    fun getMonthAndYear(calendar: Calendar?, locale: Locale?): String {
        val sdfMonth = SimpleDateFormat(MONTH_FORMAT, locale)
        val month = sdfMonth.format(calendar?.timeInMillis)

        val sdfYear = SimpleDateFormat(YEAR_FORMAT, locale)
        var tempYear = sdfYear.format(calendar?.timeInMillis).toInt()
        val year = if (locale == DatePickerDialog.LOCALE_TH) {
            tempYear += BUDDHIST_OFFSET
            tempYear
        } else {
            tempYear
        }
        return "$month $year"
    }

    fun getDayLabel(calendar: Calendar?, locale: Locale): String {
        val sdf = SimpleDateFormat(DATE_NAME_FORMAT, locale)
        return if (locale == DatePickerDialog.LOCALE_TH) {
            sdf.format(calendar?.timeInMillis).replace(".", "")
        } else {
            sdf.format(calendar?.timeInMillis).substring(0, 1).toUpperCase(locale)
        }
    }

    fun getTextOkFromLocale(context: Context, locale: Locale): String {
        return if (locale == DatePickerDialog.LOCALE_TH) {
            context.getString(R.string.calendar_ok_th)
        } else {
            context.getString(R.string.calendar_ok_en)
        }
    }

    fun getTextCancelFromLocale(context: Context, locale: Locale): String {
        return if (locale == DatePickerDialog.LOCALE_TH) {
            context.getString(R.string.calendar_cancel_th)
        } else {
            context.getString(R.string.calendar_cancel_en)
        }
    }

    fun getPeriodDateDiff(
        locale: Locale,
        initialize: DateItem,
        selectDate: DateItem,
    ): Long {
        val simpleDateFormat = SimpleDateFormat("dd MM yyyy")
        val date1 = "${initialize.dayOfMonth.toPadStart()} " +
                "${initialize.monthOfYear.plus(1).toPadStart()} " +
                "${initialize.year}"

        val date2 = if (locale == DatePickerDialog.LOCALE_TH) {
            "${selectDate.dayOfMonth.toPadStart()} " +
                    "${selectDate.monthOfYear.toPadStart()} " +
                    "${selectDate.year.minus(BUDDHIST_OFFSET)}"
        } else {
            "${selectDate.dayOfMonth.toPadStart()} " +
                    "${selectDate.monthOfYear.toPadStart()} " +
                    "${selectDate.year}"
        }

        val dateBegin: Date = simpleDateFormat.parse(date1)
        val dateEnd: Date = simpleDateFormat.parse(date2)
        val diff = dateEnd.time - dateBegin.time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }

    fun isSelectable(
        selectableDays: Array<Calendar>,
        year: Int,
        month: Int,
        day: Int
    ): Boolean {
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

    fun isBeforeMin(minDate: Calendar?, year: Int, month: Int, day: Int): Boolean {
        if (minDate == null) {
            return false
        }

        if (year < minDate[Calendar.YEAR]) {
            return true
        } else if (year > minDate[Calendar.YEAR]) {
            return false
        }

        if (month < minDate[Calendar.MONTH]) {
            return true
        } else if (month > minDate[Calendar.MONTH]) {
            return false
        }

        return if (day < minDate[Calendar.DAY_OF_MONTH]) {
            true
        } else {
            false
        }
    }

    private fun isBeforeMin(minDateCalendar: Calendar?, calendar: Calendar): Boolean {
        return isBeforeMin(
            minDateCalendar,
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
    }

    fun isAfterMax(maxDate: Calendar?, year: Int, month: Int, day: Int): Boolean {
        if (maxDate == null) {
            return false
        }

        if (year > maxDate[Calendar.YEAR]) {
            return true
        } else if (year < maxDate[Calendar.YEAR]) {
            return false
        }

        if (month > maxDate[Calendar.MONTH]) {
            return true
        } else if (month < maxDate[Calendar.MONTH]) {
            return false
        }

        return if (day > maxDate[Calendar.DAY_OF_MONTH]) {
            true
        } else {
            false
        }
    }

    private fun isAfterMax(maxDate: Calendar?, calendar: Calendar): Boolean {
        return isAfterMax(
            maxDate,
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
    }

    fun setToNearestDate(
        selectableDays: Array<Calendar>?,
        minDate: Calendar,
        maxDate: Calendar,
        calendar: Calendar
    ) {
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

        if (isBeforeMin(minDate, calendar)) {
            calendar.timeInMillis = minDate.timeInMillis
            return
        }

        if (isAfterMax(maxDate, calendar)) {
            calendar.timeInMillis = maxDate.timeInMillis
            return
        }
    }

    fun adjustDayInMonthIfNeeded(calendar: Calendar) {
        val day = calendar[Calendar.DAY_OF_MONTH]
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        if (day > daysInMonth) {
            calendar[Calendar.DAY_OF_MONTH] = daysInMonth
        }
    }

    private fun isJellybeanOrLater(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
    }

    @SuppressLint("NewApi")
    fun tryAccessibilityAnnounce(view: View?, text: CharSequence?) {
        if (isJellybeanOrLater() && view != null && text != null) {
            view.announceForAccessibility(text)
        }
    }

    fun getPulseAnimator(
        labelToAnimate: View?,
        decreaseRatio: Float,
        increaseRatio: Float
    ): ObjectAnimator {
        val k0 = Keyframe.ofFloat(0f, 1f)
        val k1 = Keyframe.ofFloat(0.275f, decreaseRatio)
        val k2 = Keyframe.ofFloat(0.69f, increaseRatio)
        val k3 = Keyframe.ofFloat(1f, 1f)

        val scaleX = PropertyValuesHolder.ofKeyframe("scaleX", k0, k1, k2, k3)
        val scaleY = PropertyValuesHolder.ofKeyframe("scaleY", k0, k1, k2, k3)
        val pulseAnimator =
            ObjectAnimator.ofPropertyValuesHolder(labelToAnimate, scaleX, scaleY)
        pulseAnimator.duration = PULSE_ANIMATOR_DURATION.toLong()
        return pulseAnimator
    }

    fun getAccentColorFromThemeIfAvailable(context: Context): Int {
        val typedValue = TypedValue()
        if (Build.VERSION.SDK_INT >= 21) {
            context.theme.resolveAttribute(R.attr.colorAccent, typedValue, true)
            return typedValue.data
        }
        val colorAccentResId = context.resources
            .getIdentifier("colorAccent", "attr", context.packageName)
        return if (colorAccentResId != 0 && context.theme.resolveAttribute(
                colorAccentResId,
                typedValue,
                true
            )
        ) {
            typedValue.data
        } else ContextCompat.getColor(context, R.color.calendar_accent_color)
    }

    operator fun get(c: Context?, name: String?): Typeface? {
        synchronized(cache) {
            if (!cache.containsKey(name)) {
                val t = Typeface.createFromAsset(
                    c?.assets, String.format("fonts/%s.ttf", name)
                )
                cache.put(name, t)
                return t
            }
            return cache[name]
        }
    }

    private fun Int.toPadStart() = this.toString().padStart(2, '0')

}

internal data class DateItem(
    val year: Int,
    val monthOfYear: Int,
    val dayOfMonth: Int,
)
