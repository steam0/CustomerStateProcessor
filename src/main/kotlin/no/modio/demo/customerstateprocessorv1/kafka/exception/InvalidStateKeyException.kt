package no.modio.demo.customerstateprocessorv1.kafka.exception

class InvalidStateKeyException(override val message: String = "State key on state object does not match command key") : RuntimeException(message)