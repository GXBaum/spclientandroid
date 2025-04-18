package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.settings

import androidx.lifecycle.ViewModel
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.PrefUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class SettingsViewModel @Inject constructor(
    open val prefUtils: PrefUtils
): ViewModel() {

}