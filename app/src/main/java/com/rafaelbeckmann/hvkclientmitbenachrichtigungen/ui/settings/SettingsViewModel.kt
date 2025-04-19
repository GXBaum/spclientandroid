package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.settings

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.PrefUtils
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.domaIn.repository.MyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class SettingsViewModel @Inject constructor(
    private val repository: MyRepository,
    open val prefUtils: PrefUtils
): ViewModel() {

    private val _vpSelectedCourse = MutableStateFlow<String>("")
    val vpSelectedCourse: StateFlow<String> = _vpSelectedCourse

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    var isDeveloper = mutableStateOf(false)
    var username = mutableStateOf("")

    init {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            isDeveloper.value = prefUtils.getString("isDeveloper").toBoolean()
            username.value = prefUtils.getString("username") ?: ""

            fetchSpSelectedCourse(username.value)


        }
    }

    fun saveUsername(username: String) {
        viewModelScope.launch {
            prefUtils.saveString("username", username)
        }
    }


    fun fetchSpSelectedCourse(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getVpSelectedCourses(username)
                .catch { exception ->
                    _error.value = exception.message
                }
                .collect { result ->
                    _isLoading.value = false
                    Log.d("SettingsViewModel", "username: $username")

                    result.fold(
                        onSuccess = { vpSelectedCourse ->
                            Log.d("SettingsViewModel", "vpSelectedCourse: $vpSelectedCourse")
                            _vpSelectedCourse.value = vpSelectedCourse.courseName
                        },
                        onFailure = { error ->
                            _error.value = error.message
                        }
                    )
                }

        }
    }




    fun postVpSelectedCourse(courseName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val courseObject = com.rafaelbeckmann.hvkclientmitbenachrichtigungen.data.model.VpSelectedCourse(courseName)

                repository.postVpSelectedCourses(username.value, courseObject)

                // After posting successfully, refresh the data
                fetchSpSelectedCourse(username.value)
            } catch (exception: Exception) {
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

}
