package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.PhoneNumberRemoved
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun PhoneNumberRemoved.apply(aggregate: Customer, metadata: Metadata): Customer {
        val existingPhoneNumber = aggregate.phoneNumbers.firstOrNull { existingPhoneNumber -> existingPhoneNumber.type == this.phoneNumber.type }

        existingPhoneNumber?.let {
                aggregate.phoneNumbers = aggregate.phoneNumbers.toMutableList()
                aggregate.phoneNumbers.remove(existingPhoneNumber)
        }

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}