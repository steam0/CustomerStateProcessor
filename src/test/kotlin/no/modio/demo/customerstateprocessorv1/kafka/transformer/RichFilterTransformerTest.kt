package no.modio.demo.customerstateprocessorv1.kafka.transformer

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.test.TestRecord

class RichFilterTransformerTest : FreeSpec({

    fun testRecord(key: String, value: String, flag: Boolean = false) = TestRecord(
        key,
        value,
        if (flag) {
            RecordHeaders(listOf(RecordHeader("flag", "true".toByteArray())))
        } else {
            RecordHeaders()
        }
    )

    val testRecords = listOf(
        testRecord("A", "1"),
        testRecord("B", "2", true),
        testRecord("C", "3"),
        testRecord("D", "4", true),
    )

    suspend fun testRichFilter(
        predicate: RichPredicate<String, String>,
        inputRecords: List<TestRecord<String, String>> = testRecords,
        outputTopicTest: suspend TestOutputTopic<String, String>.() -> Unit
    ) {
        val topology = StreamsBuilder().apply {
            stream("input", Consumed.with(Serdes.String(), Serdes.String()))
                .richFilter(predicate)
                .to("output", Produced.with(Serdes.String(), Serdes.String()))
        }.build()

        val config = mapOf(
            StreamsConfig.APPLICATION_ID_CONFIG to RichFilterTransformerTest::class.java.simpleName,
            StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "whocares:9092",
        )

        TopologyTestDriver(topology, config.toProperties()).use { testDriver ->
            val inputTopic = testDriver.createInputTopic("input", StringSerializer(), StringSerializer())
            val outputTopic = testDriver.createOutputTopic("output", StringDeserializer(), StringDeserializer())

            inputTopic.pipeRecordList(inputRecords)
            outputTopic.outputTopicTest()
        }
    }

    "filter by key" {
        testRichFilter({ key, _, _ -> key == "A" }) {
            readKeyValuesToMap() shouldContainExactly mapOf(
                "A" to "1",
            )
        }
    }
    "filter by value" {
        testRichFilter({ _, value, _ -> value == "2" }) {
            readKeyValuesToMap() shouldContainExactly mapOf(
                "B" to "2",
            )
        }
    }
    "filter by header" {
        val includeFlaggedPredicate: RichPredicate<String, String> = { _, _, context ->
            context.headers().lastHeader("flag") != null
        }
        testRichFilter(includeFlaggedPredicate) {
            readKeyValuesToMap() shouldContainExactly mapOf(
                "B" to "2",
                "D" to "4",
            )
        }
    }
    "include all" {
        testRichFilter({ _, _, _ -> true }) {
            readKeyValuesToMap() shouldContainExactly mapOf(
                "A" to "1",
                "B" to "2",
                "C" to "3",
                "D" to "4",
            )
        }
    }
    "include none" {
        testRichFilter({ _, _, _ -> false }) {
            readRecordsToList().shouldBeEmpty()
        }
    }
})
