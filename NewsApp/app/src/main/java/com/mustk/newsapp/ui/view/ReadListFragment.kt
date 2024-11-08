package com.mustk.newsapp.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentReadListBinding
import com.mustk.newsapp.ui.adapter.NewsAdapter
import com.mustk.newsapp.ui.viewmodel.ReadListViewModel
import com.mustk.newsapp.util.Constant.NULL_JSON
import com.mustk.newsapp.util.Status
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
    private lateinit var checkConnection: CheckConnectionFragment
    private var loading: LoadingFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeLiveData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshScreen()
    }

    private fun setupUI() = with(binding){
        loading = LoadingFragment(requireContext())
        newsAdapter.setOnNewsClickListener { uuid ->
            navigateDetailScreen(uuid)
        }
        readListRecyclerView.adapter = newsAdapter
        readListDeleteButton.setOnClickListener {
            confirmation = ConfirmationFragment(requireContext(), R.string.readlist_alert)
            confirmation.showConfirmationDialog {
                viewModel.deleteAllNews()
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
                        viewModel.deleteNews(news)
                    }
                }
            }
        }
        val touchHelper = ItemTouchHelper(swipe)
        touchHelper.attachToRecyclerView(binding.readListRecyclerView)
    }

    private fun setLoading(boolean: Boolean) = with(binding) {
        readListLoadingBar.isVisible = boolean
    }

    private fun setEmptyMessage(boolean: Boolean) = with(binding) {
        readListEmptyMessage.isVisible = boolean
    }

    private fun setErrorMessage(boolean: Boolean) = with(binding) {
        readListErrorMessage.isVisible = boolean
    }

    private fun setRecyclerView(boolean: Boolean) = with(binding) {
        readListRecyclerView.isVisible = boolean
    }

    private fun setDeleteButton(boolean: Boolean) = with(binding) {
        readListDeleteButton.isVisible = boolean
    }

    private fun navigateToCheckConnectionScreen() {
        checkConnection = CheckConnectionFragment(requireContext())
        checkConnection.showCheckConnectionDialog {
            if (viewModel.isConnectedNetwork) {
                checkConnection.dismissDialog()
            }
        }
    }

    private fun observeLiveData () = with(binding) {
        observe(viewModel.readListNews) {
            when (it.status) {
                Status.LOADING -> {
                    setDeleteButton(false)
                    setEmptyMessage(false)
                    setErrorMessage(false)
                    setRecyclerView(false)
                    setLoading(true)
                }
                Status.ERROR -> {
                    when (val errorMessage = it.message) {
                        NULL_JSON -> {
                            setDeleteButton(false)
                            setErrorMessage(false)
                            setRecyclerView(false)
                            setLoading(false)
                            setEmptyMessage(true)
                        }
                        else -> {
                            setDeleteButton(false)
                            setRecyclerView(false)
                            setLoading(false)
                            setEmptyMessage(false)
                            setErrorMessage(true)
                            readListErrorTextView.text = errorMessage
                        }
                    }
                }
                Status.SUCCESS -> {
                    setLoading(false)
                    setEmptyMessage(false)
                    setErrorMessage(false)
                    setRecyclerView(true)
                    setDeleteButton(true)
                    it.data?.let { readListNews ->
                        newsAdapter.submitList(readListNews)
                    }
                }
            }
        }
        observe(viewModel.snackBarMessage) { event ->
            event.getContentIfNotHandled()?.let {
                    message ->
                Snackbar.make(root, getString(message), Snackbar.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.toastError) { error ->
            Toast.makeText(requireContext(),error,Toast.LENGTH_SHORT)
                .show()
        }
        observe(viewModel.checkConnection) { event ->
            event.getContentIfNotHandled()?.let {
                if (it){
                    navigateToCheckConnectionScreen()
                }
            }
        }
    }
}