package de.rafaelbeckmann.hvkclient.data.remote

import de.rafaelbeckmann.hvkclient.data.remote.dto.NetworkUserCourse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PayloadDecoderImpl @Inject constructor(
    private val json: Json
) : PayloadDecoder {
    override fun <T> decodeJson(input: String, serializer: KSerializer<T>): T {
        return json.decodeFromString(serializer, input)
    }

    override fun decodeUserCourses(input: String): List<NetworkUserCourse> {
        return json.decodeFromString<List<NetworkUserCourse>>(input)
    }
}