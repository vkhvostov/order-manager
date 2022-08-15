package interview

import interview.clients.FulfillmentProviderClient
import interview.mock.fulfillment.FulfillmentService
import interview.persistence.OrderRepository
import interview.services.OrderService
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher

object Context {

    // TODO: proper configuration and taking value from there
    private const val fiveMinutes: Long = 5 * 1000 * 60
    private val httpRetries = 5

    private val fulfillmentProviderBaseUrl = "http://127.0.0.1:8080"

    private val orderServiceCoroutineDispatcher = Executors.newFixedThreadPool(5).asCoroutineDispatcher()

    private val fulfillmentProviderClient = FulfillmentProviderClient(fiveMinutes, httpRetries, fulfillmentProviderBaseUrl)
    private val orderRepository = OrderRepository()

    val fulfillmentService = FulfillmentService()
    val orderService = OrderService(fulfillmentProviderClient, orderRepository, orderServiceCoroutineDispatcher)
}
