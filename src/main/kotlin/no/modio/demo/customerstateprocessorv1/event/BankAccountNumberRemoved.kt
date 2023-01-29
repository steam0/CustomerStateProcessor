package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.BankAccountNumberRemoved
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log

fun BankAccountNumberRemoved.apply(aggregate: Customer, metadata: Metadata): Customer {
        aggregate.bankAccountNumber = null

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}
