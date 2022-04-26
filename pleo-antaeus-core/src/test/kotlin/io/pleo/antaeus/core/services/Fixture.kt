package io.pleo.antaeus.core.services

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import java.math.BigDecimal

val aEurCustomer = Customer(
    id = 1,
    currency = Currency.EUR
)

val aMoney = Money(
    currency = Currency.EUR,
    value = BigDecimal.valueOf(10)
)

val aUsdMoney = Money(
    currency = Currency.USD,
    value = BigDecimal.valueOf(10)
)

val aPendingInvoice = Invoice(
    id = 1,
    amount = aMoney,
    customerId = aEurCustomer.id,
    status = InvoiceStatus.PENDING
)