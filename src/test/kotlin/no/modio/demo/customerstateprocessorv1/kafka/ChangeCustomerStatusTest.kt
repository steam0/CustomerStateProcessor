package no.modio.demo.customerstateprocessorv1.kafka

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.*
import io.kotest.matchers.shouldBe
import no.modio.demo.customer.command.single.ChangeCustomerStatus
import no.modio.demo.customer.event.CustomerStatusUpdated
import no.modio.demo.customerstateprocessorv1.kafka.util.createCustomer
import no.modio.demo.customerstateprocessorv1.kafka.util.testTopology
import java.util.*

class ChangeCustomerStatusTest : FreeSpec({
    "change customer status" - {
        val customerId = UUID.randomUUID().toString()

        testTopology(createCustomer(customerId)) {
            "update customer status" {
                sendCommand(customerId, ChangeCustomerStatus("Blacklisted").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldContainInOrder listOf(CustomerStatusUpdated::class)
                states.first().state.status shouldBe "Blacklisted"
            }
            "should not result in events when status is unchanged 'Blacklisted'" {
                sendCommand(customerId, ChangeCustomerStatus("Blacklisted").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes.shouldBeEmpty()
            }
            "update customer status from Blacklisted to Active" {
                sendCommand(customerId, ChangeCustomerStatus("Active").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSize 1
                states.eventTypes shouldContainInOrder listOf(CustomerStatusUpdated::class)
                states.first().state.status shouldBe "Active"
            }
            "should not result in events when status is unchanged 'Active'" {
                sendCommand(customerId, ChangeCustomerStatus("Active").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes.shouldBeEmpty()
            }
            "update customer status from Active to Awaiting termination" {
                sendCommand(customerId, ChangeCustomerStatus("Awaiting termination").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSize 1
                states.eventTypes shouldContainInOrder listOf(CustomerStatusUpdated::class)
                states.first().state.status shouldBe "Awaiting termination"
            }
            "update customer status from Awaiting termination to Terminated" {
                sendCommand(customerId, ChangeCustomerStatus("Terminated").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSize 1
                states.eventTypes shouldHaveSingleElement CustomerStatusUpdated::class
                states.first().state.status shouldBe "Terminated"
            }
            "should not result in events when an invalid customer state is sent" {
                sendCommand(customerId, ChangeCustomerStatus("Invalid-Customer-State").toCommand())

                val states = customerStateTopic.readValuesToList()
                states shouldHaveSize 0
            }
        }
    }
})