package com.example.chronosyne.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Remove the class wrapper
fun Date.formatAsDate(): String {
    val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return format.format(this)
}

fun Date.addDays(days: Int): Date {
    return Calendar.getInstance().apply {
        time = this@addDays
        add(Calendar.DAY_OF_YEAR, days)
    }.time
}