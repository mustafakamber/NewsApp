package com.mustk.newsapp.ui.news.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.mustk.newsapp.databinding.FragmentReadListBinding
import com.mustk.newsapp.ui.news.viewmodel.ReadListViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReadListFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentReadListBinding
    private lateinit var viewModel : ReadListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReadListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(requireActivity())[ReadListViewModel::class.java]
    }
}