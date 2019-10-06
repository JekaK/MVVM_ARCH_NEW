
package com.krikun.mymvvm_arch.utils

import android.content.Context
import android.content.res.Resources
import android.os.Build
import com.krikun.mymvvm_arch.CommonApp
import android.text.format.DateFormat
import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_DATE FORMATS_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

val LOCALE_DEFAULT = Locale.ENGLISH

object DateFormats {
    val serverDateFormat get() = SimpleDateFormat("d MMM yyyy", LOCALE_DEFAULT)
    val serverFullDateFormat get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", LOCALE_DEFAULT)
    val fullDateFormat get() = SimpleDateFormat("dd MMMM, yyyy", LOCALE_DEFAULT)
    val shortDateFormat get() = SimpleDateFormat("dd.MM", LOCALE_DEFAULT)
    val userDateFormat get() = SimpleDateFormat("dd.MM.yyyy HH:mm", LOCALE_DEFAULT)
    val fullMonthFormat get() = SimpleDateFormat("MMM d, yyyy", LOCALE_DEFAULT)
//    val movieProfileDefaultFormat get() = SimpleDateFormat("MM/d/yyyy", LOCALE_DEFAULT)
//    val watchedProfileFormat get() = SimpleDateFormat("MMM dd, yyyy HH:mm", LOCALE_DEFAULT)
//    val requestedFormat get() = SimpleDateFormat("dd MMM yyyy HH:mm", LOCALE_DEFAULT)
}


/** Helpful wrapper for server date. Use in api response data classes where you have server date as a String. */
inline class ServerDate(val serverDateOriginStr: String) {
    companion object {
        fun now() = ServerDate(Date().formatDate(DateFormats.fullDateFormat))
    }

    val time get() = serverDateOriginStr.getTime()
}

val detailedDateFormat: SimpleDateFormat
    get() = SimpleDateFormat(
        "dd.MM.yyyy ${if (DateFormat.is24HourFormat(CommonApp.instance)) "HH:mm" else "h:mm a"}",
        LOCALE_DEFAULT
    )

val detailedOnlyTimeDateFormat: SimpleDateFormat
    get() = SimpleDateFormat(
        if (DateFormat.is24HourFormat(CommonApp.instance)) "HH:mm:ss" else "h:mm:ss a",
        LOCALE_DEFAULT
    )

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_UTILS METHODS_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun String.getTime(dateFormat: SimpleDateFormat = DateFormats.fullDateFormat): String {
    return try {
        dateFormat.format(DateFormats.serverFullDateFormat.parse(this).time)
    } catch (e: Throwable) {
        dateFormat.format(DateFormats.serverDateFormat.parse(this).time)
    } catch (e: Exception) {
        e.print().let { this }
    }
}

fun parseServerDate(dateString: String, modifyTimeZone: Boolean = true): Date? {
    return try {
        try {
            val dateFormat = DateFormats.serverFullDateFormat

            if (modifyTimeZone) {
                dateFormat.timeZone = TimeZone.getDefault()
            }

            dateFormat.parse(dateString)
        } catch (e: Exception) {
            e.print()
            val dateFormat = DateFormats.serverDateFormat

            if (modifyTimeZone) {
                dateFormat.timeZone = TimeZone.getDefault()
            }

            dateFormat.parse(dateString)
        }
    } catch (e: Exception) {
        e.print()
        null
    }
}

fun formatDate(year: Int, month: Int, day: Int): String {
    return try {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        DateFormats.fullDateFormat.format(Date(calendar.timeInMillis))
    } catch (e: Exception) {
        e.print()
        ""
    }
}

fun Date?.formatDateRelyOnLocale(context: Context): String {
    return try {
        val dateFormat = DateFormat.getDateFormat(context)
        dateFormat.format(this)
    } catch (e: Exception) {
        e.print()
        ""
    }
}

fun Date?.formatDate(dateFormat: SimpleDateFormat = DateFormats.fullDateFormat): String {
    return try {

        dateFormat.format(this)
    } catch (e: Exception) {
        e.print()
        ""
    }
}

fun String?.formatServerDateByPattern(pattern: String): String {
    this ?: return ""
    return try {
        SimpleDateFormat(pattern, LOCALE_DEFAULT).format(DateFormats.serverFullDateFormat.parse(this))
    } catch (e: IllegalArgumentException) {
        e.print()
        ""
    }
}

fun isDateBeforeToday(year: Int, month: Int, day: Int): Boolean {
    val calendar = Calendar.getInstance()
    val todayYear = calendar.get(Calendar.YEAR)
    val todayMonth = calendar.get(Calendar.MONTH)

    if (year > todayYear) {
        return false
    }

    if (year < todayYear) {
        return true
    }

    return if (todayMonth != month) {
        month < todayMonth
    } else day < calendar.get(Calendar.DAY_OF_MONTH)

}

fun getDateRangeByDay(range: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, range)
    return DateFormats.serverFullDateFormat.format(calendar.time)
}

fun getDateRangeByMonth(range: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, range)
    return DateFormats.serverFullDateFormat.format(calendar.time)
}

fun getDateRangeByYear(range: Int): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.YEAR, range)
    return DateFormats.serverFullDateFormat.format(calendar.time)
}

fun durationConverter(duration: Long): String {

    val day = TimeUnit.SECONDS.toDays(duration).toInt()
    val hours = TimeUnit.SECONDS.toHours(duration) - TimeUnit.DAYS.toHours(day.toLong())
    val minute = TimeUnit.SECONDS.toMinutes(duration) - TimeUnit.SECONDS.toHours(duration) * 60

    return if (hours != 0L) {
        if (minute != 0L)
            "$hours h $minute min"
        else
            "$hours h"
    } else {
        "$minute min"
    }
}

fun getSystemCurrentLocale() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    Resources.getSystem().configuration.locales[0]
} else {
    Resources.getSystem().configuration.locale
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_PRETTY TIME_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

/**
 * Returns pretty time [String].
 * @param seconds - amount of seconds ago from now.
 * */
fun getTimeAgoFromSeconds(locale: Locale = Locale.ENGLISH, seconds: Long): String {
    return PrettyTime(locale).format(Date().apply {
        time = (time - TimeUnit.SECONDS.toMillis(seconds))
    })
}

