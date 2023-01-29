package no.modio.demo.customerstateprocessorv1.kafka

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import no.modio.demo.customerstateprocessorv1.event.handler.CustomerEventHandler
import no.modio.demo.customerstateprocessorv1.kafka.config.KafkaStreamsConfig
import no.modio.demo.customerstateprocessorv1.util.Constants.APPLICATION_NAME
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import javax.annotation.PreDestroy

@Component
class CustomerStateProcessorStream(
    private val applicationContext: ApplicationContext,
    customerEventHandler: CustomerEventHandler,
    kafkaStreamsConfig: KafkaStreamsConfig
) {
    private val log: Logger = LoggerFactory.getLogger(CustomerStateProcessorStream::class.java)

    private val kafkaStream: KafkaStreams

    init {
        val properties = Properties()
        val applicationId = APPLICATION_NAME
        properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true)
        properties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaStreamsConfig.schemaRegistryUrl)
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, applicationId)
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaStreamsConfig.bootstrapServers)
        properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1)
        properties.put(StreamsConfig.MAX_WARMUP_REPLICAS_CONFIG, 1)
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000)
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0)
        kafkaStreamsConfig.stateDir?.let {
            properties.setProperty(StreamsConfig.STATE_DIR_CONFIG, it)
        }

        val specificSerdes = TopologySerdes(
            mapOf(
                AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS to true.toString(),
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to kafkaStreamsConfig.schemaRegistryUrl,
            )
        )

        val topology = CustomerStateProcessorTopology.buildTopology(
            customerEventHandler,
            kafkaStreamsConfig.commandTopic,
            kafkaStreamsConfig.stateTopic,
            specificSerdes,
        )

        log.debug(topology.describe().toString())

        kafkaStream = KafkaStreams(topology, properties)
        kafkaStream.setUncaughtExceptionHandler { _, exception ->
            log.debug("Some exception caught: $exception")
            SpringApplication.exit(applicationContext, ExitCodeGenerator { -1 })
        }

        kafkaStream.start()
    }

    /**
     * It seems like it is impossible for us to shutdown a
     * kafka streams application by adding a shutdown hook
     * (like we have done for producers/consumers.
     *
     * By using @PreDestroy we shut down the kafka streams
     * application before shotdown hooks are triggered.
     */
    @PreDestroy
    fun close() {
        if (kafkaStream.state().isRunningOrRebalancing) {
            kafkaStream.close(Duration.ofSeconds(60))
        }
    }
}