package de.rafaelbeckmann.hvkclient

import java.time.LocalDateTime

data class AlarmItem (
    val time: LocalDateTime,
    val message: String
)