package com.mustk.newsapp.ui.news.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
    private lateinit var viewModel: SignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

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

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[SignupViewModel::class.java]
    }

    private fun setupSignupScreen() = with(binding) {
        signupTitleText.slideDown()
        signupEmailEditText.addTextChangedListener {
            viewModel.setVisibleEmailEndIcon()
        }
        signupPasswordEditText.addTextChangedListener {
            viewModel.setVisiblePasswordEndIcon()
        }
        signupConfirmPasswordEditText.addTextChangedListener {
            viewModel.setVisibleConfirmPasswordEndIcon()
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
        observe(viewModel.emailErrorText) { emailError ->
            signupEmailEditText.error = getString(emailError)
        }
        observe(viewModel.emailEndIconVisible) { boolean ->
            signupEmailInputLayout.isEndIconVisible = boolean
        }
        observe(viewModel.passwordErrorText) { passwordError ->
            signupPasswordEditText.error = getString(passwordError)
        }
        observe(viewModel.passwordEndIconVisible) { boolean ->
            signupPasswordInputLayout.isEndIconVisible = boolean
        }
        observe(viewModel.confirmPasswordErrorText) { confirmPasswordError ->
            signupConfirmPasswordEditText.error = getString(confirmPasswordError)
        }
        observe(viewModel.confirmPasswordEndIconVisible) { boolean ->
            signupConfirmPasswordInputLayout.isEndIconVisible = boolean
        }
        observe(viewModel.networkErrorMessage) { event ->
            event.getContentIfNotHandled()?.let { toastMessage ->
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.navigateToHome) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) navigateHomeScreen()
            }
        }
    }
}