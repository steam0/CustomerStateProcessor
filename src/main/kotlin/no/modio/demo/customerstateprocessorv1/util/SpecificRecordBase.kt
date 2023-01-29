package no.modio.demo.customerstateprocessorv1.util

import org.apache.avro.specific.SpecificData
import org.apache.avro.specific.SpecificRecordBase

fun <T : SpecificRecordBase> T.deepCopy(): T = SpecificData.get().deepCopy(schema, this)