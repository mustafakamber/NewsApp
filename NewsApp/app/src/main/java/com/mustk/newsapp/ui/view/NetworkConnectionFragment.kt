package com.mustk.newsapp.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentNetworkConnectionBinding
import com.mustk.newsapp.ui.viewmodel.NetworkConnectionViewModel
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NetworkConnectionFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentNetworkConnectionBinding
    private val viewModel: NetworkConnectionViewModel by viewModels()
    private lateinit var checkConnection: CheckConnectionFragment
    @Inject lateinit var navOptionsBuilder: NavOptions.Builder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNetworkConnectionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeLiveData()
    }

    private fun setupUI() {
        checkConnection = CheckConnectionFragment(requireContext())
        checkConnection.showCheckConnectionDialog {
            if (viewModel.checkNetworkConnection()) {
                checkConnection.dismissDialog()
                viewModel.navigateBasedOnUserActivationState()
            }
        }
    }

    private fun navigateToLoginScreen() {
        val action =
            NetworkConnectionFragmentDirections.actionNetworkConnectionFragmentToLoginFragment()
        val navOptions = navOptionsBuilder
            .setPopUpTo(R.id.networkConnectionFragment, true)
            .build()
        findNavController().apply {
            graph.setStartDestination(R.id.loginFragment)
            navigate(action, navOptions)
        }
    }

    private fun navigateToHomeScreen() {
        val action =
            NetworkConnectionFragmentDirections.actionNetworkConnectionFragmentToHomeFragment()
        val navOptions = navOptionsBuilder
            .setPopUpTo(R.id.networkConnectionFragment, true)
            .build()
        findNavController().apply {
            graph.setStartDestination(R.id.homeFragment)
            navigate(action, navOptions)
        }
    }

    private fun observeLiveData() {
        observe(viewModel.userAuthenticated) { event ->
            event.getContentIfNotHandled()?.let { isCurrentUserActive ->
                if (isCurrentUserActive){
                    lifecycleScope.launch {
                        delay(1000)
                        navigateToHomeScreen()
                    }
                }else{
                    navigateToLoginScreen()
                }
            }
        }
    }
}