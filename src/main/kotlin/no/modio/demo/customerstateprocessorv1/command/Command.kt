package no.modio.demo.customerstateprocessorv1.command

import no.modio.demo.customer.event.Event

interface Command<AggregateA> {

    fun execute(aggregate: AggregateA): List<Event>
}
