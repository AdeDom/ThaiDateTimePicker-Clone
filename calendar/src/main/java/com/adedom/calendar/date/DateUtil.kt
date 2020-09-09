package com.adedom.calendar.date

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

internal class DateUtil {

    companion object {
        private const val BUDDHIST_OFFSET = 543

        const val YEAR_FORMAT = "yyyy"
        const val MONTH_FORMAT = "MMMM"
        const val DATE_FORMAT = "d"
        const val DATE_NAME_FORMAT = "E"
        const val FULL_DATE_FORMAT = "E, d MMMM "

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

        fun getMonthAndYear(calendar: Calendar, locale: Locale): String {
            val sdfMonth = SimpleDateFormat(MONTH_FORMAT, locale)
            val month = sdfMonth.format(calendar.timeInMillis)

            val sdfYear = SimpleDateFormat(YEAR_FORMAT, locale)
            var tempYear = sdfYear.format(calendar.timeInMillis).toInt()
            val year = if (locale == Locale("th", "TH")) {
                tempYear += BUDDHIST_OFFSET
                tempYear
            } else {
                tempYear
            }
            return "$month $year"
        }

        fun getDayLabel(calendar: Calendar, locale: Locale): String {
            val sdf = SimpleDateFormat(DATE_NAME_FORMAT, locale)
            return when (locale) {
                Locale("th", "TH") -> sdf.format(calendar.timeInMillis).replace(".", "")
                else -> sdf.format(calendar.timeInMillis).substring(0, 1).toUpperCase(locale)
            }
        }

        fun getTextOkFromLocale(locale: Locale): String {
            return if (locale == DatePickerDialog.LOCALE_TH) "ตกลง" else "OK"
        }

        fun getTextCancelFromLocale(locale: Locale): String {
            return if (locale == DatePickerDialog.LOCALE_TH) "ยกเลิก" else "CANCEL"
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
    }

}

internal fun Int.toPadStart() = this.toString().padStart(2, '0')
