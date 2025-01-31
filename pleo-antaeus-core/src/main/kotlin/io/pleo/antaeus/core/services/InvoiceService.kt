/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun fetchPending(): List<Invoice>{
        return dal.fetchPendingInvoices()
    }

    fun update(invoice: Invoice) : Invoice{
        return dal.updateInvoice(
            id = invoice.id,
            customerId = invoice.customerId,
            status = invoice.status,
            amount = invoice.amount
        )!!
    }

    fun create(invoice: Invoice): Invoice{
        return dal.createInvoice(
            customerId = invoice.customerId,
            status = invoice.status,
            amount = invoice.amount
        )!!
    }
}
