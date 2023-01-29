package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.CustomerImported
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun CustomerImported.apply(aggregate: Customer, metadata: Metadata): Customer {
    log.debug("[Apply (${this::class.java.simpleName})]: ${this.customer}")

    return this.customer
}