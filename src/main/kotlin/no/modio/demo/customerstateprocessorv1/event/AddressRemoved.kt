package no.modio.demo.customerstateprocessorv1.event


import no.modio.demo.customer.event.AddressRemoved
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun AddressRemoved.apply(aggregate: Customer, metadata: Metadata): Customer {
        val existingAddress = aggregate.address.firstOrNull { existingAddress -> existingAddress.type == this.address.type }

        existingAddress?.let {
                aggregate.address = aggregate.address.toMutableList()
                aggregate.address.remove(existingAddress)
        }

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}