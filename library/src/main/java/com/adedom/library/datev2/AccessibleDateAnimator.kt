package com.adedom.library.datev2

import android.content.Context
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import android.widget.ViewAnimator

class AccessibleDateAnimator(
    context: Context?, attrs: AttributeSet?
) : ViewAnimator(context, attrs) {

    private var mDateMillis: Long = 0

    fun setDateMillis(dateMillis: Long) {
        mDateMillis = dateMillis
    }

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent?): Boolean {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.text.clear()
            val flags = DateUtils.FORMAT_SHOW_DATE or
                    DateUtils.FORMAT_SHOW_YEAR or
                    DateUtils.FORMAT_SHOW_WEEKDAY

            val dateString = DateUtils.formatDateTime(context, mDateMillis, flags)
            event.text.add(dateString)
            return true
        }
        return super.dispatchPopulateAccessibilityEvent(event)
    }

}
