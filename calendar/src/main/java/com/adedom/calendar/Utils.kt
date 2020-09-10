package com.adedom.calendar

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat

internal class Utils {

    companion object {
        private const val PULSE_ANIMATOR_DURATION = 544

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
    }

}
