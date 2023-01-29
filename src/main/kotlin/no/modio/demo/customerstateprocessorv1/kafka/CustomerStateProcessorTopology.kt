package no.modio.demo.customerstateprocessorv1.kafka

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import no.modio.demo.customer.command.Command
import no.modio.demo.customer.result.CommandResult
import no.modio.demo.customer.result.Problem
import no.modio.demo.customer.result.Success
import no.modio.demo.customer.state.CustomerState
import no.modio.demo.customerstateprocessorv1.event.handler.CustomerEventHandler
import no.modio.demo.customerstateprocessorv1.kafka.transformer.CommandHandlerTransformer
import no.modio.demo.customerstateprocessorv1.kafka.transformer.richFilter
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.Stores
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


object CustomerStateProcessorTopology {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun buildTopology(
        customerEventHandler: CustomerEventHandler,
        commandTopic: String,
        stateTopic: String,
        serdes: TopologySerdes,
    ): Topology {
        val builder = StreamsBuilder()

        val persistentKeyValueStoreSupplier = Stores.persistentKeyValueStore("CustomerStates")

        val customerStateStoreBuilder = Stores.keyValueStoreBuilder(
            persistentKeyValueStoreSupplier,
            serdes.customerStateKey,
            serdes.customerStateValue
        )

        builder.addStateStore(customerStateStoreBuilder)

        // Command to Event
        val resultStream = builder
            .stream(commandTopic, Consumed.with(serdes.commandKey, serdes.commandValue))
            .peek { key, command -> log.debug("[Consuming][Command][Avro] ($key) $command") }
            .peek { key, command -> log.info("[Consuming][Command][Avro] ($key), commandType: '${command.command::class.java.simpleName}'") }
            .transform({ CommandHandlerTransformer("CustomerStates", customerEventHandler) }, "CustomerStates")

        resultStream.logExceptions()

        resultStream.sendCommandResultToReplyTopic(serdes)

        resultStream.sendStateEvents(stateTopic, serdes)

        return builder.build()
    }

    private fun KStream<String, Result<List<CustomerState?>>>.logExceptions() {
        peek { customerId, result ->
            if (result.isFailure) {
                log.warn("[Could not execute command] ($customerId) Command failed with exception: ${result.exceptionOrNull()}")
            }
        }
    }

    private fun KStream<String, Result<List<CustomerState?>>>.sendCommandResultToReplyTopic(serdes: TopologySerdes) =
        richFilter { _, _, context -> context.headers().lastHeader("reply_topic") != null }
            .mapValues { _, result ->
                CommandResult(
                    result.fold(
                        { states -> Success(states.lastOrNull()?.stateKey) },
                        { exception ->
                            Problem(
                                "COMMAND_FAILED",
                                "Command failed",
                                exception.message,
                                UUID.randomUUID().toString()
                            )
                        },
                    )
                )
            }
            .peek { key, result -> log.info("[Producing][Reply Topic][Avro] '$key','${result::class.java.simpleName}', '$result' ") }
            .to(
                { _, _, context -> String(context.headers().lastHeader("reply_topic").value()) },
                Produced.with(serdes.resultKey, serdes.resultValue)
            )

    private fun KStream<String, Result<List<CustomerState?>>>.sendStateEvents(
        stateTopic: String,
        serdes: TopologySerdes
    ) = flatMapValues { _, result -> result.getOrDefault(emptyList()) }
        .peek { key, aggregate -> log.debug("[Producing][Customer state][Avro] ($key) $aggregate") }
        .peek { key, aggregate ->
            if (aggregate != null) {
                log.info("[Producing][Customer state][Avro] ($key) ${aggregate.event.event::class.java.simpleName}")
            } else {
                log.info("[Producing][Customer state][Tombstone] ($key) ")
            }
        }
        .to(stateTopic, Produced.with(serdes.customerStateKey, serdes.customerStateValue))

}

class TopologySerdes(private val serdeConfig: Map<String, *>? = null) {
    private fun <T : SpecificRecord> specificSerde(isKey: Boolean = false) = SpecificAvroSerde<T>().apply {
        if (serdeConfig != null) {
            configure(serdeConfig, isKey)
        }
    }

    val commandKey: Serde<String> = Serdes.String()
    val commandValue = specificSerde<Command>()
    val customerStateKey: Serde<String> = Serdes.String()
    val customerStateValue = specificSerde<CustomerState>()
    val resultKey: Serde<String> = Serdes.String()
    val resultValue = specificSerde<CommandResult>()
}