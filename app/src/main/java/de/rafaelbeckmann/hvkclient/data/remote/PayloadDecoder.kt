package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserCourse
import kotlinx.serialization.KSerializer

interface PayloadDecoder {
    fun <T> decodeJson(input: String, serializer: KSerializer<T>): T

    fun decodeUserCourses(input: String): List<NetworkUserCourse>
}