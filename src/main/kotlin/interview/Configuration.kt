package interview

import interview.clients.FulfillmentProviderClient
import interview.mock.fulfillment.FulfillmentService
import interview.persistence.OrderRepository
import interview.services.OrderService
import io.ktor.server.config.ApplicationConfig
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlin.time.Duration

object Configuration {
    val fulfillmentService = FulfillmentService()
    lateinit var orderService: OrderService

    // TODO: what to do at initialization exception?
    fun initialize(appConfig: ApplicationConfig) {
        val httpTimeout: Long = Duration.parse(appConfig.property("http-request.timeout").getString()).inWholeMilliseconds
        val httpRetries = appConfig.property("http-request.retries").getString().toInt()
        val fulfillmentProviderBaseUrl = appConfig.property("fulfillment-provider.base-url").getString()
        val orderServiceThreadPoolSize = appConfig.property("order-service.thread-pool-size").getString().toInt()
        val orderServiceCoroutineDispatcher = Executors.newFixedThreadPool(orderServiceThreadPoolSize).asCoroutineDispatcher()

        val orderRepository = OrderRepository()
        val fulfillmentProviderClient = FulfillmentProviderClient(httpTimeout, httpRetries, fulfillmentProviderBaseUrl)
        orderService = OrderService(fulfillmentProviderClient, orderRepository, orderServiceCoroutineDispatcher)
    }
}
