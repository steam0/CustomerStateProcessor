package no.modio.demo.customerstateprocessorv1.util

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe

class CustomerStatusTest: FreeSpec({
    "Create enums directly with correct string" {
        CustomerStatusType.valueOf("CREATED") shouldBe CustomerStatusType.CREATED
        CustomerStatusType.valueOf("ACTIVE") shouldBe CustomerStatusType.ACTIVE
        CustomerStatusType.valueOf("TEMPORARILY_DISMISSED") shouldBe CustomerStatusType.TEMPORARILY_DISMISSED
        CustomerStatusType.valueOf("BLACKLISTED") shouldBe CustomerStatusType.BLACKLISTED
        CustomerStatusType.valueOf("AWAITING_TERMINATION") shouldBe CustomerStatusType.AWAITING_TERMINATION
        CustomerStatusType.valueOf("TERMINATED") shouldBe CustomerStatusType.TERMINATED

        shouldThrow<IllegalArgumentException> { CustomerStatusType.valueOf("NON_EXISTING_STATUS") }
        shouldThrow<IllegalArgumentException> { CustomerStatusType.valueOf("Created") }
        shouldThrow<IllegalArgumentException> { CustomerStatusType.valueOf("active") }
    }

    "Create enums indirectly based on enum.status" {
        CustomerStatusType.getEnumByStringStatus("Created") shouldBe CustomerStatusType.CREATED
        CustomerStatusType.getEnumByStringStatus("Active") shouldBe CustomerStatusType.ACTIVE
        CustomerStatusType.getEnumByStringStatus("Temporarily dismissed") shouldBe CustomerStatusType.TEMPORARILY_DISMISSED
        CustomerStatusType.getEnumByStringStatus("Blacklisted") shouldBe CustomerStatusType.BLACKLISTED
        CustomerStatusType.getEnumByStringStatus("Awaiting termination") shouldBe CustomerStatusType.AWAITING_TERMINATION
        CustomerStatusType.getEnumByStringStatus("Terminated") shouldBe CustomerStatusType.TERMINATED

        shouldThrow<IllegalStateException> { CustomerStatusType.getEnumByStringStatus("") }
        shouldThrow<IllegalStateException> { CustomerStatusType.getEnumByStringStatus("Non-existing status") }
    }

    "Do valid person status changes" {
        fun checkValidStateChanges (current: String, new: List<String>){
            new.forEach {
                shouldNotThrow<Exception> {
                    CustomerStatusType.checkIfValidCustomerStatusChangeOrThrowError(current, it)
                }
            }
        }

        checkValidStateChanges("Created", listOf("Active"))
        checkValidStateChanges("Active", listOf("Temporarily dismissed", "Blacklisted", "Awaiting termination"))
        checkValidStateChanges("Temporarily dismissed", listOf("Active", "Blacklisted"))
        checkValidStateChanges("Blacklisted", listOf("Active"))
        checkValidStateChanges("Awaiting termination", listOf("Active", "Terminated"))
    }

    "Do invalid person status changes" {
        fun checkStatusesThrows(current: String, new: List<String>) {
            new.forEach {
                shouldThrow<IllegalStateException> {
                    CustomerStatusType.checkIfValidCustomerStatusChangeOrThrowError(current, it)}
                }
        }
        checkStatusesThrows("Created", listOf("Created", "Temporarily dismissed", "Blacklisted", "Awaiting termination", "Terminated"))
        checkStatusesThrows("Active", listOf("Created", "Active", "Terminated"))
        checkStatusesThrows("Temporarily dismissed", listOf("Created", "Temporarily dismissed", "Awaiting termination", "Terminated"))
        checkStatusesThrows("Blacklisted", listOf("Created", "Temporarily dismissed", "Blacklisted", "Awaiting termination", "Terminated"))
        checkStatusesThrows("Awaiting termination", listOf("Created", "Temporarily dismissed", "Blacklisted", "Awaiting termination"))
        checkStatusesThrows("Terminated", listOf("Created", "Active", "Temporarily dismissed", "Blacklisted", "Awaiting termination", "Terminated"))
    }

    "Do invalid status submission on changes" {
        fun checkStatusThrows(current: String, new: String) {
            shouldThrow<IllegalStateException> {
                CustomerStatusType.checkIfValidCustomerStatusChangeOrThrowError(current, new)
            }
        }
        checkStatusThrows("Created", "Non-existing-status")
        checkStatusThrows("Non-existing-status", "Created")
        checkStatusThrows("Non-existing-status", "Non-existing-status")
        checkStatusThrows("", "")
    }


})