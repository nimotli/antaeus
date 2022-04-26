package io.pleo.antaeus.core.helper

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InsufficientBalanceException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.core.services.aPendingInvoice
import io.pleo.antaeus.models.InvoiceStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentProviderWrapperTest {
    private val paymentProvider = mockk<PaymentProvider>()

    private val paymentProviderWrapper = PaymentProviderWrapper(
        paymentProvider = paymentProvider
    )

    @Test
    fun `will process invoice correctly`() {
        every { paymentProvider.charge(aPendingInvoice) } returns true

        val actual = paymentProviderWrapper.charge(aPendingInvoice)

        verify { paymentProvider.charge(aPendingInvoice) }
        assertEquals(actual, InvoiceStatus.PAID)
    }

    @Test
    fun `will throw InsufficientBalanceException when customer account balance is less than invoice amount`() {
        every { paymentProvider.charge(aPendingInvoice) } returns false

        val exception = assertThrows<InsufficientBalanceException> { paymentProviderWrapper.charge(aPendingInvoice) }

        verify { paymentProvider.charge(aPendingInvoice) }
        assertEquals(
            exception.message,
            "Balance of account '${aPendingInvoice.customerId}' is less than amount of invoice '${aPendingInvoice.id}'"
        )
    }

    @Test
    fun `will throw CustomerNotFoundException when no customer has the given id`() {
        every { paymentProvider.charge(aPendingInvoice) } throws CustomerNotFoundException(id = aPendingInvoice.customerId)

        val exception = assertThrows<CustomerNotFoundException> { paymentProviderWrapper.charge(aPendingInvoice) }

        verify { paymentProvider.charge(aPendingInvoice) }
        assertEquals(
            exception.message,
            "Customer '${aPendingInvoice.customerId}' was not found"
        )
    }

    @Test
    fun `will throw CurrencyMismatchException when the currency does not match the customer account`() {
        every { paymentProvider.charge(aPendingInvoice) } throws CurrencyMismatchException(
            invoiceId = aPendingInvoice.id,
            customerId = aPendingInvoice.customerId
        )

        val exception = assertThrows<CurrencyMismatchException> { paymentProviderWrapper.charge(aPendingInvoice) }

        verify { paymentProvider.charge(aPendingInvoice) }
        assertEquals(
            exception.message,
            "Currency of invoice '${aPendingInvoice.id}' does not match currency of customer '${aPendingInvoice.customerId}'"
        )
    }

    @Test
    fun `will throw NetworkException when a network error happens`() {
        every { paymentProvider.charge(aPendingInvoice) } throws NetworkException()

        val exception = assertThrows<NetworkException> { paymentProviderWrapper.charge(aPendingInvoice) }

        verify { paymentProvider.charge(aPendingInvoice) }
        assertEquals(
            exception.message,
            "A network error happened please try again."
        )
    }
}