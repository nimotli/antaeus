package io.pleo.antaeus.core.external

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Money
//mock currency converter
interface CurrencyConverter {

    fun convertMoneyTo(money: Money, currency: Currency):Money
}