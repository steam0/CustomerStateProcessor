package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.PhoneNumberUpdated
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun PhoneNumberUpdated.apply(aggregate: Customer, metadata: Metadata): Customer {
        val existingPhoneNumber = aggregate.phoneNumbers.firstOrNull { existingPhoneNumber -> existingPhoneNumber.type == this.phoneNumber.type }

        existingPhoneNumber?.let {
                existingPhoneNumber.country = this.phoneNumber.country
                existingPhoneNumber.number = this.phoneNumber.number
        }

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}