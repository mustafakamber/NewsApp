package com.mustk.newsapp.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.mustk.newsapp.databinding.FragmentSearchBinding
import com.mustk.newsapp.ui.adapter.NewsAdapter
import com.mustk.newsapp.ui.viewmodel.SearchViewModel
import com.mustk.newsapp.util.Constant.NO_CONNECTION
import com.mustk.newsapp.util.Constant.NULL_JSON
import com.mustk.newsapp.util.Status
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment  @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModels()
    @Inject lateinit var newsAdapter: NewsAdapter
    @Inject lateinit var adRequest: AdRequest
    private lateinit var checkConnection: CheckConnectionFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeLiveData()
    }

    private fun setupUI() = with(binding) {
        searchAdView.loadAd(adRequest)
        newsAdapter.setOnNewsClickListener { uuid ->
            navigateDetailScreen(uuid)
        }
        searchRecyclerView.adapter = newsAdapter
        searchEditText.addTextChangedListener {
            val query = it.toString().trim()
            viewModel.onSearchTextChange(query)
        }
    }

    private fun navigateToCheckConnectionScreen() {
        checkConnection = CheckConnectionFragment(requireContext())
        checkConnection.showCheckConnectionDialog {
            if (viewModel.isConnectedNetwork) {
                checkConnection.dismissDialog()
                val previousQuery = binding.searchEditText.text.toString().trim()
                viewModel.fetchSearchNewsListFromAPI(previousQuery)
            }
        }
    }

    private fun navigateDetailScreen(newsUUID : String){
        val action = SearchFragmentDirections.actionSearchFragmentToDetailFragment(newsUUID,false)
        findNavController().navigate(action)
    }

    private fun setSearchInit(boolean: Boolean) = with(binding) {
        searchInitMessage.isVisible = boolean
    }

    private fun setSearchLoading(boolean: Boolean) = with(binding) {
        searchLoadingBar.isVisible = boolean
    }

    private fun setSearchNetwork(boolean: Boolean) = with(binding) {
        searchNetworkMessage.isVisible = boolean
    }

    private fun setSearchError(boolean: Boolean) = with(binding) {
        searchErrorMessage.isVisible = boolean
    }

    private fun setSearchNotFound(boolean: Boolean) = with(binding) {
        searchNotFoundMessage.isVisible = boolean
    }

    private fun setRecyclerView(boolean: Boolean) = with(binding) {
        searchRecyclerView.isVisible = boolean
    }
    
    private fun observeLiveData() = with(binding) {
        observe(viewModel.initStatus) {
            if (it) {
                setSearchLoading(false)
                setSearchNotFound(false)
                setSearchError(false)
                setRecyclerView(false)
                setSearchNetwork(false)
                setSearchInit(true)
            }
        }
        observe(viewModel.newsList) {
            when (it.status) {
                Status.LOADING -> {
                    setSearchInit(false)
                    setRecyclerView(false)
                    setSearchError(false)
                    setSearchNotFound(false)
                    setSearchNetwork(false)
                    setSearchLoading(true)
                }
                Status.ERROR -> {
                    when (val errorMessage = it.message) {
                        NO_CONNECTION -> {
                            setSearchInit(false)
                            setSearchLoading(false)
                            setSearchNotFound(false)
                            setSearchError(false)
                            setRecyclerView(false)
                            setSearchNetwork(true)
                            navigateToCheckConnectionScreen()
                        }
                        NULL_JSON -> {
                            setSearchInit(false)
                            setSearchError(false)
                            setRecyclerView(false)
                            setSearchNetwork(false)
                            setSearchLoading(false)
                            setSearchNotFound(true)
                        }
                        else -> {
                            setSearchInit(false)
                            setSearchNotFound(false)
                            setRecyclerView(false)
                            setSearchNetwork(false)
                            setSearchLoading(false)
                            setSearchError(true)
                            searchErrorTextView.text = errorMessage
                        }
                    }
                }
                Status.SUCCESS -> {
                    setSearchLoading(false)
                    setSearchNotFound(false)
                    setSearchError(false)
                    setSearchNetwork(false)
                    setRecyclerView(true)
                    it.data?.let { searchNews ->
                        newsAdapter.submitList(searchNews)
                    }
                }
            }
        }
    }
}