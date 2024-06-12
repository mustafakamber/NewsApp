package com.mustk.newsapp.util

import android.view.animation.AnimationUtils
import android.widget.TextView
import com.mustk.newsapp.R

fun TextView.slideDown(){
    val slideDown = AnimationUtils.loadAnimation(this.context, R.anim.slide_down)
    this.startAnimation(slideDown)
}