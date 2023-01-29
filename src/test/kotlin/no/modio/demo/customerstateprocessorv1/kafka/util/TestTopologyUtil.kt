package no.modio.demo.customerstateprocessorv1.kafka.util

import io.confluent.common.utils.TestUtils
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import no.modio.demo.customer.command.Command
import no.modio.demo.customer.command.single.ImportCustomer
import no.modio.demo.customer.state.Customer
import no.modio.demo.customer.state.CustomerState
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customerstateprocessorv1.event.handler.CustomerEventHandler
import no.modio.demo.customerstateprocessorv1.kafka.CommandTopologyTest
import no.modio.demo.customerstateprocessorv1.kafka.CustomerStateProcessorTopology
import no.modio.demo.customerstateprocessorv1.kafka.TopologySerdes
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.TestRecord
import java.util.*
import kotlin.reflect.KClass

val schemaRegistryScope = CommandTopologyTest::class.simpleName
val schemaRegistryUrl = "mock://$schemaRegistryScope"

val config = mapOf(
    StreamsConfig.APPLICATION_ID_CONFIG to "CustomerStateProcessorv1",
    StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "whocares:9092",
    StreamsConfig.STATE_DIR_CONFIG to TestUtils.tempDirectory().absolutePath,
    AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS to true.toString(),
    AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to schemaRegistryUrl,
)

val serdes = TopologySerdes(config)

const val commandTopicName = "nt.customer.internal.command"
const val resultTopicName = "nt.customer.internal.command.result"
const val customerStateTopicName = "nt.customer.internal.customerstate"

class TopologyTestScope(testDriver: TopologyTestDriver) {
    val commandTopic = testDriver.createInputTopic(
        commandTopicName,
        serdes.commandKey.serializer(),
        serdes.commandValue.serializer(),
    )
    val customerStateTopic = testDriver.createOutputTopic(
        customerStateTopicName,
        serdes.customerStateKey.deserializer(),
        serdes.customerStateValue.deserializer(),
    )
    val resultTopic = testDriver.createOutputTopic(
        resultTopicName,
        serdes.resultKey.deserializer(),
        serdes.resultValue.deserializer(),
    )

    fun sendCommand(customerId: String, command: Command, correlationId: UUID? = null) {
        val headers = if (correlationId != null) {
            RecordHeaders(
                listOf(
                    RecordHeader("correlation_id", correlationId.toString().toByteArray()),
                    RecordHeader("reply_topic", resultTopicName.toByteArray()),
                )
            )
        } else {
            RecordHeaders()
        }
        commandTopic.pipeInput(TestRecord(customerId, command, headers))
    }

    val TestRecord<*, *>.correlationId: UUID?
        get() = headers.lastHeader("correlation_id")?.let { UUID.fromString(String(it.value())) }

    val List<CustomerState>.eventTypes: List<KClass<*>>
        get() = map { it.event.event::class }

    fun Any.toCommand(
        metadata: Metadata = createMetadata(),
        stateKey: String? = null,
    ): Command = Command(
        this,
        metadata,
        stateKey,
    )
}

suspend fun testTopology(aggregate: Customer? = null, block: suspend TopologyTestScope.() -> Unit) {
    val topology = CustomerStateProcessorTopology.buildTopology(
        customerEventHandler = CustomerEventHandler,
        commandTopic = commandTopicName,
        stateTopic = customerStateTopicName,
        serdes = serdes
    )

    TopologyTestDriver(topology, config.toProperties()).use { testDriver ->
        TopologyTestScope(testDriver).apply {
            if (aggregate != null) {
                sendCommand(aggregate.customerId, Command(ImportCustomer(aggregate), createMetadata(), null))
                customerStateTopic.readValuesToList() // Dump all states so that we will not interfere in our tests
                resultTopic.readValuesToList()
            }

            block()
        }
    }
}