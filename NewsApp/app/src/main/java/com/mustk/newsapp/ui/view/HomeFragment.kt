package com.mustk.newsapp.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.denzcoskun.imageslider.constants.ScaleTypes
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
import com.mustk.newsapp.ui.adapter.NewsAdapter
import com.mustk.newsapp.ui.viewmodel.HomeViewModel
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    @Inject lateinit var newsAdapter: NewsAdapter
    private lateinit var confirmation: ConfirmationFragment
    @Inject lateinit var navOptionsBuilder: NavOptions.Builder

    @Inject
    lateinit var adRequest: AdRequest

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

    override fun onResume() {
        super.onResume()
        checkLastTabPosition()
    }

    private fun setupHomeScreen() = with(binding) {
        homeAdView.loadAd(adRequest)
        signOutButton.setOnClickListener {
            confirmation = ConfirmationFragment(requireContext(),R.string.logout_alert)
            confirmation.showConfirmationDialog {
                viewModel.currentUserLogOut()
            }
        }
        homeSwipeRefreshLayout.setOnRefreshListener {
            viewModel.swipeRefreshState()
        }
        newsAdapter.setOnNewsClickListener { uuid ->
            navigateDetailScreen(uuid)
        }
        homeRecyclerView.adapter = newsAdapter
        setupTabLayout()
    }

    private fun setupTabLayout() = with(binding.homeTabLayout) {
        val tabTitles = listOf(
            R.string.tab_general,
            R.string.tab_politics,
            R.string.tab_sport,
            R.string.tab_tech,
            R.string.tab_economy,
            R.string.tab_entertainment,
            R.string.tab_science
        )
        val tabCategories = listOf(
            CATEGORY_GENERAL,
            CATEGORY_POLITIC,
            CATEGORY_SPORT,
            CATEGORY_TECH,
            CATEGORY_BUSINESS,
            CATEGORY_ENTERTAINMENT,
            CATEGORY_SCIENCE
        )
        tabTitles.forEach { titleResId ->
            addTab(newTab().setText(getString(titleResId)))
        }
        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewModel.fetchCategoryNewsFromAPI(tabCategories[it.position])
                    viewModel.setLastSelectedTabPosition(it.position)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun checkLastTabPosition() = with(binding) {
        viewModel.lastSelectedTabPosition.value?.let {
            homeTabLayout.getTabAt(it)?.select()
        }
    }

    private fun navigateDetailScreen(newsUUID: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(newsUUID,false)
        findNavController().navigate(action)
    }

    private fun navigateToLoginScreen() {
        val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
        val navOptions = navOptionsBuilder
            .setPopUpTo(R.id.homeFragment, true)
            .build()
        findNavController().apply {
            graph.setStartDestination(R.id.loginFragment)
            navigate(action, navOptions)
        }
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.navigateToLogin) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToLoginScreen()
            }
        }
        observe(viewModel.errorMessage) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.categoryNewsList) { news ->
            newsAdapter.submitList(news)
        }
        observe(viewModel.headlineNewsList) { news ->
            headLineImageSlider.setImageList(news, ScaleTypes.FIT)
        }
        observe(viewModel.headlineLoading) { boolean ->
            homeHeadlineLoadingBar.isInvisible = !boolean
        }
        observe(viewModel.categoryLoading) { boolean ->
            homeCategoryLoadingBar.isInvisible = !boolean
        }
        observe(viewModel.swipeRefreshLoading) { boolean ->
            homeSwipeRefreshLayout.isRefreshing = boolean
        }
        observe(viewModel.recyclerView) { boolean ->
            homeRecyclerView.isInvisible = !boolean
        }
        observe(viewModel.imageSliderView) { boolean ->
            headLineImageSlider.isInvisible = !boolean
        }
    }
}