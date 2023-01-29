package no.modio.demo.customerstateprocessorv1.command

import no.modio.demo.customer.command.single.ChangeName
import no.modio.demo.customer.event.*
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customer.state.Name

fun ChangeName.execute(aggregate: Customer, metadata: Metadata): List<Event> {
    this.validate()

    val existing = aggregate.name
    val newOrUpdated = Name(firstname, lastname, middlename, shortname)

    return when (existing) {
        newOrUpdated -> emptyList()
        null -> listOf(Event(NameAdded(newOrUpdated), metadata))
        else -> listOf(Event(NameUpdated(newOrUpdated), metadata))
    }
}

private fun ChangeName.validate() {
    if (firstname.isNullOrBlank() && lastname.isNullOrBlank()) throw IllegalStateException("Firstname and Lastname are required fields")
}