package no.modio.demo.customerstateprocessorv1.kafka.util

import no.modio.demo.customer.meta.Id
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.*
import java.time.Instant
import java.util.*

fun createCustomer(customerId: String = UUID.randomUUID().toString(), status: String = "Active"): Customer {
    return Customer(
        customerId,
        createName(),
        status,
        "Male",
        createKarValidatedBankAccountNumber(),
        createPhoneNumber(),
        createEmail(),
        createAddress(),
    )
}

fun createKarValidatedBankAccountNumber(): BankAccountNumber {
    return BankAccountNumber(
        "99050599123",
        Instant.parse("2023-01-18T13:44:50.996Z")
    )
}

fun createPhoneNumber(): List<PhoneNumber> {
    return listOf(
        PhoneNumber("mobile", "47", "42225555")
    )
}

fun createEmail(): Email {
    return Email("test@nt.no")
}

fun createAddress(): List<Address> {
    return listOf(
        Address(
            "FREG",
            "Hjemmeveien 1",
            null,
            "0777",
            "Oslo",
            "Norway"
        )
    )
}

fun createName(firstname: String = "Ola", middlename: String = "Test", lastname: String = "Normann"): Name {
    return Name(
        firstname,
        lastname,
        middlename,
        null
    )
}

fun createMetadata(): Metadata = Metadata(
    "2021-10-01T14:08:19+02:00",
    "TestVendor",
    "ClientId",
    "123",
    "description",
    listOf(Id("IdType", "IdValue"))
)