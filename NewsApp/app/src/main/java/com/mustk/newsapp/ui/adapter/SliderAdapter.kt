package com.mustk.newsapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.databinding.SliderItemsBinding
import com.mustk.newsapp.util.downloadNewsImageFromURL
import com.smarteist.autoimageslider.SliderViewAdapter
import javax.inject.Inject

class SliderAdapter @Inject constructor() :
    SliderViewAdapter<SliderAdapter.Holder>() {

    inner class Holder(val binding: SliderItemsBinding) : ViewHolder(binding.root)

    private var newsSliderItems: MutableList<News> = ArrayList()
    private var onSliderNewsClickListener: ((String) -> Unit)? = null

    fun setOnSliderNewsClickListener(listener: (String) -> Unit) {
        onSliderNewsClickListener = listener
    }

    fun reloadSliderNews(sliderItems: List<News>) {
        this.newsSliderItems = sliderItems.toMutableList()
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return newsSliderItems.size
    }

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapter.Holder {
        val binding = SliderItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: SliderAdapter.Holder, position: Int) = with(holder) {
        binding.sliderImageView.downloadNewsImageFromURL(newsSliderItems[position].imageUrl)
        binding.slideTextView.text = newsSliderItems[position].title
        itemView.setOnClickListener {
            onSliderNewsClickListener?.let {
                it(newsSliderItems[position].uuid)
            }
        }
    }
}