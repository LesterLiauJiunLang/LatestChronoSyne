package com.example.chronosyne.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateUtils {
    fun Date.formatAsDate(): String {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return format.format(this)
    }
}