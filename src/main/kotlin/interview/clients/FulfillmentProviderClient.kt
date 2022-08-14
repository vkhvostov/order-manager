package interview.clients

import arrow.core.Either
import interview.FulfillmentError
import interview.OrderManagementError
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeout.HttpTimeoutCapabilityConfiguration
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

object FulfillmentProviderClient {

    // TODO: proper configuration and taking value from there
    private const val fiveMinutes: Long = 5 * 1000 * 60

    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            HttpTimeoutCapabilityConfiguration(
                requestTimeoutMillis = fiveMinutes,
                connectTimeoutMillis = fiveMinutes,
                socketTimeoutMillis = fiveMinutes
            )
        }
        install(HttpRequestRetry) {
            exponentialDelay()
            maxRetries = 5
        }
    }

    /**
     * Assuming that fulfillment provider can reject double fulfillment requests based on order ID
     */
    suspend fun sendFulfillmentRequest(orderId: Int): Either<OrderManagementError, Boolean> {
        return Either.catch { sendRequest(orderId) }
            .mapLeft { FulfillmentError("Error during sending order with ID $orderId for fulfillment", it) }
    }

    private suspend fun sendRequest(orderId: Int): Boolean {
        // TODO: Move to settings
        val response = client.post("http://127.0.0.1:8080/fulfillment-request") {
            contentType(ContentType.Application.Json)
            setBody("{ \"orderId\": $orderId }") // TODO: part the object
        }

        return when (response.status) {
            HttpStatusCode.Accepted -> true
            else -> false
        }
    }
}
