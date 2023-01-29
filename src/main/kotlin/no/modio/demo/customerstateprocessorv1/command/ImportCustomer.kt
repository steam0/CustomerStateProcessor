package no.modio.demo.customerstateprocessorv1.command

import no.modio.demo.customer.command.single.ImportCustomer
import no.modio.demo.customer.event.CustomerImported
import no.modio.demo.customer.event.Event
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer

fun ImportCustomer.execute(aggregate: Customer, metadata: Metadata): List<Event> {
        return listOf(Event(CustomerImported(customer), metadata))
}