package com.mustk.newsapp.ui.news.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.mustk.newsapp.databinding.FragmentLoginBinding
import com.mustk.newsapp.ui.NewsActivity
import com.mustk.newsapp.ui.news.viewmodel.LoginViewModel
import com.mustk.newsapp.util.observe
import com.mustk.newsapp.util.slideDown
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    @Inject lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupLoginScreen()
        observeLiveData()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
    }

    private fun setupLoginScreen() = with(binding) {
        loginTitleText.slideDown()
        loginEmailEditText.addTextChangedListener {
            viewModel.setVisibleEmailEndIcon()
        }
        loginPasswordEditText.addTextChangedListener {
            viewModel.setVisiblePasswordEndIcon()
        }
        loginButton.setOnClickListener {
            val emailAddress = loginEmailEditText.text.toString().trim()
            val password = loginPasswordEditText.text.toString().trim()
            viewModel.inputCheckForLogin(emailAddress, password)
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

    private fun navigateToGoogleSignInScreen() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            viewModel.handleSignInGoogleRequest(result)
        }

    private fun navigateToPasswordScreen() {
        val action = LoginFragmentDirections.actionLoginFragmentToPasswordFragment()
        findNavController().navigate(action)
    }

    private fun navigateToSignupScreen() {
        val action = LoginFragmentDirections.actionLoginFragmentToSignupFragment()
        findNavController().navigate(action)
    }

    private fun navigateToHomeScreen() {
        val intentToNewsScreen = Intent(requireActivity(), NewsActivity::class.java)
        startActivity(intentToNewsScreen)
        requireActivity().finish()
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.emailErrorText) { emailError ->
            loginEmailEditText.error = getString(emailError)
        }
        observe(viewModel.emailEndIconVisible) { boolean ->
            loginEmailInputLayout.isEndIconVisible = boolean
        }
        observe(viewModel.passwordErrorText) { passwordError ->
            loginPasswordEditText.error = getString(passwordError)
        }
        observe(viewModel.passwordEndIconVisible) { boolean ->
            loginPasswordInputLayout.isEndIconVisible = boolean
        }
        observe(viewModel.networkErrorMessage) { event ->
            event.getContentIfNotHandled()?.let { toastMessage ->
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.navigateToHome) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) navigateToHomeScreen()
            }
        }
    }
}