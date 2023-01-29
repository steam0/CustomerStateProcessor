package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.CustomerStatusUpdated
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun CustomerStatusUpdated.apply(aggregate: Customer, metadata: Metadata): Customer {
        aggregate.status = this.status

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}
