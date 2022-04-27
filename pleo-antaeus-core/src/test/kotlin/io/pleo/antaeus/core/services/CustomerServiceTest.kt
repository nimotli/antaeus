package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.CustomerStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CustomerServiceTest {
    private val dal = mockk<AntaeusDal> {

    }

    private val customerService = CustomerService(dal = dal)

    @Test
    fun `will throw if customer is not found`() {
        every { dal.fetchCustomer(404) } returns null
        assertThrows<CustomerNotFoundException> {
            customerService.fetch(404)
        }
    }

    @Test
    fun `will suspend customer`(){
        every { dal.fetchCustomer(404) } returns aUsdCustomer
        every { dal.updateCustomer(id = 404, status = CustomerStatus.SUSPENDED, currency = aUsdCustomer.currency) } returns aUsdCustomer

        customerService.suspendCustomer(404)

        verify { dal.updateCustomer(id = 404, status = CustomerStatus.SUSPENDED, currency = aUsdCustomer.currency) }
    }
    @Test
    fun `will throw if customer is not found while trying to suspend customer`() {
        every { dal.fetchCustomer(404) } returns null
        assertThrows<CustomerNotFoundException> {
            customerService.suspendCustomer(404)
        }
    }
}
