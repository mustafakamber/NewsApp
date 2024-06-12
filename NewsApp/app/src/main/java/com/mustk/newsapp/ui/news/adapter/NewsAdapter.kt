package com.mustk.newsapp.ui.news.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.databinding.RecyclerNewsRowBinding
import com.mustk.newsapp.shared.Constant.SHORT_TITLE_MAX_SIZE
import com.mustk.newsapp.util.downloadImageFromUrl
import com.mustk.newsapp.util.truncateString
import javax.inject.Inject

class NewsAdapter @Inject constructor() :
    ListAdapter<News, NewsAdapter.NewsHolder>(NewsDiffCallback()) {

    private var onNewsClickListener: ((String) -> Unit)? = null

    inner class NewsHolder(private val binding: RecyclerNewsRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(news: News) = with(binding) {
            news.imageUrl?.let {
                rowNewsPosterImageView.downloadImageFromUrl(it)
            }
            news.title?.let {
                rowNewsTitleTextView.text = it.truncateString(SHORT_TITLE_MAX_SIZE)
            }
            news.getShortSource()?.let {
                rowNewsSourceTextView.text = it.source
            }
            news.getPublishedDateAndTime()?.let {
                rowNewsDateTextView.text = it.date
                rowNewsTimeTextView.text = it.time
            }
            rowNewsCardView.setOnClickListener {
                onNewsClickListener?.let {
                    it(news.uuid)
                }
            }
        }
    }

    fun setOnNewsClickListener(listener: (String) -> Unit) {
        onNewsClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsHolder {
        val binding =
            RecyclerNewsRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class NewsDiffCallback : DiffUtil.ItemCallback<News>() {
    override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem.uuid == newItem.uuid
    }
}