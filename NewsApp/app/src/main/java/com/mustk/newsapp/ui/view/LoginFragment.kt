package com.mustk.newsapp.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentLoginBinding
import com.mustk.newsapp.ui.viewmodel.LoginViewModel
import com.mustk.newsapp.ui.viewmodel.SplashViewModel
import com.mustk.newsapp.util.observe
import com.mustk.newsapp.util.slideDown
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel : LoginViewModel by viewModels()
    private val splashViewModel: SplashViewModel by activityViewModels()
    @Inject lateinit var googleSignInClient: GoogleSignInClient
    @Inject lateinit var navOptionsBuilder: NavOptions.Builder
    private lateinit var checkConnection: CheckConnectionFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeLiveData()
    }

    private fun setupUI() = with(binding) {
        loginTitleText.slideDown()
        loginEmailEditText.addTextChangedListener {
            setEmailEndIcon(true)
        }
        loginPasswordEditText.addTextChangedListener {
            setPasswordEndIcon(true)
        }
        loginButton.setOnClickListener {
            val email = loginEmailEditText.text.toString().trim()
            val password = loginPasswordEditText.text.toString().trim()
            viewModel.inputValidate(email, password)
        }
        signupText.setOnClickListener {
            navigateToSignupScreen()
        }
        forgotPasswordText.setOnClickListener {
            navigateToPasswordScreen()
        }
        googleButton.setOnClickListener {
            navigateToGoogleSignInScreen()
        }
    }

    private fun setEmailEndIcon(isVisible: Boolean) = with(binding) {
        loginEmailInputLayout.isEndIconVisible = isVisible
    }

    private fun setPasswordEndIcon(isVisible: Boolean) = with(binding) {
        loginPasswordInputLayout.isEndIconVisible = isVisible
    }

    private fun navigateToGoogleSignInScreen() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.handleSignInGoogleRequest(result)
        }

    private fun navigateToPasswordScreen() {
        val action = LoginFragmentDirections.actionLoginFragmentToPasswordFragment(null)
        findNavController().navigate(action)
    }

    private fun navigateToNetworkConnectionScreen() {
        val action = LoginFragmentDirections.actionLoginFragmentToNetworkConnectionFragment()
        val navOptions = navOptionsBuilder
            .setPopUpTo(R.id.loginFragment, true)
            .build()
        findNavController().apply {
            graph.setStartDestination(R.id.networkConnectionFragment)
            navigate(action, navOptions)
        }
    }

    private fun navigateToSignupScreen() {
        val action = LoginFragmentDirections.actionLoginFragmentToSignupFragment()
        findNavController().navigate(action)
    }

    private fun navigateToHomeScreen() {
        val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
        val navOptions = navOptionsBuilder
            .setPopUpTo(R.id.loginFragment, true)
            .build()
        findNavController().apply {
            graph.setStartDestination(R.id.homeFragment)
            navigate(action, navOptions)
        }
    }

    private fun navigateToCheckConnectionScreen() {
        checkConnection = CheckConnectionFragment(requireContext())
        checkConnection.showCheckConnectionDialog {
            if (viewModel.isConnectedNetwork()) {
                checkConnection.dismissDialog()
                viewModel.checkCurrentUser()
            }
        }
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.currentUserActive) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToHomeScreen()
            }
        }
        observe(viewModel.emailError) { error ->
            error.let {
                loginEmailEditText.error = getString(it)
                setEmailEndIcon(false)
            }
        }
        observe(viewModel.passwordError) { error ->
            error.let {
                loginPasswordEditText.error = getString(it)
                setPasswordEndIcon(false)
            }
        }
        observe(viewModel.authError) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
        observe(viewModel.success) { event ->
            event.getContentIfNotHandled()?.let { boolean ->
                if (boolean){
                    lifecycleScope.launch {
                        delay(500)
                        navigateToHomeScreen()
                    }
                }
            }
        }
        observe(viewModel.checkNetworkConnection) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToCheckConnectionScreen()
            }
        }
        observe(splashViewModel.userAuthenticated) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToHomeScreen()
            }
        }
        observe(splashViewModel.checkNetworkConnection) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToNetworkConnectionScreen()
            }
        }
    }
}