package io.pleo.antaeus.core.helper

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class PaymentProviderWrapper(
    private val paymentProvider: PaymentProvider
) {
    fun charge(invoice: Invoice): InvoiceStatus {

    }
}