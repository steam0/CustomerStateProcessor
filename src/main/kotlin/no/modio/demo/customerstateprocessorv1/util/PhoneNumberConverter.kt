package no.modio.demo.customerstateprocessorv1.util

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import no.modio.demo.customer.state.PhoneNumber

fun convertPhoneNumber(type: String, number: String): PhoneNumber? {
    return when (val googlePhoneNumber = parsePhoneNumber(number)) {
        null -> null
        else -> PhoneNumber(type, googlePhoneNumber.countryCode.toString(), googlePhoneNumber.nationalNumber.toString())
    }
}

fun parsePhoneNumber(number: String): Phonenumber.PhoneNumber? {
    return try {
        PhoneNumberUtil.getInstance().parse(number, "NO")
    } catch (exception: NumberParseException) {
        null
    }
}