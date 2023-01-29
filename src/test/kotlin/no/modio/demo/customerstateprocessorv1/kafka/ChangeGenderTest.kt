package no.modio.demo.customerstateprocessorv1.kafka

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.shouldBe
import no.modio.demo.customer.command.single.ChangeGender
import no.modio.demo.customer.event.GenderUpdated
import no.modio.demo.customerstateprocessorv1.kafka.util.createCustomer
import no.modio.demo.customerstateprocessorv1.kafka.util.testTopology
import java.util.*

class ChangeGenderTest : FreeSpec({
    "change gender" - {
        val customerId = UUID.randomUUID().toString()

        testTopology(createCustomer(customerId)) {
            "update gender" {
                sendCommand(customerId, ChangeGender("Female").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement GenderUpdated::class
                states.last().state.gender shouldBe "Female"
            }
            "should not result in events when gender is unchanged" {
                sendCommand(customerId, ChangeGender("Female").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes.shouldBeEmpty()
            }
        }
    }
})