package no.modio.demo.customerstateprocessorv1.command

import no.modio.demo.customer.command.single.ChangePhoneNumber
import no.modio.demo.customer.event.Event
import no.modio.demo.customer.event.PhoneNumberAdded
import no.modio.demo.customer.event.PhoneNumberRemoved
import no.modio.demo.customer.event.PhoneNumberUpdated
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customer.state.PhoneNumber

fun ChangePhoneNumber.execute(aggregate: Customer, metadata: Metadata): List<Event> {
    val existing = aggregate.phoneNumbers.find { it.type == type }
    val newOrUpdated = PhoneNumber(type, country, number)
    return when (existing) {
        newOrUpdated -> emptyList()
        null -> when (isAllNullOrBlank()) {
            true -> emptyList()
            false -> listOf(Event(PhoneNumberAdded(newOrUpdated), metadata))
        }
        else -> when (isAllNullOrBlank()) {
            true -> listOf(Event(PhoneNumberRemoved(newOrUpdated), metadata))
            false -> listOf(Event(PhoneNumberUpdated(newOrUpdated), metadata))
        }
    }
}

private fun ChangePhoneNumber.isAllNullOrBlank(): Boolean = country.isNullOrBlank() && number.isNullOrBlank()