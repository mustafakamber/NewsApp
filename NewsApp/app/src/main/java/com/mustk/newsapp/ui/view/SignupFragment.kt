package com.mustk.newsapp.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentSignupBinding
import com.mustk.newsapp.ui.viewmodel.SignupViewModel
import com.mustk.newsapp.util.observe
import com.mustk.newsapp.util.slideDown
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignupFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private val viewModel: SignupViewModel by viewModels()
    @Inject lateinit var navOptionsBuilder: NavOptions.Builder
    private lateinit var checkConnection: CheckConnectionFragment
    private var loading: LoadingFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeLiveData()
    }

    private fun setupUI() = with(binding) {
        loading = LoadingFragment(requireContext())
        signupTitleText.slideDown()
        signupEmailEditText.addTextChangedListener {
            setEmailEndIcon(true)
        }
        signupPasswordEditText.addTextChangedListener {
            setPasswordEndIcon(true)
        }
        signupConfirmPasswordEditText.addTextChangedListener {
            setConfirmPasswordEndIcon(true)
        }
        loginText.setOnClickListener {
            findNavController().popBackStack()
        }
        signupButton.setOnClickListener {
            val emailAddress = signupEmailEditText.text.toString().trim()
            val password = signupPasswordEditText.text.toString().trim()
            val confirmPassword = signupConfirmPasswordEditText.text.toString().trim()
            viewModel.validateInput(
                emailAddress,
                password,
                confirmPassword
            )
        }
    }

    private fun setEmailEndIcon(isVisible: Boolean) = with(binding) {
        signupEmailInputLayout.isEndIconVisible = isVisible
    }

    private fun setPasswordEndIcon(isVisible: Boolean) = with(binding) {
        signupPasswordInputLayout.isEndIconVisible = isVisible
    }

    private fun setConfirmPasswordEndIcon(isVisible: Boolean) = with(binding) {
        signupConfirmPasswordInputLayout.isEndIconVisible = isVisible
    }

    private fun navigateToCheckConnectionScreen() {
        checkConnection = CheckConnectionFragment(requireContext())
        checkConnection.showCheckConnectionDialog {
            if (viewModel.isConnectedNetwork()) {
                checkConnection.dismissDialog()
            }
        }
    }

    private fun navigateToHomeScreen() {
        val action = SignupFragmentDirections.actionSignupFragmentToHomeFragment()
        val navOptions = navOptionsBuilder
            .setPopUpTo(R.id.signupFragment, true)
            .build()
        findNavController().apply {
            graph.setStartDestination(R.id.homeFragment)
            navigate(action, navOptions)
        }
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.emailError) { error ->
            error.let {
                signupEmailEditText.error = getString(error)
                setEmailEndIcon(false)
            }
        }
        observe(viewModel.passwordError) { error ->
            error.let {
                signupPasswordEditText.error = getString(error)
                setPasswordEndIcon(false)
            }
        }
        observe(viewModel.confirmPasswordError) { error ->
            error.let {
                signupConfirmPasswordEditText.error = getString(error)
                setConfirmPasswordEndIcon(false)
            }
        }
        observe(viewModel.authError) { error ->
            error.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.loading) { boolean ->
            if (boolean) {
                loading?.showLoadingDialog()
            } else {
                loading?.dismissDialog()
            }
        }
        observe(viewModel.success) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToHomeScreen()
            }
        }
        observe(viewModel.checkNetworkConnection) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToCheckConnectionScreen()
            }
        }
    }
}