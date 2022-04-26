package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.InsufficientBalanceException
import io.pleo.antaeus.core.helper.PaymentProviderWrapper
import io.pleo.antaeus.models.InvoiceStatus
import org.junit.jupiter.api.Test

class BillingServiceTest {
    private val paymentProviderWrapper = mockk<PaymentProviderWrapper>()
    private val invoiceService = mockk<InvoiceService>()

    private val billingService = BillingService(
        paymentProviderWrapper = paymentProviderWrapper,
        invoiceService = invoiceService
    )

    @Test
    fun `will process invoice correctly`() {
        every { invoiceService.fetchPending() } returns listOf(aPendingInvoice)
        every { paymentProviderWrapper.charge(aPendingInvoice) } returns InvoiceStatus.PAID
        every { invoiceService.update(aPaidInvoice) } returns aPaidInvoice

        billingService.processInvoices()

        verify { paymentProviderWrapper.charge(aPendingInvoice) }
        verify { invoiceService.update(aPaidInvoice) }
    }

    @Test
    fun `will handle insufficient user balance`() {
        every { invoiceService.fetchPending() } returns listOf(aPendingInvoice)
        every { paymentProviderWrapper.charge(aPendingInvoice) } throws InsufficientBalanceException(
            aPendingInvoice.id, aPendingInvoice.customerId
        )
        every { invoiceService.update(aFailedInvoice) } returns aFailedInvoice

        billingService.processInvoices()

        verify { paymentProviderWrapper.charge(aPendingInvoice) }
        verify { invoiceService.update(aFailedInvoice) }
    }

}