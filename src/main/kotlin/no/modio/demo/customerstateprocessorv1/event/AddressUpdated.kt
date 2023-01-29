package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.AddressUpdated
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun AddressUpdated.apply(aggregate: Customer, metadata: Metadata): Customer {
        val existingAddress = aggregate.address.firstOrNull { existingAddress -> existingAddress.type == this.address.type }

        existingAddress?.let {
                existingAddress.address1 = this.address.address1
                existingAddress.address2 = this.address.address2
                existingAddress.postalCode = this.address.postalCode
                existingAddress.postalArea = this.address.postalArea
                existingAddress.country = this.address.country
        }

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}