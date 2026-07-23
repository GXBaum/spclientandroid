package de.rafaelbeckmann.hvkclient.domain.repository

import okhttp3.Cookie

interface SpRepositoryTest {
    suspend fun getSpAuthCookiesTest(): List<Cookie>
}