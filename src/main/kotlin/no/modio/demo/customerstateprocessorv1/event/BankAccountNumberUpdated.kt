package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.BankAccountNumberUpdated
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.BankAccountNumber
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log
import java.time.ZonedDateTime

fun BankAccountNumberUpdated.apply(aggregate: Customer, metadata: Metadata): Customer {
        aggregate.bankAccountNumber = BankAccountNumber(
                this.bankAccountNumber,
                ZonedDateTime.parse(metadata.timestamp).toInstant()
        )

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}
