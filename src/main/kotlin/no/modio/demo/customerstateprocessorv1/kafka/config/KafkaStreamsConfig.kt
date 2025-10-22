package no.modio.demo.customerstateprocessorv1.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("streams")
data class KafkaStreamsConfig(
  val commandTopic: String,
  val stateTopic: String,
  val stateDir: String? = null,
  val bootstrapServers: String,
  val schemaRegistryUrl: String,
)
