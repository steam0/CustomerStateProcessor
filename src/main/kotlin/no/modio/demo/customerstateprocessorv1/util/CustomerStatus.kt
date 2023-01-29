package no.modio.demo.customerstateprocessorv1.util

/**
@see http://customer.docs.norsk-tipping.no/#/?id=customer-status For more information about the statuses, and for the flowchart presented with Mermaid.js
@see no.modio.demo.customerstateprocessorv1.command.util.CustomerStatus.CustomerStatusType#validNewStatuses (the valid state changes are implemented there)
@see no.modio.demo.customerstateprocessorv1.command.rcac.ChangeCustomerStatus the calling class, which also handles status changes to the same status (for instance Active -> Active)
This flowchart portrays the statuses and the valid status changes that can be done on a customer

                            │
                            ▼
                       ┌─────────┐
                       │ Created │
                       └────┬────┘
                            ▼
                        ┌────────┐
           ┌────────────┤ Active ├───────────┐
           │            └─────┬──┘           │
           │              ▲ ▲ │ ▲            │
           ▼              │ │ │ │            ▼
┌───────────────────────┐ │ │ │ │ ┌──────────────────────┐
│ Temporarily dismissed ├─┘ │ │ └─┤ Awaiting termination │
└──────────┬────────────┘   │ │   └──────────┬───────────┘
           │                │ ▼              ▼
           │         ┌──────┴──────┐  ┌────────────┐
           └────────►│ Blacklisted │  │ Terminated │
                     └─────────────┘  └────────────┘
 */

enum class CustomerStatusType(val status: String, val buypassCustomerStatus: List<String>) {
    CREATED("Created", listOf("")),
    ACTIVE("Active", listOf("20")),
    TEMPORARILY_DISMISSED("Temporarily dismissed", listOf("41")),
    BLACKLISTED("Blacklisted", listOf("40")),
    AWAITING_TERMINATION("Awaiting termination", listOf("80", "84", "85")),
    TERMINATED("Terminated", listOf("-1"))
    ;

    companion object {
        private val validNewStatuses = mapOf(
            CREATED to listOf(ACTIVE),
            ACTIVE to listOf(TEMPORARILY_DISMISSED, BLACKLISTED, AWAITING_TERMINATION),
            TEMPORARILY_DISMISSED to listOf(ACTIVE, BLACKLISTED),
            BLACKLISTED to listOf(ACTIVE),
            AWAITING_TERMINATION to listOf(ACTIVE, TERMINATED),
            TERMINATED to emptyList<CustomerStatusType>(),
        )

        fun getEnumByStringStatus(status: String): CustomerStatusType {
            return values().find { enum -> enum.status == status } ?:
                throw IllegalStateException("Invalid status '$status'. Valid statuses are: ${values().map { it.status }}")
        }

        fun checkIfValidCustomerStatusChangeOrThrowError(currentStatus: String, inputStatus: String) {
            val current = getEnumByStringStatus(currentStatus)
            val new = getEnumByStringStatus(inputStatus)
            val allowedStatuses = validNewStatuses[current] as List<CustomerStatusType>

            if (!allowedStatuses.contains(new)) throw IllegalStateException(
                "Invalid status change from '${current.status}' to '${new.status}'. Allowed new statuses from '${current.status}' are: '${allowedStatuses.map { it.status }}'"
            )
        }
    }
}