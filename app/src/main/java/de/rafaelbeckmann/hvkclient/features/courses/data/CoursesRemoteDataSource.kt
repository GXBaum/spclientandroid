package de.rafaelbeckmann.hvkclient.features.courses.data

import de.rafaelbeckmann.hvkclient.core.data.safeCall
import de.rafaelbeckmann.hvkclient.core.domain.DataError
import de.rafaelbeckmann.hvkclient.core.domain.Result
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