package io.pleo.antaeus.core.external

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Money

interface CurrencyConverter {

    fun convertMoneyTo(money: Money, currency: Currency):Money
}