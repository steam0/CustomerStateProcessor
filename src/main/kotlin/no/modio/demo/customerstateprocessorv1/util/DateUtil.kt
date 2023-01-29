package no.modio.demo.customerstateprocessorv1.util

import no.modio.demo.customerstateprocessorv1.kafka.exception.InvalidTimeStringException
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Example: 1975-10-08T12:28:44.185Z
 */
fun String.epochToZuluDateTime(): String {
    return Instant
        .ofEpochMilli(this.toLong())
        .toString()
}

fun parseTimeStringToEpochMilli(timestamp: String): String {
    return try {
        ZonedDateTime.parse(timestamp).toInstant().toEpochMilli().toString()
    } catch (exception: DateTimeParseException) {
        LocalDateTime.parse(timestamp).atZone(ZoneId.of("Europe/Oslo")).toInstant().toEpochMilli().toString()
    }
}

fun String.validateTimeString(): String {
    if (this.isBlank()) {
        return this
    }

    return try {
        ZonedDateTime.parse(this, DateTimeFormatter.ISO_INSTANT)

        this
    } catch (exception: DateTimeParseException) {
        try {
            ZonedDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME)


            this
        } catch (exception: DateTimeParseException) {
            throw InvalidTimeStringException(this)
        }
    }
}