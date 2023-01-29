package no.modio.demo.customerstateprocessorv1.event.handler

import no.modio.demo.customer.event.*
import no.modio.demo.customer.meta.Metadata
import no.modio.demo.customer.state.Customer
import no.modio.demo.customerstateprocessorv1.event.apply
import org.springframework.stereotype.Component

@Component
object CustomerEventHandler : EventHandler<Customer, Event> {

    override fun apply(aggregate: Customer, event: Event): Customer {
        return when (val eventData = event.event) {
            // Import legacy customers
            is CustomerImported -> eventData.apply(aggregate, event.metadata as Metadata)
            // Initial create
            is CustomerCreated -> eventData.apply(aggregate, event.metadata as Metadata)
            // New element added
            is NameAdded -> eventData.apply(aggregate, event.metadata as Metadata)
            is EmailAdded ->  eventData.apply(aggregate, event.metadata as Metadata)
            is PhoneNumberAdded -> eventData.apply(aggregate, event.metadata as Metadata)
            is BankAccountNumberAdded -> eventData.apply(aggregate, event.metadata as Metadata)
            is AddressAdded -> eventData.apply(aggregate, event.metadata as Metadata)
            is GenderAdded -> eventData.apply(aggregate, event.metadata as Metadata)
            // Existing element updated
            is NameUpdated -> eventData.apply(aggregate, event.metadata as Metadata)
            is EmailUpdated -> eventData.apply(aggregate, event.metadata as Metadata)
            is BankAccountNumberUpdated -> eventData.apply(aggregate, event.metadata as Metadata)
            is AddressUpdated -> eventData.apply(aggregate, event.metadata as Metadata)
            is PhoneNumberUpdated -> eventData.apply(aggregate, event.metadata as Metadata)
            is CustomerStatusUpdated -> eventData.apply(aggregate, event.metadata as Metadata)
            is GenderUpdated -> eventData.apply(aggregate, event.metadata as Metadata)
            // Existing element removed
            is EmailRemoved -> eventData.apply(aggregate, event.metadata as Metadata)
            is PhoneNumberRemoved -> eventData.apply(aggregate, event.metadata as Metadata)
            is BankAccountNumberRemoved -> eventData.apply(aggregate, event.metadata as Metadata)
            is AddressRemoved -> eventData.apply(aggregate, event.metadata as Metadata)
            else -> aggregate
        }
    }
}