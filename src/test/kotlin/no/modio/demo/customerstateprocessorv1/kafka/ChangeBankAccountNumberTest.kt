package no.modio.demo.customerstateprocessorv1.kafka

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.shouldBe
import no.modio.demo.customer.command.single.ChangeBankAccountNumber
import no.modio.demo.customer.event.BankAccountNumberAdded
import no.modio.demo.customer.event.BankAccountNumberRemoved
import no.modio.demo.customer.event.BankAccountNumberUpdated
import no.modio.demo.customerstateprocessorv1.kafka.util.createCustomer
import no.modio.demo.customerstateprocessorv1.kafka.util.testTopology
import java.util.*

class ChangeBankAccountNumberTest : FreeSpec({
    "change bank account number" - {
        val customerId = UUID.randomUUID().toString()

        testTopology(createCustomer(customerId).apply { bankAccountNumber = null }) {
            "remove bank account number that does not exist" {
                sendCommand(customerId, ChangeBankAccountNumber("").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.size shouldBe 0
            }
            "add bank account number" {
                sendCommand(customerId, ChangeBankAccountNumber("19171011111").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement BankAccountNumberAdded::class
                states.last().state.bankAccountNumber.number shouldBe "19171011111"
            }
            "update bank account number" {
                sendCommand(customerId, ChangeBankAccountNumber("19171022222").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement BankAccountNumberUpdated::class
                states.last().state.bankAccountNumber.number shouldBe "19171022222"
            }
            "remove bank account number" {
                sendCommand(customerId, ChangeBankAccountNumber("").toCommand())

                val states = customerStateTopic.readValuesToList()
                states.eventTypes shouldHaveSingleElement BankAccountNumberRemoved::class
                states.last().state.bankAccountNumber shouldBe null
            }
        }
    }
})