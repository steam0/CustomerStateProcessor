package no.modio.demo.customerstateprocessorv1.command

import no.modio.demo.customer.command.single.ChangeAddress
import no.modio.demo.customer.event.AddressAdded
import no.modio.demo.customer.event.AddressRemoved
import no.modio.demo.customer.event.AddressUpdated
import no.modio.demo.customer.event.Event
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Address
import no.modio.demo.customer.state.Customer

fun ChangeAddress.execute(aggregate: Customer, metadata: Metadata): List<Event> {
    val existing = aggregate.address.find { it.type == type }
    val newOrUpdated = Address(
        type,
        address1,
        address2,
        postalCode,
        postalArea,
        country
    )

    return when (existing) {
        newOrUpdated -> when (isAllNullOrBlank()) {
            true -> listOf(Event(AddressRemoved(newOrUpdated), metadata))
            false -> emptyList()
        }
        null -> when (isAllNullOrBlank()) {
            true -> emptyList()
            false -> listOf(Event(AddressAdded(newOrUpdated), metadata))
        }
        else -> when (isAllNullOrBlank()) {
            true -> listOf(Event(AddressRemoved(newOrUpdated), metadata))
            false -> listOf(Event(AddressUpdated(newOrUpdated), metadata))
        }
    }
}

private fun ChangeAddress.isAllNullOrBlank(): Boolean =
    address1.isNullOrBlank() && address2.isNullOrBlank() && postalCode.isNullOrBlank() && postalArea.isNullOrBlank() && country.isNullOrBlank()