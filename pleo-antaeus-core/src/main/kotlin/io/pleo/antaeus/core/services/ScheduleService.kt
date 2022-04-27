package io.pleo.antaeus.core.services

import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ScheduleService(
    private val billingService: BillingService
) {

    fun execute() {
        GlobalScope.launch {
            //Process invoices at every hour in the first day of every month
            doInfinity("1 1 * 1 *", block = {
                billingService.processInvoices()
            })
        }
    }
}