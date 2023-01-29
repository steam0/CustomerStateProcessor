package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.PhoneNumberAdded
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun PhoneNumberAdded.apply(aggregate: Customer, metadata: Metadata): Customer {
        aggregate.phoneNumbers.add(this.phoneNumber)

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}