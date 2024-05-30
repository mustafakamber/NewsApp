package com.mustk.newsapp.ui.news.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mustk.newsapp.data.model.News
import com.mustk.newsapp.databinding.RecyclerNewsRowBinding
import com.mustk.newsapp.util.downloadImageFromUrl
import javax.inject.Inject

class NewsAdapter @Inject constructor(): ListAdapter<News, NewsAdapter.NewsHolder>(MovieDiffCallback()) {

    private var onNewsClickListener: ((String) -> Unit)? = null

    inner class NewsHolder(private val binding: RecyclerNewsRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(news: News) = with(binding) {
            rowNewsPosterImageView.downloadImageFromUrl(news.imageUrl)
            rowNewsTitleTextView.text = news.title
            rowNewsSourceTextView.text = news.source
            news.publishedTime?.let {
                val newsDate = it.substring(0,10)
                val newsTime = it.substring(11,16)
                rowNewsDateTextView.text = newsDate
                rowNewsTimeTextView.text = newsTime
            }
            rowNewsCardView.setOnClickListener {
                news.uuid?.let { uuid ->
                    onNewsClickListener?.let {
                        it(uuid)
                    }
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

class MovieDiffCallback : DiffUtil.ItemCallback<News>() {
    override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem == newItem
    }
}