package com.adedom.calendar.customcalendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import java.util.*

internal class SimpleDayPickerView : CustomCalendarDayPickerView {

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context?,
        controller: CustomCalendarPickerController,
        locale: Locale
    ) : super(context, controller, locale)

    override fun createMonthAdapter(
        context: Context?,
        controller: CustomCalendarPickerController,
        locale: Locale
    ): CustomCalendarMonthAdapter {
        return SimpleMonthAdapter(context, controller, locale)
    }

}

internal class SimpleMonthAdapter(
    context: Context?, controller: CustomCalendarPickerController, private val mLocale: Locale
) : CustomCalendarMonthAdapter(context, controller) {

    override fun createMonthView(context: Context?): CustomCalendarMonthView {
        return SimpleMonthView(context, null, mController, mLocale)
    }

}

internal class SimpleMonthView(
    context: Context?, attr: AttributeSet?, controller: CustomCalendarPickerController, locale: Locale
) : CustomCalendarMonthView(context, attr, controller, locale) {

    override fun drawMonthDay(
        canvas: Canvas?,
        year: Int,
        month: Int,
        day: Int,
        x: Int,
        y: Int,
        startX: Int,
        stopX: Int,
        startY: Int,
        stopY: Int
    ) {
        if (mSelectedDay == day) {
            canvas?.drawCircle(
                x.toFloat(),
                y - (MINI_DAY_NUMBER_TEXT_SIZE / 3).toFloat(),
                DAY_SELECTED_CIRCLE_SIZE.toFloat(),
                mSelectedCirclePaint
            )
        }
        if (isHighlighted(year, month, day)) {
            mMonthNumPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        } else {
            mMonthNumPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        if (mController.isOutOfRange(year, month, day)) {
            mMonthNumPaint.color = mDisabledDayTextColor
        } else if (mSelectedDay == day) {
            mMonthNumPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            mMonthNumPaint.color = mSelectedDayTextColor
        } else if (mHasToday && mToday == day) {
            mMonthNumPaint.color = mTodayNumberColor
        } else {
            mMonthNumPaint.color =
                if (isHighlighted(year, month, day)) mHighlightedDayTextColor else mDayTextColor
        }
        canvas?.drawText(String.format("%d", day), x.toFloat(), y.toFloat(), mMonthNumPaint)
    }

}
