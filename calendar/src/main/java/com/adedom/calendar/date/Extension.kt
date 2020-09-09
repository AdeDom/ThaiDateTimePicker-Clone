package com.adedom.calendar.date

internal fun Int.toPadStart() = this.toString().padStart(2, '0')
