package com.mustk.newsapp.util

import android.content.Context
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mustk.newsapp.R

fun ImageView.downloadNewsImageFromURL(url: String?) {
    val options = RequestOptions()
        .placeholder(placeHolderProgressBar(this.context))
        .error(R.drawable.poster)
    Glide.with(this.context)
        .setDefaultRequestOptions(options)
        .load(url)
        .into(this)
}

fun ImageView.downloadProfileImageFromURL(url: String?) {
    val options = RequestOptions()
        .placeholder(placeHolderProgressBar(this.context))
        .error(R.drawable.default_profile_image)
    Glide.with(this.context)
        .setDefaultRequestOptions(options)
        .load(url)
        .into(this)
}

fun placeHolderProgressBar(context : Context) : CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 8f
        centerRadius = 40f
        start()
    }
}