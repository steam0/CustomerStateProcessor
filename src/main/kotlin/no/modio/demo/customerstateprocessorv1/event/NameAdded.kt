package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.NameAdded
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun NameAdded.apply(aggregate: Customer, metadata: Metadata): Customer {
        aggregate.name = this.name

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}