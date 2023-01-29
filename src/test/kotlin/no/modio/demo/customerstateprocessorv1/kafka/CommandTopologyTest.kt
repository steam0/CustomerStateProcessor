package no.modio.demo.customerstateprocessorv1.kafka

import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import no.modio.demo.customer.command.Command
import no.modio.demo.customer.command.single.ChangeEmail
import no.modio.demo.customer.event.EmailAdded
import no.modio.demo.customer.event.EmailUpdated
import no.modio.demo.customer.result.Problem
import no.modio.demo.customer.result.Success
import no.modio.demo.customerstateprocessorv1.kafka.util.createCustomer
import no.modio.demo.customerstateprocessorv1.kafka.util.createMetadata
import no.modio.demo.customerstateprocessorv1.kafka.util.schemaRegistryScope
import no.modio.demo.customerstateprocessorv1.kafka.util.testTopology
import java.util.*

class CommandTopologyTest : FreeSpec({

    afterSpec {
        MockSchemaRegistry.dropScope(schemaRegistryScope)
    }

    "updates with and without state key" - {
        val customerId = UUID.randomUUID().toString()

        testTopology(createCustomer(customerId).apply { email = null }) {
            var previousStateKey: String? = null

            "add element without state key" {
                val command = Command(
                    ChangeEmail("test1@norsk-tipping.no"),
                    createMetadata(),
                    null
                )

                sendCommand(customerId, command)

                val states = customerStateTopic.readValuesToList()
                val events = states.map { it.event.event::class }
                events shouldContainExactly listOf(
                    EmailAdded::class
                )

                states.last().state.email.email shouldBe "test1@norsk-tipping.no"
                previousStateKey = states.last().stateKey
            }

            "update element with valid state key" - {
                val correlationId = UUID.randomUUID()
                val command = Command(
                    ChangeEmail("test2@norsk-tipping.no"),
                    createMetadata(),
                    previousStateKey
                )

                sendCommand(customerId, command, correlationId)

                val states = customerStateTopic.readValuesToList()
                val events = states.map { it.event.event::class }
                events shouldContainExactly listOf(
                    EmailUpdated::class
                )

                states.last().state.email.email shouldBe "test2@norsk-tipping.no"

                "expect success result on reply topic" {
                    val result = resultTopic.readRecordsToList().last()
                    result.correlationId shouldBe correlationId
                    val reply = result.value.result.shouldBeInstanceOf<Success>()
                    reply.stateKey.shouldNotBeNull()
                }
            }

            "update element with invalid state key" - {
                val correlationId = UUID.randomUUID()
                val command = Command(
                    ChangeEmail("test3@norsk-tipping.no"),
                    createMetadata(),
                    UUID.randomUUID().toString()
                )

                sendCommand(customerId, command, correlationId)

                val states = customerStateTopic.readValuesToList()
                states.size shouldBe 0

                "expect problem result on reply topic" {
                    val result = resultTopic.readRecordsToList().last()
                    result.correlationId shouldBe correlationId
                    val problem = result.value.result.shouldBeInstanceOf<Problem>()
                    problem.detail shouldBe "State key on state object does not match command key"
                }
            }
        }
    }

})
