package no.modio.demo.customerstateprocessorv1.kafka.transformer

import no.modio.demo.customer.command.Command
import no.modio.demo.customer.command.single.*
import no.modio.demo.customer.event.Event
import no.modio.demo.customer.state.Customer
import no.modio.demo.customer.state.CustomerState
import no.modio.demo.customerstateprocessorv1.command.execute
import no.modio.demo.customerstateprocessorv1.event.handler.CustomerEventHandler
import no.modio.demo.customerstateprocessorv1.kafka.exception.InvalidStateKeyException
import no.modio.demo.customerstateprocessorv1.util.deepCopy
import no.modio.demo.customerstateprocessorv1.util.validateTimeString
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore
import java.util.*

class CommandHandlerTransformer(
    private val customerStateStoreName: String,
    private val customerEventHandler: CustomerEventHandler,
) : Transformer<String, Command, KeyValue<String, Result<List<CustomerState?>>>> {
    private lateinit var customerStateStore: KeyValueStore<String, CustomerState>

    override fun init(context: ProcessorContext) {
        customerStateStore = context.getStateStore(customerStateStoreName)
    }

    override fun transform(customerId: String, commandRecord: Command): KeyValue<String, Result<List<CustomerState?>>> {
        try {
            // Get current state
            val customerState: CustomerState? = customerStateStore.get(customerId)

            // Function to validate that we have a current state for commands that requires a previous state
            fun <T> requireCustomer(block: (CustomerState) -> T): T = if (customerState == null) {
                throw IllegalStateException("Cannot execute command. No customer with this id: '$customerId'")
            } else {
                block(customerState)
            }

            // Function to validate that we DO NOT have a current state for commands that requires to start from scratch
            fun <T> requireNoExistingCustomer(block: () -> T): T = if (customerState != null) {
                throw IllegalStateException("Cannot execute command. Customer with this id already exist: '$customerId'")
            } else {
                block()
            }

            validateStateKey(customerState, commandRecord)
            validateMetadata(commandRecord)

            // Convert command to events
            val events = when (val command = commandRecord.command) {
                // Requires no existing customer
                is CreateCustomer -> requireNoExistingCustomer { command.execute(Customer(), commandRecord.metadata) }

                // Requires existing customer
                is ChangeAddress -> requireCustomer { command.execute(it.state, commandRecord.metadata) }
                is ChangeBankAccountNumber -> requireCustomer { command.execute(it.state, commandRecord.metadata) }
                is ChangeEmail -> requireCustomer { command.execute(it.state, commandRecord.metadata) }
                is ChangeCustomerStatus -> requireCustomer { command.execute(it.state, commandRecord.metadata) }
                is ChangeGender -> requireCustomer { command.execute(it.state, commandRecord.metadata) }
                is ChangeName -> requireCustomer { command.execute(it.state, commandRecord.metadata) }
                is ChangePhoneNumber -> requireCustomer { command.execute(it.state, commandRecord.metadata) }

                // Everything else is not implemented
                else -> throw IllegalStateException("Command '${command::class.java.simpleName}' is not implemented")
            }

            // Aggregate customer state and add each state to new list (and then remove initialState as we don't need that one)
            val customerStates = events.runningFold(customerState) { aggregatedState: CustomerState?, event: Event ->
                when (event.event) {
                    else -> CustomerState(
                        customerEventHandler.apply(aggregatedState?.deepCopy()?.state ?: Customer(), event),
                        event,
                        UUID.randomUUID().toString()
                    )
                }
            }.drop(1)

            // Store latest state to local state store
            if (customerStates.isNotEmpty()) {
                val newCustomerState = customerStates.last()

                if (newCustomerState == null) {
                    customerStateStore.delete(customerId)
                } else {
                    customerStateStore.put(customerId, newCustomerState)
                }
            }

            return KeyValue(customerId, Result.success(customerStates))
        } catch (exception: Exception) {
            return KeyValue(customerId, Result.failure(exception))
        }
    }

    private fun validateMetadata(commandRecord: Command) {
        commandRecord.metadata.timestamp.validateTimeString()
    }

    private fun validateStateKey(customerState: CustomerState?, commandRecord: Command) {
        // If the command does not contain a state key, continue
        if (commandRecord.stateKey?.toString().isNullOrBlank()) return

        // If there is no existing state, continue
        if (customerState == null) return

        // If state key on state object matches state key on command, continue
        if (customerState.stateKey == commandRecord.stateKey) return

        throw InvalidStateKeyException()
    }

    override fun close() {
        // No need to close
    }
}