package com.mustk.newsapp.ui.news.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mustk.newsapp.databinding.FragmentDetailBinding
import com.mustk.newsapp.shared.Constant.SHARE_SCREEN_TITLE
import com.mustk.newsapp.shared.Constant.SHARE_SCREEN_TYPE
import com.mustk.newsapp.ui.news.adapter.NewsAdapter
import com.mustk.newsapp.ui.news.viewmodel.DetailViewModel
import com.mustk.newsapp.util.downloadImageFromURL
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment @Inject constructor() : Fragment() {

    private lateinit var binding : FragmentDetailBinding
    private val viewModel: DetailViewModel by viewModels()
    @Inject lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendInfoToViewModel()
        setupDetailScreen()
        observeLiveData()
    }

    private fun sendInfoToViewModel() {
        arguments?.apply {
            val args = DetailFragmentArgs.fromBundle(this)
            val newsUUID = args.newsUUID
            val isFromReadList = args.isFromReadList
            viewModel.fetchDetailNews(newsUUID,isFromReadList)
        }
    }

    private fun setupDetailScreen() = with(binding) {
        newsAdapter.setOnNewsClickListener { uuid ->
            restartDetailScreen(uuid)
        }
        detailRecyclerView.adapter = newsAdapter

        detailExitButton.setOnClickListener {
            findNavController().popBackStack()
        }
        detailSaveButton.setOnClickListener {
            viewModel.saveNews()
        }
        detailDeleteButton.setOnClickListener {
            viewModel.deleteNews()
        }
        detailShareButton.setOnClickListener {
            navigateToShareScreen()
        }
    }

    private fun navigateToShareScreen() {
        viewModel.detailNews.value?.newsUrl?.let { newsUrl ->
            val intentToShareScreen = Intent(Intent.ACTION_SEND)
            intentToShareScreen.apply {
                type = SHARE_SCREEN_TYPE
                putExtra(Intent.EXTRA_TEXT, newsUrl)
                startActivity(Intent.createChooser(this, SHARE_SCREEN_TITLE))
            }
        }
    }

    private fun restartDetailScreen(uuid: String) {
        val action = DetailFragmentDirections.actionDetailFragmentSelf(uuid,false)
        findNavController().navigate(action)
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.errorMessage) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.snackbarMessage) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Snackbar.make(root, getString(message), Snackbar.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.detailLoading) { boolean ->
            detailItemLoadingBar.isInvisible = !boolean
        }
        observe(viewModel.detailNews) { news ->
            with(news) {
                getShortSource()?.let {
                    detailSourceTextView.text = it.source
                }
                getFormattedCategories()?.let {
                    detailCategoriesTextView.text = it.categories
                }
                detailTitleTextView.text = title
                detailPosterImageView.downloadImageFromURL(imageUrl)
                detailDescriptionTextView.text = description
                getPublishedDateAndTime()?.let {
                    detailDateTextView.text = it.date
                    detailTimeTextView.text = it.time
                }
            }
        }
        observe(viewModel.similarLoading) { boolean ->
            detailSimilarLoadingBar.isInvisible = !boolean
        }
        observe(viewModel.similarNews) { similarNews ->
            newsAdapter.submitList(similarNews)
        }
        observe(viewModel.deleteButtonVisibility) { boolean ->
            detailDeleteButton.isInvisible = !boolean
        }
        observe(viewModel.saveButtonVisibility){ boolean ->
            detailSaveButton.isInvisible = !boolean
        }
        observe(viewModel.detailItems){ boolean ->
            detailItemScrollView.isInvisible = !boolean
        }
    }
}