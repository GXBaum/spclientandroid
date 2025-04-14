package com.example.hvkclientmitbenachrichtigungen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.FcmApi
import com.example.UserCourse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class CoursesScreenViewModel : ViewModel() {
    // Use StateFlow to hold the list of courses
    private val _courses = MutableStateFlow<List<UserCourse>>(emptyList())
    val courses: StateFlow<List<UserCourse>> = _courses

    // Optional loading and error states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Initialize API internally
    private val api: FcmApi = Retrofit.Builder()
        .baseUrl("https://rafaelbeckmann.de/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(FcmApi::class.java)

    fun fetchCourses(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getUserCourses(username)
                if (response.isSuccessful) {
                    response.body()?.let { userCourses ->
                        // Store the complete course objects
                        _courses.value = userCourses.courses
                    }
                } else {
                    _error.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}