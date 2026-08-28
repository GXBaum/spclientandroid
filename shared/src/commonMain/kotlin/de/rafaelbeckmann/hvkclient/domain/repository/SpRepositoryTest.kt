package de.rafaelbeckmann.hvkclient.domain.repository

import de.rafaelbeckmann.hvkclient.features.other.data.NetworkCookie

interface SpRepositoryTest {
    suspend fun getSpAuthCookiesTest(): List<NetworkCookie>
}