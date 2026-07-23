package de.rafaelbeckmann.hvkclient.ui.common

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val fullFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM., HH:mm")
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun relativeDateTimeFormatter(time: LocalDateTime): String {
    val now = LocalDateTime.now()
    val elapsedTime = Duration.between(time, now)

    return when {
        time.isAfter(now) -> {
            time.format(fullFormatter)
        }
        time.isAfter(now.minusSeconds(10)) -> {
            "vor wenigen Sekunden"
        }
        time.isAfter(now.minusMinutes(10)) -> {
            val minutes = elapsedTime.toMinutes();

            if (minutes == 1L) {
                "vor 1 Minute"
            } else {
                "vor $minutes Minuten"
            }
        }
        time.toLocalDate() == now.toLocalDate() -> {
            "Heute, ${time.format(timeFormatter)}"
        }
        time.toLocalDate() == now.toLocalDate().minusDays(1) -> {
            "Gestern, ${time.format(timeFormatter)}"
        }
        time.year == now.year -> {
            time.format(dateTimeFormatter)
        }
        else -> {
            time.format(fullFormatter)
        }
    }
}
















private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.")
private val fullDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

fun relativeDateFormatter(date: LocalDate): String {
    val now = LocalDate.now()
    return when {
        date == now -> {
            "Heute"
        }
        date == now.plusDays(1) -> {
            "Morgen"
        }
        date == now.plusDays(2) -> {
            "Übermorgen"
        }
        date == now.minusDays(1) -> {
            "Gestern"
        }
        date.year == now.year -> {
            date.format(dateFormatter)
        }
        else -> {
            date.format(fullDateFormatter)
        }
    }
}