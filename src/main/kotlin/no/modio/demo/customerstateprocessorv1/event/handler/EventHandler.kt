package no.modio.demo.customerstateprocessorv1.event.handler

interface EventHandler<T,E> {

    fun apply(aggregate: T, event: E): T

    fun fold(initialState: T, events: List<E>): T {
        return events.fold(initialState) { state, event -> apply(state, event) }
    }
}
