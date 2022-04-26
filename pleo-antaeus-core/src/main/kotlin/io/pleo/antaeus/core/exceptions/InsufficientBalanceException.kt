package io.pleo.antaeus.core.exceptions


class InsufficientBalanceException(invoiceId: Int, customerId: Int) :
    Exception("Balance of account '$customerId' is less than amount of invoice '$invoiceId'")