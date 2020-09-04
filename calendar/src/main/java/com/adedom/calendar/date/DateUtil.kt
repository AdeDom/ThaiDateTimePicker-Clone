package com.adedom.calendar.date

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {

    private const val BUDDHIST_OFFSET = 543

    fun getLocaleYear(locale: Locale, calendar: Calendar): String {
        val year = SimpleDateFormat(DatePickerDialog.YEAR_FORMAT, locale)
            .format(calendar.time).toInt()

        return if (locale == DatePickerDialog.LOCALE_TH) {
            year.plus(BUDDHIST_OFFSET).toString()
        } else {
            year.toString()
        }
    }

    fun getDatePicker(locale: Locale, calendar: Calendar): String {
        return if (locale == DatePickerDialog.LOCALE_TH) {
            "${calendar[Calendar.DAY_OF_MONTH]}/" +
                    "${calendar[Calendar.MONTH].plus(1)}/" +
                    "${calendar[Calendar.YEAR].plus(BUDDHIST_OFFSET)}"
        } else {
            "${calendar[Calendar.DAY_OF_MONTH]}/" +
                    "${calendar[Calendar.MONTH].plus(1)}/" +
                    "${calendar[Calendar.YEAR]}"
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

}
