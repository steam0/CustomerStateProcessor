package no.modio.demo.customerstateprocessorv1.command

import no.modio.demo.customer.command.single.ChangeBankAccountNumber
import no.modio.demo.customer.event.BankAccountNumberAdded
import no.modio.demo.customer.event.BankAccountNumberRemoved
import no.modio.demo.customer.event.BankAccountNumberUpdated
import no.modio.demo.customer.event.Event
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer

fun ChangeBankAccountNumber.execute(aggregate: Customer, metadata: Metadata): List<Event> {
    val existing = aggregate.bankAccountNumber?.number
    val newOrUpdated = bankAccountNumber
    return when (existing) {
        newOrUpdated -> emptyList()
        null -> when (newOrUpdated.isNullOrBlank()) {
            true -> emptyList()
            false -> listOf(Event(BankAccountNumberAdded(newOrUpdated), metadata))
        }
        else -> when (newOrUpdated.isNullOrBlank()) {
            true -> listOf(Event(BankAccountNumberRemoved(existing), metadata))
            false -> listOf(Event(BankAccountNumberUpdated(newOrUpdated), metadata))
        }
    }
}