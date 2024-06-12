package com.mustk.newsapp.ui.news.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mustk.newsapp.databinding.FragmentSignupBinding
import com.mustk.newsapp.ui.NewsActivity
import com.mustk.newsapp.ui.news.viewmodel.SignupViewModel
import com.mustk.newsapp.util.observe
import com.mustk.newsapp.util.slideDown
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignupFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private val viewModel: SignupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSignupScreen()
        observeLiveData()
    }

    private fun setupSignupScreen() = with(binding) {
        signupTitleText.slideDown()
        signupEmailEditText.addTextChangedListener {
            viewModel.setEmailEndIcon(true)
        }
        signupPasswordEditText.addTextChangedListener {
            viewModel.setPasswordEndIcon(true)
        }
        signupConfirmPasswordEditText.addTextChangedListener {
            viewModel.setConfirmPasswordEndIcon(true)
        }
        loginText.setOnClickListener {
            findNavController().popBackStack()
        }
        signupButton.setOnClickListener {
            val emailAddress = signupEmailEditText.text.toString().trim()
            val password = signupPasswordEditText.text.toString().trim()
            val confirmPassword = signupConfirmPasswordEditText.text.toString().trim()
            viewModel.inputCheckForSignup(requireContext(), emailAddress, password, confirmPassword)
        }
    }

    private fun navigateHomeScreen() {
        val intentToNewsScreen = Intent(requireActivity(), NewsActivity::class.java)
        startActivity(intentToNewsScreen)
        requireActivity().finish()
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.emailErrorText) { event ->
            event.getContentIfNotHandled()?.let { emailError ->
                signupEmailEditText.error = getString(emailError)
            }
        }
        observe(viewModel.emailEndIconVisibility) { boolean ->
            signupEmailInputLayout.isEndIconVisible = boolean
        }
        observe(viewModel.passwordErrorText) { event ->
            event.getContentIfNotHandled()?.let { passwordError ->
                signupPasswordEditText.error = getString(passwordError)
            }
        }
        observe(viewModel.passwordEndIconVisibility) { boolean ->
            signupPasswordInputLayout.isEndIconVisible = boolean
        }
        observe(viewModel.confirmPasswordErrorText) { event ->
            event.getContentIfNotHandled()?.let { confirmPasswordError ->
                signupConfirmPasswordEditText.error = getString(confirmPasswordError)
            }
        }
        observe(viewModel.confirmPasswordEndIconVisibility) { boolean ->
            signupConfirmPasswordInputLayout.isEndIconVisible = boolean
        }
        observe(viewModel.errorMessage) { event ->
            event.getContentIfNotHandled()?.let { toastMessage ->
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.navigateToHome) { event ->
            event.getContentIfNotHandled()?.let {
                navigateHomeScreen()
            }
        }
    }
}