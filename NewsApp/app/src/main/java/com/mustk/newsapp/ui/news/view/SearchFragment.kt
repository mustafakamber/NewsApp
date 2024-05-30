package com.mustk.newsapp.ui.news.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mustk.newsapp.databinding.FragmentSearchBinding
import com.mustk.newsapp.ui.news.adapter.NewsAdapter
import com.mustk.newsapp.ui.news.viewmodel.SearchViewModel
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment  @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: SearchViewModel
    @Inject lateinit var newsAdapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearchScreen()
        observeLiveData()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
    }

    private fun setupSearchScreen() = with(binding){
        newsAdapter.setOnNewsClickListener { uuid ->
            navigateDetailScreen(uuid)
        }
        searchRecyclerView.adapter = newsAdapter
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean = false
            override fun onQueryTextChange(keyQuery: String): Boolean {
                viewModel.onSearchTextChange(keyQuery)
                return true
            }
        })
    }

    private fun navigateDetailScreen(newsUUID : String){
        val action = SearchFragmentDirections.actionSearchFragmentToDetailFragment(newsUUID)
        findNavController().navigate(action)
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.searchInitMessage) { boolean ->
            searchMessage.isVisible = boolean
        }
        observe(viewModel.searchNewsList){ news ->
            newsAdapter.submitList(news)
        }
        observe(viewModel.searchLoading) { boolean ->
            searchLoadingBar.isInvisible = !boolean
            searchRecyclerView.isInvisible = boolean
        }
        observe(viewModel.networkErrorMessage) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}