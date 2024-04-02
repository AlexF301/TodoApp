package com.android.todoapp

import java.text.SimpleDateFormat
import java.util.*

/**
 * Get the textual representation of the time like "3:42 pm".
 * @return the String like "3:42 pm"
 */
fun Date.toTimeString(): String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(time)

/**
 *
 */
fun Date.formatDateString(): String {
    val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(time)
}

/**
 * Returns a new Date that has the time of "this" and the date of the argument.
 * The time on the date is ignored and the date on the time is ignored. This is
 * just the reverse of combineWithTime() for convenience.
 * @param date the Date to use for the date portion
 * @return a Date that is the combination of the date and time
 */
fun Date.combineWithDate(date: Date): Date = date.combineWithTime(this)
/**
 * Returns a new Date that has the date of "this" and the time of the argument.
 * The time on the date is ignored and the date on the time is ignored.
 * @param time the Date to use for the time portion
 * @return a Date that is the combination of the date and time
 */

fun Date.combineWithTime(time: Date): Date {
    val calDate = Calendar.getInstance()
    val calTime = Calendar.getInstance()
    calDate.time = this
    calTime.time = time
    return GregorianCalendar(
        calDate[Calendar.YEAR], calDate[Calendar.MONTH], calDate[Calendar.DAY_OF_MONTH],
        calTime[Calendar.HOUR_OF_DAY], calTime[Calendar.MINUTE]
    ).time
}

/**
 * Get the textual representation of a date NOT including the day of the week like
 * "April 1, 2021".
 * @return the String like "April 1, 2021"
 */
fun Date.toDateString(): String = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(time)