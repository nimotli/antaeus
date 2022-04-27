package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InsufficientBalanceException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.CurrencyConverter
import io.pleo.antaeus.core.helper.PaymentProviderWrapper
import io.pleo.antaeus.core.helper.ThreadHelper
import io.pleo.antaeus.core.services.BillingService.Companion.NETWORK_FAILURE_RETRY_DELAY
import io.pleo.antaeus.models.InvoiceStatus
import org.junit.jupiter.api.Test

class BillingServiceTest {
    private val paymentProviderWrapper = mockk<PaymentProviderWrapper>()
    private val invoiceService = mockk<InvoiceService>()
    private val currencyConverter = mockk<CurrencyConverter>()
    private val customerService = mockk<CustomerService>()
    private val threadHelper = mockk<ThreadHelper>()

    private val billingService = BillingService(
        paymentProviderWrapper = paymentProviderWrapper,
        invoiceService = invoiceService,
        currencyConverter = currencyConverter,
        customerService = customerService,
        threadHelper = threadHelper
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
        every {customerService.suspendCustomer(aFailedInvoice.customerId)} returns Unit

        billingService.processInvoices()

        verify { paymentProviderWrapper.charge(aPendingInvoice) }
        verify { invoiceService.update(aFailedInvoice) }
        verify { customerService.suspendCustomer(aFailedInvoice.customerId) }
    }

    @Test
    fun `will handle customer not found`() {
        every { invoiceService.fetchPending() } returns listOf(aPendingInvoice)
        every { paymentProviderWrapper.charge(aPendingInvoice) } throws CustomerNotFoundException(
            aPendingInvoice.customerId
        )
        every { invoiceService.update(aFailedInvoice) } returns aFailedInvoice

        billingService.processInvoices()

        verify { paymentProviderWrapper.charge(aPendingInvoice) }
        verify { invoiceService.update(aFailedInvoice) }
    }

    @Test
    fun `will handle currency mismatch`() {
        every { invoiceService.fetchPending() } returns listOf(aPendingInvoice)
        every { paymentProviderWrapper.charge(aPendingInvoice) } throws CurrencyMismatchException(
            aPendingInvoice.id, aPendingInvoice.customerId
        )
        every { invoiceService.update(anInvalidInvoice) } returns anInvalidInvoice
        every { customerService.fetch(aPendingInvoice.customerId) } returns aUsdCustomer
        every { currencyConverter.convertMoneyTo(aPendingInvoice.amount, aUsdCustomer.currency) } returns aUsdMoney
        every { invoiceService.create(aUsdPendingInvoice) } returns aUsdPendingInvoice

        billingService.processInvoices()

        verify { paymentProviderWrapper.charge(aPendingInvoice) }
        verify { invoiceService.update(anInvalidInvoice) }
        verify { customerService.fetch(aPendingInvoice.customerId) }
        verify { invoiceService.create(aUsdPendingInvoice) }
        verify { currencyConverter.convertMoneyTo(aPendingInvoice.amount, aUsdCustomer.currency) }
    }

    @Test
    fun `will handle network exception`() {
        every { paymentProviderWrapper.charge(aPendingInvoice) } throws NetworkException()
        every { invoiceService.fetchPending() } returns listOf(aPendingInvoice)
        every { invoiceService.update(aFailedInvoice) } returns aFailedInvoice
        every { threadHelper.sleep(NETWORK_FAILURE_RETRY_DELAY) } returns Unit

        billingService.processInvoices()

        verify(exactly = BillingService.MAX_NETWORK_FAILURES) { paymentProviderWrapper.charge(aPendingInvoice) }
        verify(exactly = BillingService.MAX_NETWORK_FAILURES) { threadHelper.sleep(NETWORK_FAILURE_RETRY_DELAY) }
        verify { invoiceService.update(aFailedInvoice) }
    }

}