package com.adedom.library.datev2

import android.content.Context
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.adedom.library.R
import com.adedom.library.date.DatePickerController
import com.adedom.library.date.DatePickerDialog
import com.adedom.library.date.TextViewWithCircularIndicator
import java.util.*

class YearPickerView(
    context: Context,
    private val mController: DatePickerController
) : ListView(context),
    OnItemClickListener,
    DatePickerDialog.OnDateChangedListener {

    private lateinit var mAdapter: YearAdapter
    private val mViewSize: Int
    private val mChildSize: Int
    private var mSelectedView: TextViewWithCircularIndicator? = null

    private fun init(context: Context) {
        val years = ArrayList<String>()
        for (year in mController.minYear..mController.maxYear) {
            years.add(String.format("%d", year))
        }
        mAdapter = YearAdapter(context, R.layout.mdtp_year_label_text_view, years)
        adapter = mAdapter
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, p2: Int, p3: Long) {
        mController.tryVibrate()
        val clickedView = view as TextViewWithCircularIndicator
        if (clickedView != mSelectedView) {
            if (mSelectedView != null) {
                mSelectedView?.drawIndicator(false)
                mSelectedView?.requestLayout()
            }
            clickedView.drawIndicator(true)
            clickedView.requestLayout()
            mSelectedView = clickedView
        }
        mController.onYearSelected(getYearFromTextView(clickedView))
        mAdapter.notifyDataSetChanged()
    }

    private inner class YearAdapter(
        context: Context, resource: Int, objects: List<String>
    ) : ArrayAdapter<String>(context, resource, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = super.getView(position, convertView, parent) as TextViewWithCircularIndicator
            v.setAccentColor(mController.accentColor)
            v.requestLayout()
            val year = getYearFromTextView(v)
            val selected = mController.selectedDay.year == year
            v.drawIndicator(selected)
            if (selected) {
                mSelectedView = v
            }
            return v
        }

    }

    fun postSetSelectionCentered(position: Int) {
        postSetSelectionFromTop(position, mViewSize / 2 - mChildSize / 2)
    }

    fun postSetSelectionFromTop(position: Int, offset: Int) {
        post {
            setSelectionFromTop(position, offset)
            requestLayout()
        }
    }

    fun getFirstPositionOffset(): Int {
        val firstChild = getChildAt(0) ?: return 0
        return firstChild.top
    }

    override fun onDateChanged() {
        mAdapter.notifyDataSetChanged()
        postSetSelectionCentered(mController.selectedDay.year - mController.minYear)
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            event.fromIndex = 0
            event.toIndex = 0
        }
    }

    companion object {

        private const val TAG = "YearPickerView"

        private fun getYearFromTextView(view: TextView): Int {
            return Integer.valueOf(view.text.toString())
        }
    }

    init {
        mController.registerOnDateChangedListener(this)
        val frame = ViewGroup.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        layoutParams = frame
        val res = context.resources
        mViewSize = res.getDimensionPixelOffset(R.dimen.mdtp_date_picker_view_animator_height)
        mChildSize = res.getDimensionPixelOffset(R.dimen.mdtp_year_label_height)
        isVerticalFadingEdgeEnabled = true
        setFadingEdgeLength(mChildSize / 3)
        init(context)
        onItemClickListener = this
        selector = StateListDrawable()
        dividerHeight = 0
        onDateChanged()
    }

}
