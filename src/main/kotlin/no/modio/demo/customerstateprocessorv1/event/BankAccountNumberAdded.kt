package no.modio.demo.customerstateprocessorv1.event

import no.modio.demo.customer.event.BankAccountNumberAdded
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.BankAccountNumber
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.util.log
import java.time.ZonedDateTime

fun BankAccountNumberAdded.apply(aggregate: Customer, metadata: Metadata): Customer {
        aggregate.bankAccountNumber = BankAccountNumber(
                this.bankAccountNumber,
                ZonedDateTime.parse(metadata.timestamp).toInstant()
        )

        log.debug("[Apply (${this::class.java.simpleName})]: $aggregate")

        return aggregate
}
