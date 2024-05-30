package com.mustk.newsapp.di.factory

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.mustk.newsapp.ui.news.adapter.NewsAdapter
import com.mustk.newsapp.ui.news.view.HomeFragment
import javax.inject.Inject

class NewsFragmentFactory @Inject constructor(private val newsAdapter: NewsAdapter) :
    FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            HomeFragment::class.java.name -> HomeFragment()
            else -> super.instantiate(classLoader, className)
        }
    }
}
