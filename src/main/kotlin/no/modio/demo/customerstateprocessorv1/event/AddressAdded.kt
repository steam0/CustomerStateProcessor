package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.AddressAdded
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun AddressAdded.apply(aggregate: Customer, metadata: Metadata): Customer {
        aggregate.address.add(this.address)

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}