package no.modio.demo.customerstateprocessorv1.kafka.transformer

import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext


typealias RichPredicate<K, V> = (key: K, value: V, context: ProcessorContext) -> Boolean

/**
 * Replace with standard library filter when/if rich functions are introduced:
 * https://cwiki.apache.org/confluence/display/KAFKA/KIP-159%3A+Introducing+Rich+functions+to+Streams
 */
class RichFilterTransformer<K, V>(
    val predicate: RichPredicate<K, V>
) : Transformer<K, V, KeyValue<K, V>> {
    private lateinit var context: ProcessorContext

    override fun init(context: ProcessorContext) {
        this.context = context
    }

    override fun transform(key: K, value: V): KeyValue<K, V>? = if (predicate(key, value, context)) {
        KeyValue(key, value)
    } else {
        null
    }

    override fun close() {
        // no need to close
    }
}

fun <K, V> KStream<K, V>.richFilter(predicate: RichPredicate<K, V>): KStream<K, V> =
    transform({ RichFilterTransformer(predicate) })