package no.modio.demo.customerstateprocessorv1.command

import no.modio.demo.customer.command.single.ChangeCustomerStatus
import no.modio.demo.customer.event.CustomerStatusUpdated
import no.modio.demo.customer.event.Event
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.util.CustomerStatusType

fun ChangeCustomerStatus.execute(aggregate: Customer, metadata: Metadata): List<Event> {
    val existing = aggregate.status
    val newOrUpdated = status

    return buildList {
        if (existing != newOrUpdated) {
            CustomerStatusType.checkIfValidCustomerStatusChangeOrThrowError(existing, newOrUpdated)
            add(Event(CustomerStatusUpdated(newOrUpdated), metadata))
        }
    }
}