package com.mustk.newsapp.ui.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mustk.newsapp.R
import com.mustk.newsapp.broadcastreceiver.NotificationReceiver
import com.mustk.newsapp.databinding.FragmentSettingsBinding
import com.mustk.newsapp.shared.Constant.ABOUT_ME_URL
import com.mustk.newsapp.shared.Constant.APP_URL
import com.mustk.newsapp.shared.Constant.LANGUAGE_EN
import com.mustk.newsapp.shared.Constant.LANGUAGE_TR
import com.mustk.newsapp.shared.Constant.PENDING_INTENT_REQUEST_CODE
import com.mustk.newsapp.shared.Constant.SEND_MESSAGE_EMAIL
import com.mustk.newsapp.shared.Constant.SHARE_SCREEN_TITLE
import com.mustk.newsapp.shared.Constant.SHARE_SCREEN_TYPE
import com.mustk.newsapp.ui.viewmodel.SettingsViewModel
import com.mustk.newsapp.util.downloadProfileImageFromURL
import com.mustk.newsapp.util.observe
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment @Inject constructor() : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var confirmation: ConfirmationFragment
    @Inject lateinit var navOptionsBuilder: NavOptions.Builder


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettingsScreen()
        observeLiveData()
    }

    private fun setupSettingsScreen() = with(binding) {

        setupSpinner()

        viewModel.darkThemeEnabled.value?.let {
            themeSwitch.isChecked = it
        }

        viewModel.notificationEnabled.value?.let {
            notificationSwitch.isChecked = it
        }

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            changeNotificationSwitchState(isChecked)
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            changeThemeSwitchState(isChecked)
        }
        changePasswordButton.setOnClickListener {
            navigateToPasswordScreen()
        }
        deleteAccountButton.setOnClickListener {
            confirmation = ConfirmationFragment(requireContext(), R.string.delete_account_alert)
            confirmation.showConfirmationDialog {
                viewModel.deleteAccountButtonClicked()
            }
        }
        logOutButton.setOnClickListener {
            confirmation = ConfirmationFragment(requireContext(), R.string.logout_alert)
            confirmation.showConfirmationDialog {
                viewModel.logOutButtonClicked()
            }
        }
        sendMessageButton.setOnClickListener {
            navigateMailScreen()
        }
        aboutMeButton.setOnClickListener {
            navigateToSeeMoreScreen()
        }
        shareAppButton.setOnClickListener {
            navigateToShareScreen()
        }
    }

    private fun setupSpinner() = with(binding) {
        val languageItems = listOf(
            R.string.spinner_english, R.string.spinner_turkish
        )
        val languageOptions = listOf(
            LANGUAGE_EN, LANGUAGE_TR
        )
        val languageTitles = languageItems.map { getString(it) }
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languageTitles)
        languageSelectSpinner.adapter = adapter
        languageSelectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val languageCode = languageOptions[position]
                viewModel.languageItem.value?.let { currentLanguage ->
                    if (languageCode != currentLanguage){
                        viewModel.saveLanguagePreference(languageCode)
                        changeUILanguage(languageCode)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun changeNotificationSwitchState(isChecked: Boolean) {
        viewModel.saveNotificationPreference(isChecked)
        if (isChecked) {
            NotificationReceiver.scheduleNextAlarm(requireContext())
        } else {
            cancelNotifications()
        }
    }

    private fun changeUILanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        requireContext().resources.updateConfiguration(
            config,
            requireContext().resources.displayMetrics
        )
        activity?.recreate()
    }

    private fun changeThemeSwitchState(isChecked: Boolean) {
        viewModel.saveDarkThemePreference(isChecked)
        changeUITheme()
    }

    private fun changeUITheme() {
        viewModel.darkThemeEnabled.value?.let { isEnabled ->
            if (isEnabled) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun cancelNotifications() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            PENDING_INTENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun navigateMailScreen() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:$SEND_MESSAGE_EMAIL")
        startActivity(intent)
    }

    private fun navigateToShareScreen() {
        val intentToShareScreen = Intent(Intent.ACTION_SEND)
        intentToShareScreen.apply {
            type = SHARE_SCREEN_TYPE
            putExtra(Intent.EXTRA_TEXT, APP_URL)
            startActivity(Intent.createChooser(this, SHARE_SCREEN_TITLE))
        }
    }

    private fun navigateToPasswordScreen() {
        viewModel.userEmail.value?.let {
            val action = SettingsFragmentDirections.actionSettingsFragmentToPasswordFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun navigateToSeeMoreScreen() {
        val action =
            SettingsFragmentDirections.actionSettingsFragmentToSeeMoreFragment(ABOUT_ME_URL)
        findNavController().navigate(action)
    }

    private fun navigateToLoginScreen() {
        val action = SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
        val navOptions = navOptionsBuilder
            .setPopUpTo(R.id.homeFragment, true)
            .build()
        findNavController().apply {
            graph.setStartDestination(R.id.loginFragment)
            navigate(action, navOptions)
        }
    }

    private fun observeLiveData() = with(binding) {
        observe(viewModel.userPhoto) { photoUrl ->
            settingsProfileImageView.downloadProfileImageFromURL(photoUrl)
        }
        observe(viewModel.userEmail) { emailAddress ->
            settingsProfileEmailTextView.text = emailAddress
        }
        observe(viewModel.navigateToLogin) { event ->
            event.getContentIfNotHandled()?.let {
                navigateToLoginScreen()
            }
        }
    }
}