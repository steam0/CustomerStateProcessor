package no.modio.demo.customerstateprocessorv1.kafka

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.shouldBe
import no.modio.demo.customer.command.single.ChangePhoneNumber
import no.modio.demo.customer.event.PhoneNumberAdded
import no.modio.demo.customer.event.PhoneNumberRemoved
import no.modio.demo.customer.event.PhoneNumberUpdated
import no.modio.demo.customer.state.PhoneNumber
import no.modio.demo.customerstateprocessorv1.kafka.util.createCustomer
import no.modio.demo.customerstateprocessorv1.kafka.util.testTopology
import java.util.*

class ChangePhoneNumberTest : FreeSpec({
    "change phone number" - {
        val customerId = UUID.randomUUID().toString()

        testTopology(createCustomer(customerId).apply { phoneNumbers = emptyList() }) {
            "add phone number" {
                sendCommand(customerId, ChangePhoneNumber("Mobile", "47", "12345678").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement PhoneNumberAdded::class
                states.last().state.phoneNumbers shouldHaveSingleElement PhoneNumber("Mobile", "47", "12345678")
            }
            "update phone number" {
                sendCommand(customerId, ChangePhoneNumber("Mobile", "47", "11111111").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement PhoneNumberUpdated::class
                states.last().state.phoneNumbers shouldHaveSingleElement PhoneNumber("Mobile", "47", "11111111")
            }
            "remove phone number" {
                sendCommand(customerId, ChangePhoneNumber("Mobile", "", "").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement PhoneNumberRemoved::class
                states.last().state.phoneNumbers.shouldBeEmpty()
            }
            "remove phone number twice" {
                sendCommand(customerId, ChangePhoneNumber("Mobile", "", "").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldBe emptyList()
            }
        }
    }
})