package com.mustk.newsapp.ui.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.mustk.newsapp.R
import com.mustk.newsapp.databinding.FragmentChangeProfileBinding
import com.mustk.newsapp.ui.viewmodel.ChangeProfileViewModel
import com.mustk.newsapp.ui.viewmodel.PhotoViewModel
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangeProfileFragment @Inject constructor() : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    private lateinit var binding: FragmentChangeProfileBinding
    private val viewModel: ChangeProfileViewModel by viewModels()
    private val sharedViewModel : PhotoViewModel by activityViewModels()
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangeProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerLauncher()
        setupUI()
        observeLiveData()
    }

    private fun registerLauncher() {
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { galleryResult ->
            fetchPhotoFromGallery(galleryResult)
        }
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permissionResult ->
            handleGalleryPermissionResult(permissionResult)
        }
    }

    private fun handleGalleryPermissionResult(result: Boolean) {
        if (result) {
            navigateToGalleryScreen()
        } else {
            showToastMessage(getString(R.string.read_external_storage_permission))
        }
    }

    private fun showToastMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || (checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            navigateToGalleryScreen()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showSnackBarMessage()
            } else {
                viewModel.setToastMessage(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun navigateToGalleryScreen() {
        galleryLauncher.launch(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
        )
    }

    private fun setupUI() = with(binding) {
        deleteProfilePhotoButton.setOnClickListener {
            viewModel.deleteProfilePhoto()
        }
        uploadProfilePhotoButton.setOnClickListener {
            checkGalleryPermission()
        }
    }

    private fun showSnackBarMessage() {
        Snackbar.make(
            binding.root,
            getString(R.string.read_external_storage_permission),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(
            getString(R.string.allow)
        ) {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }.show()
    }

    private fun fetchPhotoFromGallery(galleryResult: ActivityResult) {
        if (galleryResult.resultCode == AppCompatActivity.RESULT_OK) {
            val intentFromGalleryResult = galleryResult.data
            if (intentFromGalleryResult != null) {
                val newProfilePhotoUri = intentFromGalleryResult.data
                viewModel.saveNewProfilePhoto(newProfilePhotoUri)
            }
        }
    }

    private fun backToSettingsScreen() {
        findNavController().popBackStack()
    }

    private fun observeLiveData() {
        observe(viewModel.backToSettings) { event ->
            event.getContentIfNotHandled()?.let {
                if (it) {
                    backToSettingsScreen()
                }
            }
        }
        observe(viewModel.userPhoto) { photoUrl ->
            sharedViewModel.setChangedUserPhoto(photoUrl)
        }
        observe(viewModel.actionSnackBarMessage) { event ->
            event.getContentIfNotHandled()?.let { actionSnackBar ->
                val snackBarText = getString(actionSnackBar.snackBarText)
                val allowButtonText = getString(actionSnackBar.allowButtonText)
                val permissionMessage = actionSnackBar.permissionMessage
                Snackbar.make(binding.root, snackBarText, Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                        allowButtonText
                    ) {
                        permissionLauncher.launch(permissionMessage)
                    }.show()
            }
        }
        observe(viewModel.navigateToGallery) { event ->
            event.getContentIfNotHandled()?.let {
                if (it) {
                    navigateToGalleryScreen()
                }
            }
        }
    }
}