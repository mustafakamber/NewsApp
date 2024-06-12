package com.mustk.newsapp.ui.news.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentReadListBinding
import com.mustk.newsapp.ui.news.adapter.NewsAdapter
import com.mustk.newsapp.ui.news.viewmodel.ReadListViewModel
import com.mustk.newsapp.util.SwipeDecorator
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReadListFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentReadListBinding
    private val viewModel : ReadListViewModel by viewModels()
    @Inject lateinit var newsAdapter: NewsAdapter
    private lateinit var confirmation: ConfirmationFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupReadListScreen()
        observeLiveData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshReadListData()
    }

    private fun setupReadListScreen() = with(binding){
        newsAdapter.setOnNewsClickListener { uuid ->
            navigateDetailScreen(uuid)
        }
        readListRecyclerView.adapter = newsAdapter
        readListDeleteButton.setOnClickListener {
            confirmation = ConfirmationFragment(requireContext(), R.string.readlist_alert)
            confirmation.showConfirmationDialog {
                viewModel.deleteAllReadListNews()
            }
        }
        setupSwipeItem()
    }

    private fun navigateDetailScreen(newsUUID: String) {
        val action = ReadListFragmentDirections.actionReadListFragmentToDetailFragment(newsUUID,true)
        findNavController().navigate(action)
    }

    private fun setupSwipeItem(){
        val swipe = object : SwipeDecorator(requireActivity()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val news = newsAdapter.currentList[position]
                when (direction) {
                    ItemTouchHelper.LEFT, ItemTouchHelper.RIGHT -> {
                        viewModel.deleteNewsFromLocal(news)
                    }
                }
            }
        }
        val touchHelper = ItemTouchHelper(swipe)
        touchHelper.attachToRecyclerView(binding.readListRecyclerView)
    }

    private fun observeLiveData () = with(binding) {
        observe(viewModel.errorMessage) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.readListNews) { news ->
            newsAdapter.submitList(news)
        }
        observe(viewModel.recyclerViewVisibility) { boolean ->
            readListRecyclerView.isInvisible = !boolean
        }
        observe(viewModel.loading) { boolean ->
            readListLoadingBar.isInvisible = !boolean
        }
        observe(viewModel.emptyMessage) { boolean ->
            readListMessage.isInvisible = !boolean
        }
        observe(viewModel.deleteButton) { boolean ->
            readListDeleteButton.isInvisible = !boolean
        }
        observe(viewModel.snackbarMessage) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Snackbar.make(root, getString(message), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}