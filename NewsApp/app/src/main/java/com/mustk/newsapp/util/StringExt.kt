package com.mustk.newsapp.util

import java.util.Locale

fun String.truncateString(maxLength: Int) : String {
    val truncatedText = if (this.length > maxLength) {
        "${this.substring(0, maxLength)}.."
    } else {
        this
    }
    return truncatedText
}
fun String.capitalized(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(Locale.getDefault())
        else it.toString()
    }
}