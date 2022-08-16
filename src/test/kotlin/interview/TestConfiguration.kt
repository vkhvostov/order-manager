package interview

import arrow.core.right
import interview.configuration.Configuration
import interview.mock.fulfillment.FulfillmentService
import interview.models.OrderStatus
import interview.persistence.OrderRepository
import interview.services.OrderProcessor
import interview.services.OrderService
import io.ktor.server.config.ApplicationConfig
import io.mockk.every
import io.mockk.mockk

object TestConfiguration : Configuration {
    override val orderRepository: OrderRepository = mockk()
    override val orderService: OrderService = mockk()
    override val orderProcessor: OrderProcessor = mockk()
    override val fulfillmentService: FulfillmentService = mockk()

    override fun initialize(appConfig: ApplicationConfig) {}
}
