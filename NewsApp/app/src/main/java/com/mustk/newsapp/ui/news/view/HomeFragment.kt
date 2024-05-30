package com.mustk.newsapp.ui.news.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.google.android.material.tabs.TabLayout
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentHomeBinding
import com.mustk.newsapp.shared.Constant.ECO_CATEGORIES
import com.mustk.newsapp.shared.Constant.ENTERTAINMENT_CATEGORIES
import com.mustk.newsapp.shared.Constant.GENERAL_CATEGORIES
import com.mustk.newsapp.shared.Constant.POLITIC_CATEGORIES
import com.mustk.newsapp.shared.Constant.SPORTS_CATEGORIES
import com.mustk.newsapp.shared.Constant.TECH_CATEGORIES
import com.mustk.newsapp.ui.AuthActivity
import com.mustk.newsapp.ui.news.adapter.NewsAdapter
import com.mustk.newsapp.ui.news.viewmodel.HomeViewModel
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    @Inject lateinit var newsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

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

    private fun setupViewModel() {
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
    }

    private fun setupHomeScreen() = with(binding) {
        button.setOnClickListener {
            viewModel.currentUserLogOut()
        }
        newsAdapter.setOnNewsClickListener { uuid ->
            navigateDetailScreen(uuid)
        }
        homeRecyclerView.adapter = newsAdapter
        setupTabLayout()
    }

    private fun setupTabLayout() = with(binding.homeTabLayout) {
        val tabTitles = listOf(
            R.string.home_general,
            R.string.home_politics,
            R.string.home_sport,
            R.string.home_tech,
            R.string.home_economy,
            R.string.home_entertainment
        )
        val tabCategories = listOf(
            GENERAL_CATEGORIES,
            POLITIC_CATEGORIES,
            SPORTS_CATEGORIES,
            TECH_CATEGORIES,
            ECO_CATEGORIES,
            ENTERTAINMENT_CATEGORIES
        )
        tabTitles.forEach { titleResId ->
            addTab(newTab().setText(getString(titleResId)))
        }
        addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewModel.fetchCategoryNewsFromAPI(tabCategories[it.position])
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun navigateDetailScreen(newsUUID: String) {
        val action = HomeFragmentDirections.actionHomeFragmentToDetailFragment(newsUUID)
        findNavController().navigate(action)
    }

    private fun navigateToLoginScreen() {
        val intentToAuthScreen = Intent(requireActivity(), AuthActivity::class.java)
        startActivity(intentToAuthScreen)
        requireActivity().finish()
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.navigateToLogin) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) navigateToLoginScreen()
            }
        }
        observe(viewModel.networkErrorMessage) { event ->
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
            headLineImageSlider.isInvisible = boolean
        }
        observe(viewModel.categoryLoading) { boolean ->
            homeCategoryLoadingBar.isInvisible = !boolean
            homeRecyclerView.isInvisible = boolean
        }
    }
}