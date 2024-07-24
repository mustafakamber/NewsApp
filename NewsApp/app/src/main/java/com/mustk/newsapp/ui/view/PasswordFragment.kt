package com.mustk.newsapp.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentPasswordBinding
import com.mustk.newsapp.ui.viewmodel.PasswordViewModel
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PasswordFragment @Inject constructor() : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    private lateinit var binding: FragmentPasswordBinding
    private val viewModel: PasswordViewModel by viewModels()

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
        setupPasswordScreen()
        observeLiveData()
    }

    private fun setupPasswordScreen() = with(binding) {
        resetEmailEditText.addTextChangedListener {
            viewModel.setEmailEndIcon(true)
        }
        resetSendButton.setOnClickListener {
            val emailAddress = resetEmailEditText.text.toString().trim()
            viewModel.inputCheckForResetPassword(emailAddress)
        }
        resetBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.errorMessage) { event ->
            event.getContentIfNotHandled()?.let { toastMessage ->
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
        observe(viewModel.emailErrorText) { event ->
            event.getContentIfNotHandled()?.let { emailError ->
                resetEmailEditText.error = getString(emailError)
            }
        }
        observe(viewModel.titleText) { titleMessage ->
            resetInfoText.text = getString(titleMessage)
        }
        observe(viewModel.emailEndIconVisibility) { boolean ->
            resetEmailInputLayout.isEndIconVisible = boolean
        }
        observe(viewModel.backButtonVisibility) { boolean ->
            resetBackButton.isVisible = boolean
        }
        observe(viewModel.sendButtonVisibility) { boolean ->
            resetSendButton.isVisible = boolean
        }
    }
}