package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InsufficientBalanceException
import io.pleo.antaeus.core.external.CurrencyConverter
import io.pleo.antaeus.core.helper.PaymentProviderWrapper
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging

class BillingService(
    private val paymentProviderWrapper: PaymentProviderWrapper,
    private val invoiceService: InvoiceService,
    private val currencyConverter: CurrencyConverter,
    private val customerService: CustomerService
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun processInvoices() {
        val pendingInvoices = invoiceService.fetchPending()
        pendingInvoices.forEach {
            processInvoice(it)
        }
    }

    private fun processInvoice(invoice: Invoice) {
        val newStatus = try {
            paymentProviderWrapper.charge(invoice)
        } catch (e: InsufficientBalanceException) {
            handleInsufficientBalanceException(invoice)
        }catch (e: CustomerNotFoundException) {
            handleCustomerNotFoundException(invoice)
        }catch (e: CurrencyMismatchException) {
            handleCurrencyMismatch(invoice)
        }
        invoiceService.update(invoice.copy(status = newStatus))
    }

    private fun handleInsufficientBalanceException(invoice: Invoice): InvoiceStatus {
        logger.error("Balance of account '${invoice.customerId}' is less than amount of invoice '${invoice.id}'")
        return InvoiceStatus.FAILED
    }

    private fun handleCustomerNotFoundException(invoice: Invoice): InvoiceStatus {
        logger.error("Customer '${invoice.customerId}' was not found")
        return InvoiceStatus.FAILED
    }

    private fun handleCurrencyMismatch(invoice: Invoice): InvoiceStatus {
        logger.warn("Currency of invoice '${invoice.id}' does not match currency of customer '${invoice.customerId}'")
        val customer = customerService.fetch(invoice.customerId)
        val convertedAmount = currencyConverter.convertMoneyTo(invoice.amount, customer.currency)
        invoiceService.create(invoice.copy(amount = convertedAmount))
        return InvoiceStatus.INVALID
    }
}
