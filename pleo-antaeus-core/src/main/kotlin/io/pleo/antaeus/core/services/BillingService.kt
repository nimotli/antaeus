package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.helper.PaymentProviderWrapper
import io.pleo.antaeus.models.Invoice

class BillingService(
    private val paymentProviderWrapper: PaymentProviderWrapper,
    private val invoiceService: InvoiceService
) {

    fun processInvoices() {
        val pendingInvoices = invoiceService.fetchPending()
        pendingInvoices.forEach {
            processInvoice(it)
        }
    }

    private fun processInvoice(invoice: Invoice) {

    }
}
