package com.mustk.newsapp.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentPasswordBinding
import com.mustk.newsapp.ui.viewmodel.PasswordViewModel
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PasswordFragment @Inject constructor() : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    private lateinit var binding: FragmentPasswordBinding
    private val viewModel: PasswordViewModel by viewModels()
    private lateinit var checkConnection: CheckConnectionFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPasswordBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeLiveData()
    }

    private fun setupUI() = with(binding) {
        arguments?.apply {
            val args = PasswordFragmentArgs.fromBundle(this)
            resetEmailEditText.setText(args.password)
        }
        resetEmailEditText.addTextChangedListener {
            setEmailEndIcon(true)
        }
        resetSendButton.setOnClickListener {
            val email = resetEmailEditText.text.toString().trim()
            viewModel.validateInput(email)
        }
        resetBackButton.setOnClickListener {
            navigateToLoginScreen()
        }
    }

    private fun navigateToCheckConnectionScreen() {
        checkConnection = CheckConnectionFragment(requireContext())
        checkConnection.showCheckConnectionDialog {
            if (viewModel.isConnectedNetwork) {
                checkConnection.dismissDialog()
            }
        }
    }

    private fun navigateToLoginScreen() {
        findNavController().popBackStack()
    }

    private fun setResetInfoText(message: String?) = with(binding) {
        resetInfoText.text = message
    }

    private fun setSendButton(isVisible: Boolean) = with(binding) {
        resetSendButton.isVisible = isVisible
    }

    private fun setLoadingBar(isVisible: Boolean) = with(binding) {
        passwordLoadingBar.isVisible = isVisible
    }

    private fun setEmailEndIcon(isVisible: Boolean) = with(binding) {
        resetEmailInputLayout.isEndIconVisible = isVisible
    }

    private fun observeLiveData() {
        observe(viewModel.emailError) { error ->
            error.let {
                setEmailEndIcon(false)
                binding.resetEmailEditText.error = getString(error)
            }
        }
        observe(viewModel.authError) { error ->
            error.let {
                setSendButton(true)
                setResetInfoText(it)
            }
        }
        observe(viewModel.loading) { boolean ->
            if (boolean) {
                setSendButton(false)
                setLoadingBar(true)
            } else {
                setLoadingBar(false)
            }
        }
        observe(viewModel.success) { event ->
            event.getContentIfNotHandled()?.let { success ->
                if (success) {
                    setSendButton(false)
                    binding.resetBackButton.isVisible = true
                    lifecycleScope.launch {
                        delay(500)
                        navigateToLoginScreen()
                    }
                }
            }
        }
        observe(viewModel.checkConnection) { event ->
            event.getContentIfNotHandled()?.let { alert ->
                if (alert) {
                    navigateToCheckConnectionScreen()
                }
            }
        }
    }
}