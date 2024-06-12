package com.mustk.newsapp.ui.news.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
    private val viewModel: SearchViewModel by viewModels()
    @Inject lateinit var newsAdapter: NewsAdapter

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

    private fun setupSearchScreen() = with(binding){
        newsAdapter.setOnNewsClickListener { uuid ->
            navigateDetailScreen(uuid)
        }
        searchRecyclerView.adapter = newsAdapter
        searchEditText.addTextChangedListener {
            val query = it.toString()
            viewModel.onSearchTextChange(query)
        }
    }

    private fun navigateDetailScreen(newsUUID : String){
        val action = SearchFragmentDirections.actionSearchFragmentToDetailFragment(newsUUID,false)
        findNavController().navigate(action)
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.initMessage) { boolean ->
            searchInitMessage.isVisible = boolean
        }
        observe(viewModel.notFoundMessage) { boolean ->
            searchNotFoundMessage.isVisible = boolean
        }
        observe(viewModel.recyclerView){ boolean ->
            searchRecyclerView.isVisible = boolean
        }
        observe(viewModel.loading) { boolean ->
            searchLoadingBar.isVisible = boolean
        }
        observe(viewModel.newsList){ news ->
            newsAdapter.submitList(news)
        }
        observe(viewModel.errorMessage) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}