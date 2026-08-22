package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpResponse
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSelectedCourseRequest
import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkVpSelectedCoursesResponse
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.DataError
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.Result
import de.rafaelbeckmann.hvkclient.data.remote.philliplacknertutorial.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class VpRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun getSubstitutions(courses: List<String>): Result<NetworkVpResponse, DataError.Remote> {
        return safeCall {
            httpClient.get(
                "vp/substitutions"
            ) {
                url {
                    parameters.appendAll("courses", courses)
                }
            }
        }
    }

    suspend fun getSelectedCourses(): Result<NetworkVpSelectedCoursesResponse, DataError.Remote> {
        return safeCall {
            httpClient.get(
                "vp/enrolled"
            ) {
                "vp/enrolled"
            }
        }
    }

    suspend fun postSelectedCourse(courseName: String): Result<Unit, DataError.Remote> {
        return safeCall {
            httpClient.post(
                "vp/enrolled"
            ) {
                setBody(
                    NetworkVpSelectedCourseRequest(courseName)
                )
            }
        }
    }

    suspend fun deleteSelectedCourse(courseId: String): Result<Unit, DataError.Remote> {
        return safeCall {
            httpClient.delete(
                "vp/enrolled/$courseId"
            ) {

            }
        }
    }

}