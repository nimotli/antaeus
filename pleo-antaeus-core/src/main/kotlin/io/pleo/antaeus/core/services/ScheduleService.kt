package io.pleo.antaeus.core.services

import dev.inmo.krontab.doInfinity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ScheduleService(
    private val billingService: BillingService
) {

    fun execute() {
        GlobalScope.launch {
            doInfinity("1 1 * 1 *", block = {
                billingService.processInvoices()
            })
        }
    }
}