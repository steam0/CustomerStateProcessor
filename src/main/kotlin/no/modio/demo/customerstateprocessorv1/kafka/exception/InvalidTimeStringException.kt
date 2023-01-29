package no.modio.demo.customerstateprocessorv1.kafka.exception

class InvalidTimeStringException(val timestring: String, override val message: String = "Time string '$timestring' is not in a valid format") : RuntimeException(message)