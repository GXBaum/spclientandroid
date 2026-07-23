package de.rafaelbeckmann.hvkclient.data.repository

import android.util.Log
import de.rafaelbeckmann.hvkclient.di.AppModule
import de.rafaelbeckmann.hvkclient.domain.repository.EncryptedUserPreferencesRepository
import de.rafaelbeckmann.hvkclient.domain.repository.SpRepositoryTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class SpRepositoryTestImpl @Inject constructor(
    @param:AppModule.UsingThisToNotHaveTheProvidesAnnotationDuplicationError
    private val spAuthOkHttp: OkHttpClient,
    private val encryptedUserPreferencesRepository: EncryptedUserPreferencesRepository
) : SpRepositoryTest {

    override suspend fun getSpAuthCookiesTest(): List<Cookie> = withContext(Dispatchers.IO) {

        val schoolId = 6078

        var returnVal: List<Cookie>

        val userPreferences = encryptedUserPreferencesRepository.getUserPreferences().first()

        val username = userPreferences.spUsername
        val password = userPreferences.spPassword

        if (username.isNullOrEmpty() || password.isNullOrEmpty()) return@withContext emptyList() // TODO lol

        Log.d("TEST", "$schoolId.$username")

        val formBody = FormBody.Builder()
            .add("user2", username)
            .add("user", "$schoolId.$username")
            .add("password", password)
            .add("stayconnected", "1")
            .build()

        val request = Request.Builder()
            .url("https://login.schulportal.hessen.de/?i=$schoolId")
            //.header USER AGENT
            .post(formBody)
            .build()

        spAuthOkHttp.newCall(request).execute()/*.use {
            returnVal = if (it.isSuccessful) {
                it.body.string()

            } else {
                emptyList()
            }
        }*/

        val cookies = spAuthOkHttp.cookieJar.toString()


        Log.d("TEST", cookies)
        Log.d("TEST", spAuthOkHttp.cookieJar.loadForRequest("https://login.schulportal.hessen.de/?i=$schoolId".toHttpUrl()).toString())
        returnVal = spAuthOkHttp.cookieJar.loadForRequest("https://login.schulportal.hessen.de/?i=$schoolId".toHttpUrl())
        //returnVal = cookies

        Log.d("TEST", spAuthOkHttp.cookieJar.loadForRequest("https://schulportal.hessen.de/".toHttpUrl()).toString())


        if (!cookies.contains("sid=")) {
            // todo throw error
            Log.d("TEST", "Authentication failed. Invalid credentials? (Missing 'sid')")
        }

        return@withContext returnVal
    }
}