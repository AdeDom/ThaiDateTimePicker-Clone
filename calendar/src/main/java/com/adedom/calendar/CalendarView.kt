package com.adedom.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.StateListDrawable
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.math.min

internal class AccessibleDateAnimator(
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

internal class TextViewWithCircularIndicator(
    context: Context, attrs: AttributeSet?
) : AppCompatTextView(context, attrs) {

    private var mCirclePaint = Paint()
    private var mCircleColor: Int
    private val mItemIsSelectedText: String
    private var mDrawCircle = false

    private fun init() {
        mCirclePaint.isFakeBoldText = true
        mCirclePaint.isAntiAlias = true
        mCirclePaint.color = mCircleColor
        mCirclePaint.textAlign = Paint.Align.CENTER
        mCirclePaint.style = Paint.Style.FILL
        mCirclePaint.alpha = SELECTED_CIRCLE_ALPHA
    }

    fun setAccentColor(color: Int) {
        mCircleColor = color
        mCirclePaint.color = mCircleColor
        setTextColor(createTextColor(color))
    }

    private fun createTextColor(accentColor: Int): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(android.R.attr.state_selected),
            intArrayOf()
        )
        val colors = intArrayOf(
            accentColor,
            Color.WHITE,
            Color.BLACK
        )
        return ColorStateList(states, colors)
    }

    fun drawIndicator(drawCircle: Boolean) {
        mDrawCircle = drawCircle
    }

    public override fun onDraw(canvas: Canvas) {
        if (mDrawCircle) {
            val width = width
            val height = height
            val radius = min(width, height) / 2
            canvas.drawCircle(
                width / 2.toFloat(),
                height / 2.toFloat(),
                radius.toFloat(),
                mCirclePaint
            )
        }
        isSelected = mDrawCircle
        super.onDraw(canvas)
    }

    @SuppressLint("GetContentDescriptionOverride")
    override fun getContentDescription(): CharSequence {
        val itemText = text
        return if (mDrawCircle) {
            String.format(mItemIsSelectedText, itemText)
        } else {
            itemText
        }
    }

    companion object {
        private const val SELECTED_CIRCLE_ALPHA = 255
    }

    init {
        mCircleColor = ContextCompat.getColor(context, R.color.calendar_accent_color)
        mItemIsSelectedText = context.resources.getString(R.string.calendar_item_is_selected)

        init()
    }

}

internal class YearPickerView(
    context: Context,
    private val mController: DatePickerController,
    private val mLocale: Locale,
) : ListView(context),
    AdapterView.OnItemClickListener,
    DatePickerDialog.OnDateChangedListener {

    private lateinit var mAdapter: YearAdapter
    private val mViewSize: Int
    private val mChildSize: Int
    private var mSelectedView: TextViewWithCircularIndicator? = null

    private fun init(context: Context) {
        val years = DateUtil.getYearList(
            mLocale,
            mController.getMinYear(),
            mController.getMaxYear()
        )
        mAdapter = YearAdapter(context, R.layout.calendar_year, years)
        adapter = mAdapter
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, p2: Int, p3: Long) {
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
            v.setAccentColor(mController.getAccentColor())
            v.requestLayout()
            val year = getYearFromTextView(v)
            val selected = mController.getSelectedDay().year == year
            v.drawIndicator(selected)
            if (selected) {
                mSelectedView = v
            }
            return v
        }

    }

    private fun postSetSelectionCentered(position: Int) {
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
        postSetSelectionCentered(mController.getSelectedDay().year - mController.getMinYear())
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            event.fromIndex = 0
            event.toIndex = 0
        }
    }

    private fun getYearFromTextView(view: TextView): Int {
        return DateUtil.getYearFromText(mLocale, view.text.toString())
    }

    init {
        mController.registerOnDateChangedListener(this)
        val frame = ViewGroup.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        layoutParams = frame
        val res = context.resources
        mViewSize = res.getDimensionPixelOffset(R.dimen.calendar_date_picker_view_animator_height)
        mChildSize = res.getDimensionPixelOffset(R.dimen.calendar_year_label_height)
        isVerticalFadingEdgeEnabled = true
        setFadingEdgeLength(mChildSize / 3)
        init(context)
        onItemClickListener = this
        selector = StateListDrawable()
        dividerHeight = 0
        onDateChanged()
    }

}
