package de.rafaelbeckmann.hvkclient.core.presentation

import de.rafaelbeckmann.hvkclient.core.domain.DataError

fun DataError.toUiText(fallback: String? = null): String {
    return when (this) {
        DataError.Remote.NO_INTERNET -> "kein Internet"
        DataError.Remote.REQUEST_TIMEOUT -> "Timeout"
        else -> fallback ?: "Fehler beim Laden"
    }
}