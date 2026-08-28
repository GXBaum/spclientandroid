package de.rafaelbeckmann.hvkclient

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import kotlin.io.encoding.Base64
import kotlin.io.readBytes
import kotlin.use

object UserPreferencesSerializer: Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        val encryptedBytesBase64 = withContext(Dispatchers.IO) {
            input.use {
                it.readBytes()
            }
        }

        val encryptedBytes = Base64.decode(encryptedBytesBase64)

        val bytes = Crypto.decrypt(encryptedBytes)
        val jsonString = bytes.decodeToString()

        return Json.Default.decodeFromString(jsonString)
    }

    override suspend fun writeTo(
        t: UserPreferences,
        output: OutputStream
    ) {
        val json = Json.encodeToString(t)
        val bytes = json.toByteArray()
        val encryptedBytes = Crypto.encrypt(bytes)
        val encryptedBytesBase64 = Base64.encodeToByteArray(encryptedBytes)

        withContext(Dispatchers.IO) {
            output.use {
                it.write(encryptedBytesBase64)
            }
        }
    }
}