package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.CustomerCreated
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Address
import no.modio.demo.customer.state.Customer
import no.modio.demo.customer.state.PhoneNumber
import no.modio.demo.customerstateprocessorv1.event.util.log

fun CustomerCreated.apply(aggregate: Customer, metadata: Metadata): Customer {
        val newCustomer = Customer(
                this.customerId,
                null,
                this.status,
                null,
                null,
                mutableListOf<PhoneNumber>(),
                null,
                mutableListOf<Address>()
        )

        log.debug("[Apply (${this::class.java.simpleName})]: $newCustomer")

        return newCustomer
}