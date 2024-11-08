package com.mustk.newsapp.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayout
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentHomeBinding
import com.mustk.newsapp.ui.adapter.NewsAdapter
import com.mustk.newsapp.ui.adapter.SliderAdapter
import com.mustk.newsapp.ui.viewmodel.HomeViewModel
import com.mustk.newsapp.util.Constant.CATEGORY_BUSINESS
import com.mustk.newsapp.util.Constant.CATEGORY_ENTERTAINMENT
import com.mustk.newsapp.util.Constant.CATEGORY_GENERAL
import com.mustk.newsapp.util.Constant.CATEGORY_POLITIC
import com.mustk.newsapp.util.Constant.CATEGORY_SCIENCE
import com.mustk.newsapp.util.Constant.CATEGORY_SPORT
import com.mustk.newsapp.util.Constant.CATEGORY_TECH
import com.mustk.newsapp.util.Constant.LANGUAGE_AR
import com.mustk.newsapp.util.Constant.LANGUAGE_BG
import com.mustk.newsapp.util.Constant.LANGUAGE_DE
import com.mustk.newsapp.util.Constant.LANGUAGE_EN
import com.mustk.newsapp.util.Constant.LANGUAGE_ES
import com.mustk.newsapp.util.Constant.LANGUAGE_FR
import com.mustk.newsapp.util.Constant.LANGUAGE_GR
import com.mustk.newsapp.util.Constant.LANGUAGE_ID
import com.mustk.newsapp.util.Constant.LANGUAGE_IN
import com.mustk.newsapp.util.Constant.LANGUAGE_IT
import com.mustk.newsapp.util.Constant.LANGUAGE_JP
import com.mustk.newsapp.util.Constant.LANGUAGE_KO
import com.mustk.newsapp.util.Constant.LANGUAGE_NL
import com.mustk.newsapp.util.Constant.LANGUAGE_PT
import com.mustk.newsapp.util.Constant.LANGUAGE_RO
import com.mustk.newsapp.util.Constant.LANGUAGE_RU
import com.mustk.newsapp.util.Constant.LANGUAGE_TR
import com.mustk.newsapp.util.Constant.LANGUAGE_UK
import com.mustk.newsapp.util.Constant.LANGUAGE_ZH
import com.mustk.newsapp.util.Constant.NO_CONNECTION
import com.mustk.newsapp.util.Constant.NULL_JSON
import com.mustk.newsapp.util.Status
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    @Inject lateinit var newsAdapter: NewsAdapter
    @Inject lateinit var adRequest: AdRequest
    @Inject lateinit var sliderAdapter: SliderAdapter
    private lateinit var checkConnection: CheckConnectionFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeLiveData()
    }

    override fun onResume() {
        super.onResume()
        checkLastTabPosition()
    }

    private fun setupUI() = with(binding) {
        homeAdView.loadAd(adRequest)
        homeSwipeRefreshLayout.setOnRefreshListener {
            setSwipeRefreshing(true)
            viewModel.refreshScreen()
            lifecycleScope.launch {
                delay(500)
                setSwipeRefreshing(false)
            }
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
            R.string.spinner_gb, R.string.spinner_tr,
            R.string.spinner_fr, R.string.spinner_nl, R.string.spinner_de,
            R.string.spinner_es, R.string.spinner_pt, R.string.spinner_it,
            R.string.spinner_gr, R.string.spinner_ro, R.string.spinner_uk,
            R.string.spinner_bg, R.string.spinner_ar, R.string.spinner_in,
            R.string.spinner_id, R.string.spinner_ru, R.string.spinner_kr,
            R.string.spinner_zh, R.string.spinner_jp
        )
        val newsLanguages = listOf(
            LANGUAGE_EN, LANGUAGE_TR,
            LANGUAGE_FR, LANGUAGE_NL, LANGUAGE_DE,
            LANGUAGE_ES, LANGUAGE_PT, LANGUAGE_IT,
            LANGUAGE_GR, LANGUAGE_RO, LANGUAGE_UK,
            LANGUAGE_BG, LANGUAGE_AR, LANGUAGE_IN,
            LANGUAGE_ID, LANGUAGE_RU, LANGUAGE_KO,
            LANGUAGE_ZH, LANGUAGE_JP
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

    private fun navigateToCheckConnectionScreen() {
        checkConnection = CheckConnectionFragment(requireContext())
        checkConnection.showCheckConnectionDialog {
            if (viewModel.isConnectedNetwork) {
                checkConnection.dismissDialog()
                viewModel.refreshData()
            }
        }
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.headlineNewsList) {
            when (it.status) {
                Status.LOADING -> {
                    setImageSlider(false)
                    setHeadlineNetwork(false)
                    setHeadlineError(false)
                    setHeadlineLoading(true)
                }

                Status.ERROR -> {
                    val errorMessage = it.message
                    if (errorMessage == NO_CONNECTION) {
                        setHeadlineLoading(false)
                        setImageSlider(false)
                        setHeadlineError(false)
                        setHeadlineNetwork(true)
                        navigateToCheckConnectionScreen()
                    } else {
                        setImageSlider(false)
                        setHeadlineNetwork(false)
                        setHeadlineLoading(false)
                        setHeadlineError(true)
                        binding.homeHeadlineErrorTextView.text = errorMessage
                    }
                }

                Status.SUCCESS -> {
                    setHeadlineNetwork(false)
                    setHeadlineLoading(false)
                    setHeadlineError(false)
                    setImageSlider(true)
                    it.data?.let { headlineNews ->
                        sliderAdapter.reloadSliderNews(headlineNews)
                        headLineImageSlider.startAutoCycle()
                    }
                }
            }
        }
        observe(viewModel.categoryNewsList) {
            when (it.status) {
                Status.LOADING -> {
                    setCategoryNotFound(false)
                    setCategoryError(false)
                    setRecyclerView(false)
                    setCategoryNetwork(false)
                    setCategoryLoading(true)
                }

                Status.ERROR -> {
                    when (val errorMessage = it.message) {
                        NO_CONNECTION -> {
                            setCategoryLoading(false)
                            setCategoryNotFound(false)
                            setCategoryError(false)
                            setRecyclerView(false)
                            setCategoryNetwork(true)
                        }

                        NULL_JSON -> {
                            setCategoryError(false)
                            setRecyclerView(false)
                            setCategoryNetwork(false)
                            setCategoryLoading(false)
                            setCategoryNotFound(true)
                        }

                        else -> {
                            setCategoryNotFound(false)
                            setRecyclerView(false)
                            setCategoryNetwork(false)
                            setCategoryLoading(false)
                            setCategoryError(true)
                            homeCategoryErrorTextView.text = errorMessage
                        }
                    }
                }

                Status.SUCCESS -> {
                    setCategoryLoading(false)
                    setCategoryNotFound(false)
                    setCategoryError(false)
                    setCategoryNetwork(false)
                    setRecyclerView(true)

                    it.data.let { categoryNews ->
                        newsAdapter.submitList(categoryNews)
                    }
                }
            }
        }
    }

    private fun setSwipeRefreshing(boolean: Boolean) = with(binding) {
        homeSwipeRefreshLayout.isRefreshing = boolean
    }

    private fun setHeadlineLoading(boolean: Boolean) = with(binding) {
        homeHeadlineLoadingBar.isInvisible = !boolean
    }

    private fun setHeadlineNetwork(boolean: Boolean) = with(binding) {
        homeHeadlineNetworkMessage.isInvisible = !boolean
    }

    private fun setHeadlineError(boolean: Boolean) = with(binding) {
        homeHeadlineErrorMessage.isInvisible = !boolean
    }

    private fun setImageSlider(boolean: Boolean) = with(binding) {
        imageSliderCardView.isInvisible = !boolean
        headLineImageSlider.isInvisible = !boolean
    }

    private fun setCategoryLoading(boolean: Boolean) = with(binding) {
        homeCategoryLoadingBar.isInvisible = !boolean
    }

    private fun setCategoryNetwork(boolean: Boolean) = with(binding) {
        homeCategoryNetworkMessage.isInvisible = !boolean
    }

    private fun setCategoryError(boolean: Boolean) = with(binding) {
        homeCategoryErrorMessage.isInvisible = !boolean
    }

    private fun setCategoryNotFound(boolean: Boolean) = with(binding) {
        homeNotFoundMessage.isInvisible = !boolean
    }

    private fun setRecyclerView(boolean: Boolean) = with(binding) {
        homeRecyclerView.isGone = !boolean
    }
}