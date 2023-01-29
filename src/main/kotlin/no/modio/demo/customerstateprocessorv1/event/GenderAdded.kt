package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.GenderAdded
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun GenderAdded.apply(aggregate: Customer, metadata: Metadata): Customer {
    aggregate.gender = this.gender

    log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

    return aggregate
}
