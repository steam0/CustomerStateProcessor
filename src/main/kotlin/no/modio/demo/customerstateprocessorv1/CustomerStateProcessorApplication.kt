package no.modio.demo.customerstateprocessorv1

import no.modio.demo.customerstateprocessorv1.kafka.config.KafkaStreamsConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
	KafkaStreamsConfig::class
)
class CustomerStateProcessorApplication

fun main(args: Array<String>) {
	runApplication<CustomerStateProcessorApplication>(*args)
}
