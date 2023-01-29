package no.modio.demo.customerstateprocessorv1.command

import no.modio.demo.customer.command.single.ChangeEmail
import no.modio.demo.customer.event.EmailAdded
import no.modio.demo.customer.event.EmailRemoved
import no.modio.demo.customer.event.EmailUpdated
import no.modio.demo.customer.event.Event
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customer.state.Email

fun ChangeEmail.execute(aggregate: Customer, metadata: Metadata): List<Event> {
    this.validate()

    val existing = aggregate.email
    val newOrUpdated = Email(email)

    return when (existing) {
        newOrUpdated -> emptyList()
        null -> when (newOrUpdated.email.isNullOrBlank()) {
            true -> emptyList()
            false -> listOf(Event(EmailAdded(newOrUpdated), metadata))
        }
        else -> when (newOrUpdated.email.isNullOrBlank()) {
            true -> listOf(Event(EmailRemoved(existing), metadata))
            false -> listOf(Event(EmailUpdated(newOrUpdated), metadata))
        }
    }
}

private fun ChangeEmail.validate() {
    if (
        !this.email.isNullOrBlank() && !this.email.contains("@")
    ) throw IllegalStateException("Email must include: '@'")
}