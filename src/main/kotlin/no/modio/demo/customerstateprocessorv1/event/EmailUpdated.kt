package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.EmailUpdated
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun EmailUpdated.apply(aggregate: Customer, metadata: Metadata): Customer {
        aggregate.email = this.email

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}