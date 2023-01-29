package no.modio.demo.customerstateprocessorv1.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("streams")
data class KafkaStreamsConfig(
  val commandTopic: String,
  val stateTopic: String,
  val stateDir: String? = null,
  val bootstrapServers: String,
  val schemaRegistryUrl: String,
)