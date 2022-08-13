package interview.clients

import interview.models.OrderStatus
import interview.services.OrderService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking

object FulfillmentProviderClient {

    // TODO: proper timeouts
    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
            connectTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
            socketTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
        }
        install(HttpRequestRetry) {
            exponentialDelay()
            maxRetries = 5
        }
    }
    private val coroutineDispatcher = Executors.newFixedThreadPool(5).asCoroutineDispatcher()

    fun sendFulfillmentRequest(orderId: Int) {
        runBlocking(coroutineDispatcher) {
            val successful = sendRequest(orderId)
            if (successful) OrderService.updateOrderStatus(orderId, OrderStatus.IN_FULFILLMENT)
            // TODO: proper error handling
            // TODO: get result back?
        }
    }

    private suspend fun sendRequest(orderId: Int): Boolean {
        val response = client.post("http://127.0.0.1:8080/fulfillment-request") {
            contentType(ContentType.Application.Json)
            setBody("{ \"orderId\": $orderId }")
        }
//        val response = client.request("http://localhost:8080/fulfillment-request")

        // TODO: response handling
        return when (response.status) {
            HttpStatusCode.Accepted -> true
            else -> false
        }
    }

//    fun HttpClient.foo(url: String) = put(url)
}
