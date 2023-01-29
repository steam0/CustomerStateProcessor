package no.modio.demo.customerstateprocessorv1.command

import no.modio.demo.customer.command.single.ChangeGender
import no.modio.demo.customer.event.Event
import no.modio.demo.customer.event.GenderAdded
import no.modio.demo.customer.event.GenderUpdated
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer

fun ChangeGender.execute(aggregate: Customer, metadata: Metadata): List<Event> {
    this.validate()

    val existing = aggregate.gender
    val newOrUpdated = gender.lowercase().replaceFirstChar { it.titlecase() }

    return when (existing) {
        newOrUpdated -> emptyList()
        null -> listOf(Event(GenderAdded(newOrUpdated), metadata))
        else -> listOf(Event(GenderUpdated(newOrUpdated), metadata))
    }
}

private fun ChangeGender.validate() {
    if (
        !this.gender.equals("male", ignoreCase = true) &&
        !this.gender.equals("female", ignoreCase = true)
    ) throw IllegalStateException("Gender must be a boolean value: 'Male' or 'Female'")
}