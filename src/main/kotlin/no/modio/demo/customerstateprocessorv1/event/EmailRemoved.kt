package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.EmailRemoved
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun EmailRemoved.apply(aggregate: Customer, metadata: Metadata): Customer {
        aggregate.email = null

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}