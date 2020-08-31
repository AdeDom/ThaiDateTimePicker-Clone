package com.adedom.library.datev2

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.adedom.library.R
import kotlin.math.min

class TextViewWithCircularIndicator(
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
        mCirclePaint.textAlign = Align.CENTER
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
        mCircleColor = ContextCompat.getColor(context, R.color.mdtp_accent_color)
        mItemIsSelectedText = context.resources.getString(R.string.mdtp_item_is_selected)

        init()
    }

}
