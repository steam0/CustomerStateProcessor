package no.modio.demo.customerstateprocessorv1.kafka

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSingleElement
import no.modio.demo.customer.command.single.ChangeAddress
import no.modio.demo.customer.event.AddressAdded
import no.modio.demo.customer.event.AddressRemoved
import no.modio.demo.customer.event.AddressUpdated
import no.modio.demo.customer.state.Address
import no.modio.demo.customerstateprocessorv1.kafka.util.createCustomer
import no.modio.demo.customerstateprocessorv1.kafka.util.testTopology
import java.util.*

class ChangeAddressTest : FreeSpec({
    "change address and coordinates" - {
        val customerId = UUID.randomUUID().toString()

        testTopology(createCustomer(customerId).apply { address = emptyList() }) {
            "add address" {
                val command = ChangeAddress(
                    "FREG",
                    "Storgata 1",
                    "Addressline 2",
                    "1234",
                    "Oslo",
                    "Norge",
                ).toCommand()

                sendCommand(customerId, command)

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement AddressAdded::class
                states.last().state.address shouldHaveSingleElement Address(
                    "FREG",
                    "Storgata 1",
                    "Addressline 2",
                    "1234",
                    "Oslo",
                    "Norge",
                )
            }
            "update address" {
                val command = ChangeAddress(
                    "FREG",
                    "Storgata 2",
                    null,
                    "1234",
                    "Oslo",
                    "Norge",
                ).toCommand()

                sendCommand(customerId, command)

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement AddressUpdated::class
                states.last().state.address shouldHaveSingleElement Address(
                    "FREG",
                    "Storgata 2",
                    null,
                    "1234",
                    "Oslo",
                    "Norge",
                )
            }
            "remove address" {
                val command = ChangeAddress(
                    "FREG",
                    null,
                    null,
                    "",
                    "",
                    null,
                ).toCommand()

                sendCommand(customerId, command)

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement AddressRemoved::class
                states.last().state.address.shouldBeEmpty()
            }

            "remove address again" {
                val command = ChangeAddress(
                    "FREG",
                    null,
                    null,
                    "",
                    "",
                    null,
                ).toCommand()

                sendCommand(customerId, command)

                val states = customerStateTopic.readValuesToList()
                states.shouldBeEmpty()
            }
        }
    }
})