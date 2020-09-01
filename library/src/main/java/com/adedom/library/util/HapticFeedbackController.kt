package com.adedom.library.util

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.SystemClock
import android.os.Vibrator
import android.provider.Settings

class HapticFeedbackController(private val mContext: Context) {

    private val mContentObserver: ContentObserver
    private var mVibrator: Vibrator? = null
    private var mIsGloballyEnabled = false
    private var mLastVibrate: Long = 0

    fun start() {
        if (hasVibratePermission(mContext)) {
            mVibrator = mContext.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
        }

        mIsGloballyEnabled = checkGlobalSetting(mContext)
        val uri = Settings.System.getUriFor(Settings.System.HAPTIC_FEEDBACK_ENABLED)
        mContext.contentResolver.registerContentObserver(uri, false, mContentObserver)
    }

    private fun hasVibratePermission(context: Context): Boolean {
        val pm = context.packageManager
        val hasPerm = pm.checkPermission(Manifest.permission.VIBRATE, context.packageName)
        return hasPerm == PackageManager.PERMISSION_GRANTED
    }

    fun stop() {
        mVibrator = null
        mContext.contentResolver.unregisterContentObserver(mContentObserver)
    }

    fun tryVibrate() {
        if (mVibrator != null && mIsGloballyEnabled) {
            val now = SystemClock.uptimeMillis()
            if (now - mLastVibrate >= VIBRATE_DELAY_MS) {
                mVibrator!!.vibrate(VIBRATE_LENGTH_MS.toLong())
                mLastVibrate = now
            }
        }
    }

    companion object {

        private const val VIBRATE_DELAY_MS = 125
        private const val VIBRATE_LENGTH_MS = 50

        private fun checkGlobalSetting(context: Context): Boolean {
            return Settings.System.getInt(
                context.contentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0
            ) == 1
        }

    }

    init {
        mContentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                mIsGloballyEnabled = checkGlobalSetting(mContext)
            }
        }
    }

}
