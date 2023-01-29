package no.modio.demo.customerstateprocessorv1.util

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import no.modio.demo.customerstateprocessorv1.kafka.exception.InvalidTimeStringException

class DateUtilTest : FreeSpec({

    "Parse time string with and without zone info" {
        val timestampWithoutZone = "1994-04-14T11:51:25"
        val timestampAtZulu = "1994-04-14T09:51:25Z"
        val timestampWithOffset = "1994-04-14T10:51:25+01:00"

        val parsedWithoutZone = parseTimeStringToEpochMilli(timestampWithoutZone)
        val parsedAtZulu = parseTimeStringToEpochMilli(timestampAtZulu)
        val parsedWithOffset = parseTimeStringToEpochMilli(timestampWithOffset)

        parsedAtZulu shouldBe parsedWithoutZone
        parsedAtZulu shouldBe parsedWithOffset
    }

    "validate epochmillis to zulu date time" - {
            val input = "258940800000"

            shouldNotThrow<InvalidTimeStringException> {
                val timestring = input.epochToZuluDateTime()

                 timestring shouldBe "1978-03-17T00:00:00Z"
            }
    }

    "validate epochmillis to zulu date time and validation" - {
        val input = "258940800000"

        shouldNotThrow<InvalidTimeStringException> {
            val timestring = input.epochToZuluDateTime().validateTimeString()

            timestring shouldBe "1978-03-17T00:00:00Z"
        }
    }

    "validate epochmillis to zulu date time with millis" - {
        val input = "258940802105"

        shouldNotThrow<InvalidTimeStringException> {
            val timestring = input.epochToZuluDateTime()

            timestring shouldBe "1978-03-17T00:00:02.105Z"
        }
    }

    "validate epochmillis to zulu date time with millis and validation" - {
        val input = "258940802105"

        shouldNotThrow<InvalidTimeStringException> {
            val timestring = input.epochToZuluDateTime().validateTimeString()

            timestring shouldBe "1978-03-17T00:00:02.105Z"
        }
    }

    "validate time string '1975-10-08T12:28:44.185Z' should return the same string" - {
            val input = "1975-10-08T12:28:44.185Z"

            shouldNotThrow<InvalidTimeStringException> {
                val timestring = input.validateTimeString()

                timestring shouldBe input
            }
    }

    "validate time string '2011-12-03T10:15:30+01:00' should return the same string" - {
        val input = "2011-12-03T10:15:30+01:00"

        shouldNotThrow<InvalidTimeStringException> {
            val timestring = input.validateTimeString()

            timestring shouldBe input
        }
    }

    "validate empty time string '' should return the same string" - {
        val input = ""

        shouldNotThrow<InvalidTimeStringException> {
            val timestring = input.validateTimeString()

            timestring shouldBe input
        }
    }

    "validate time string '2011-12-03T10:15:30+01:00[Europe/Paris]' should throw exception" - {
        val input = "2011-12-03T10:15:30+01:00[Europe/Paris]"

        shouldThrow<InvalidTimeStringException> {
            input.validateTimeString()
        }
    }
})
