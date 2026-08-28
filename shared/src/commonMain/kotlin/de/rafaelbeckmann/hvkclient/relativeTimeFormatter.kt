package de.rafaelbeckmann.hvkclient

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

//private val fullFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
private val fullFormatter = LocalDateTime.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    year()
    char(' ')
    hour()
    char(':')
    minute()
}
//private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM., HH:mm")
private val dateTimeFormatter = LocalDateTime.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    char(',')
    char(' ')
    hour()
    char(':')
    minute()
}
//private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val timeFormatter = LocalDateTime.Format {
    hour()
    char(':')
    minute()
}

fun relativeDateTimeFormatter(time: LocalDateTime): String {
    val nowInstant = Clock.System.now()
    val now = nowInstant.toLocalDateTime(TimeZone.currentSystemDefault())
    val timeInstant = time.toInstant(TimeZone.currentSystemDefault())
    val elapsedTime = nowInstant.minus(timeInstant)

    return when {
        time > now -> {
            time.format(fullFormatter)
        }
        elapsedTime < 10.seconds -> {
            "vor wenigen Sekunden"
        }
        elapsedTime < 10.minutes -> {
            val minutes = elapsedTime.inWholeMinutes;

            if (minutes == 1L) {
                "vor 1 Minute"
            } else {
                "vor $minutes Minuten"
            }
        }
        time.date == now.date -> {
            "Heute, ${time.format(timeFormatter)}"
        }
        time.date == now.date.minus(1, DateTimeUnit.DAY) -> {
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
















//private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.")
private val dateFormatter = LocalDate.Format {
    day()
    char('.')
    monthNumber()
    char('.')
}
//private val fullDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val fullDateFormatter = LocalDate.Format {
    day()
    char('.')
    monthNumber()
    char('.')
    year()
}

fun relativeDateFormatter(date: LocalDate): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when {
        date == now -> {
            "Heute"
        }
        date == now.plus(1, DateTimeUnit.DAY) -> {
            "Morgen"
        }
        date == now.plus(2, DateTimeUnit.DAY) -> {
            "Übermorgen"
        }
        date == now.minus(1, DateTimeUnit.DAY) -> {
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