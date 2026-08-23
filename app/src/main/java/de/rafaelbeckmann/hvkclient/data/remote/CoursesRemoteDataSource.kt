package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkSingleCourseResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserCoursesResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserMarks
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.DataError
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.Result
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import javax.inject.Inject

class CoursesRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun getCourses(): Result<NetworkUserCoursesResponse, DataError.Remote> {
        return safeCall {
            httpClient.get(
                "sp/courses"
            ) {
            }
        }
    }

    suspend fun getCourse(courseId: Int): Result<NetworkSingleCourseResponse, DataError.Remote> {
        return safeCall {
            httpClient.get(
                "sp/courses/$courseId"
            ) {
            }
        }
    }

    suspend fun getMarks(courseId: Int): Result<NetworkUserMarks, DataError.Remote> {
        return safeCall {
            httpClient.get(
                "sp/courses/$courseId/marks"
            ) {
            }
        }
    }
}