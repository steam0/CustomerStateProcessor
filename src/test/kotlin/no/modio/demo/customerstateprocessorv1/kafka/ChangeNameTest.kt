package no.modio.demo.customerstateprocessorv1.kafka

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.shouldBe
import no.modio.demo.customer.command.single.ChangeName
import no.modio.demo.customer.event.NameUpdated
import no.modio.demo.customer.state.Name
import no.modio.demo.customerstateprocessorv1.kafka.util.createCustomer
import no.modio.demo.customerstateprocessorv1.kafka.util.testTopology
import java.util.*

class ChangeNameTest : FreeSpec({
    "change name" - {
        val customerId = UUID.randomUUID().toString()

        testTopology(createCustomer(customerId)) {
            "update name" {
                sendCommand(customerId, ChangeName("Reidar", "Larsen", "Fos", "Reidar Larsen").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement NameUpdated::class
                states.last().state.name shouldBe Name(
                    "Reidar",
                    "Larsen",
                    "Fos",
                    "Reidar Larsen",
                )
            }
            "should not result in events when name is unchanged" {
                sendCommand(customerId, ChangeName("Reidar", "Larsen", "Fos", "Reidar Larsen").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes.shouldBeEmpty()
            }
        }
    }
})