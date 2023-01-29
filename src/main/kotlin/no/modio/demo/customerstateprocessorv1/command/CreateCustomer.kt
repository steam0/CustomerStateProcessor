package no.modio.demo.customerstateprocessorv1.command

import no.modio.demo.customer.command.single.CreateCustomer
import no.modio.demo.customer.event.CustomerCreated
import no.modio.demo.customer.event.Event
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.util.CustomerStatusType

fun CreateCustomer.execute(aggregate: Customer, metadata: Metadata): List<Event> {
    return listOf(Event(CustomerCreated(this.customerId, CustomerStatusType.CREATED.status), metadata))
}