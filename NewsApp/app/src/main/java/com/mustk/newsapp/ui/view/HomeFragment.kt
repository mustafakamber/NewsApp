package com.mustk.newsapp.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayout
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentHomeBinding
import com.mustk.newsapp.shared.Constant.CATEGORY_BUSINESS
import com.mustk.newsapp.shared.Constant.CATEGORY_ENTERTAINMENT
import com.mustk.newsapp.shared.Constant.CATEGORY_GENERAL
import com.mustk.newsapp.shared.Constant.CATEGORY_POLITIC
import com.mustk.newsapp.shared.Constant.CATEGORY_SCIENCE
import com.mustk.newsapp.shared.Constant.CATEGORY_SPORT
import com.mustk.newsapp.shared.Constant.CATEGORY_TECH
import com.mustk.newsapp.shared.Constant.LANGUAGE_AR
import com.mustk.newsapp.shared.Constant.LANGUAGE_DE
import com.mustk.newsapp.shared.Constant.LANGUAGE_EN
import com.mustk.newsapp.shared.Constant.LANGUAGE_ES
import com.mustk.newsapp.shared.Constant.LANGUAGE_FR
import com.mustk.newsapp.shared.Constant.LANGUAGE_IT
import com.mustk.newsapp.shared.Constant.LANGUAGE_TR
import com.mustk.newsapp.ui.adapter.NewsAdapter
import com.mustk.newsapp.ui.adapter.SliderAdapter
import com.mustk.newsapp.ui.viewmodel.HomeViewModel
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    @Inject lateinit var newsAdapter: NewsAdapter
    @Inject lateinit var adRequest: AdRequest
    @Inject lateinit var sliderAdapter: SliderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHomeScreen()
        observeLiveData()
    }

    override fun onPause() {
        super.onPause()
        viewModel.setSlideListClear()
    }

    override fun onResume() {
        super.onResume()
        checkLastTabPosition()
        viewModel.refreshHomeData()
    }

    private fun setupHomeScreen() = with(binding) {
        homeAdView.loadAd(adRequest)
        homeSwipeRefreshLayout.setOnRefreshListener {
            viewModel.swipeRefreshClicked()
        }
        sliderAdapter.setOnSliderNewsClickListener { uuid ->
            navigateDetailScreen(uuid)
        }
        headLineImageSlider.setSliderAdapter(sliderAdapter)
        newsAdapter.setOnNewsClickListener { uuid ->
            navigateDetailScreen(uuid)
        }
        homeRecyclerView.adapter = newsAdapter
        setupCountryTabLayout()
        setupCategoryTabLayout()
    }

    private fun setupCountryTabLayout() = with(binding.homeCountryTabLayout) {
        val languageItems = listOf(
            R.string.spinner_uk, R.string.spinner_tr,
            R.string.spinner_fr, R.string.spinner_es, R.string.spinner_it,
            R.string.spinner_de, R.string.spinner_ar
        )
        val newsLanguages = listOf(
            LANGUAGE_EN, LANGUAGE_TR, LANGUAGE_FR,
            LANGUAGE_ES, LANGUAGE_IT, LANGUAGE_DE,
            LANGUAGE_AR
        )
        languageItems.forEach { titleResId ->
            addTab(newTab().setText(getString(titleResId)))
        }
        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewModel.onLanguageChange(newsLanguages[it.position], it.position)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupCategoryTabLayout() = with(binding.homeCategoryTabLayout) {
        val tabTitles = listOf(
            R.string.tab_general, R.string.tab_politics, R.string.tab_sport,
            R.string.tab_tech, R.string.tab_economy, R.string.tab_entertainment,
            R.string.tab_science
        )
        val tabCategories = listOf(
            CATEGORY_GENERAL, CATEGORY_POLITIC, CATEGORY_SPORT,
            CATEGORY_TECH, CATEGORY_BUSINESS, CATEGORY_ENTERTAINMENT,
            CATEGORY_SCIENCE
        )
        tabTitles.forEach { titleResId ->
            addTab(newTab().setText(getString(titleResId)))
        }
        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewModel.onCategoryChange(tabCategories[it.position], it.position)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    private fun checkLastTabPosition() = with(binding) {
        viewModel.lastSelectedTabCategoryPosition.value?.let {
            homeCategoryTabLayout.getTabAt(it)?.select()
        }
        viewModel.lastSelectedTabLanguagePosition.value?.let {
            homeCountryTabLayout.getTabAt(it)?.select()
        }
    }

    private fun navigateDetailScreen(newsUUID: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(newsUUID,false)
        findNavController().navigate(action)
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.errorMessage) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.categoryNewsList) { news ->
            newsAdapter.submitList(news)
        }
        observe(viewModel.headlineNewsList) { news ->
            sliderAdapter.reloadSliderNews(news)
            headLineImageSlider.startAutoCycle()
            headLineImageSlider.setIndicatorVisibility(true)
        }
        observe(viewModel.headlineLoading) { boolean ->
            homeHeadlineLoadingBar.isInvisible = !boolean
        }
        observe(viewModel.categoryLoading) { boolean ->
            homeCategoryLoadingBar.isInvisible = !boolean
        }
        observe(viewModel.categoryNullMessage) { boolean ->
            homeNotFoundMessage.isInvisible = !boolean
        }
        observe(viewModel.swipeRefreshLoading) { boolean ->
            homeSwipeRefreshLayout.isRefreshing = boolean
        }
        observe(viewModel.recyclerView) { boolean ->
            homeRecyclerView.isInvisible = !boolean
        }
        observe(viewModel.imageSliderView) { boolean ->
            imageSliderCardView.isInvisible = !boolean
            headLineImageSlider.isInvisible = !boolean
        }
    }
}