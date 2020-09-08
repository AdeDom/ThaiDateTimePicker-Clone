package com.adedom.calendar.date

import org.junit.Test
import java.util.*

class DateUtilTest {

    @Test
    fun test_getDay() {
        val calendar = Calendar.getInstance()
        calendar.set(2020,8,1)
        val dateEn1 = DateUtil.getDayLabel(calendar, LOCALE_EN)
        val dateTh1 = DateUtil.getDayLabel(calendar, LOCALE_TH)
        println("dateEn : $dateEn1 ||| dateTh : $dateTh1")

        calendar.set(2020,8,2)
        val dateEn2 = DateUtil.getDayLabel(calendar, LOCALE_EN)
        val dateTh2 = DateUtil.getDayLabel(calendar, LOCALE_TH)
        println("dateEn : $dateEn2 ||| dateTh : $dateTh2")

        calendar.set(2020,8,3)
        val dateEn3 = DateUtil.getDayLabel(calendar, LOCALE_EN)
        val dateTh3 = DateUtil.getDayLabel(calendar, LOCALE_TH)
        println("dateEn : $dateEn3 ||| dateTh : $dateTh3")

        calendar.set(2020,8,4)
        val dateEn4 = DateUtil.getDayLabel(calendar, LOCALE_EN)
        val dateTh4 = DateUtil.getDayLabel(calendar, LOCALE_TH)
        println("dateEn : $dateEn4 ||| dateTh : $dateTh4")

        calendar.set(2020,8,5)
        val dateEn5 = DateUtil.getDayLabel(calendar, LOCALE_EN)
        val dateTh5 = DateUtil.getDayLabel(calendar, LOCALE_TH)
        println("dateEn : $dateEn5 ||| dateTh : $dateTh5")

        calendar.set(2020,8,6)
        val dateEn6 = DateUtil.getDayLabel(calendar, LOCALE_EN)
        val dateTh6 = DateUtil.getDayLabel(calendar, LOCALE_TH)
        println("dateEn : $dateEn6 ||| dateTh : $dateTh6")

        calendar.set(2020,8,7)
        val dateEn7 = DateUtil.getDayLabel(calendar, LOCALE_EN)
        val dateTh7 = DateUtil.getDayLabel(calendar, LOCALE_TH)
        println("dateEn : $dateEn7 ||| dateTh : $dateTh7")

        calendar.set(2020,8,8)
        val dateEn8 = DateUtil.getDayLabel(calendar, LOCALE_EN)
        val dateTh8 = DateUtil.getDayLabel(calendar, LOCALE_TH)
        println("dateEn : $dateEn8 ||| dateTh : $dateTh8")

    }

    companion object {
        val LOCALE_EN = Locale("en", "EN")
        val LOCALE_TH = Locale("th", "TH")
    }

}
