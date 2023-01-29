package no.modio.demo.customerstateprocessorv1.kafka

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.shouldBe
import no.modio.demo.customer.command.single.ChangeEmail
import no.modio.demo.customer.event.EmailAdded
import no.modio.demo.customer.event.EmailRemoved
import no.modio.demo.customer.event.EmailUpdated
import no.modio.demo.customerstateprocessorv1.kafka.util.createCustomer
import no.modio.demo.customerstateprocessorv1.kafka.util.testTopology
import java.util.*

class ChangeEmailTest : FreeSpec({
    "change email" - {
        val customerId = UUID.randomUUID().toString()

        testTopology(createCustomer(customerId).apply { email = null }) {
            "remove email that does not exist" {
                val command = ChangeEmail("").toCommand()

                sendCommand(customerId, command)

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldBe emptyList()
            }

            "add email" {
                val command = ChangeEmail("test1@norsk-tipping.no").toCommand()

                sendCommand(customerId, command)

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement EmailAdded::class
                states.last().state.email.email shouldBe "test1@norsk-tipping.no"
            }

            "update email" {
                val command = ChangeEmail("test2@norsk-tipping.no").toCommand()

                sendCommand(customerId, command)

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement EmailUpdated::class
                states.last().state.email.email shouldBe "test2@norsk-tipping.no"
            }

            "remove email" {
                val command = ChangeEmail("").toCommand()

                sendCommand(customerId, command)

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement EmailRemoved::class
                states.last().state.email shouldBe null
            }
        }
    }
})