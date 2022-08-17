package interview.clients

import arrow.core.Either
import interview.FulfillmentError
import interview.OrderManagementError
import interview.models.FulfillmentRequest
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

open class FulfillmentProviderClient(
    private val httpTimeout: Long,
    private val httpMaxRetries: Int,
    private val fulfillmentProviderBaseUrl: String,
    private val httpThreadCount: Int,
) {

    private val fulfillmentRequestUrl = "/fulfillment-request"

    private val client = HttpClient(CIO) {
        install(HttpTimeout) {
            HttpTimeoutCapabilityConfiguration(
                requestTimeoutMillis = httpTimeout,
                connectTimeoutMillis = httpTimeout,
                socketTimeoutMillis = httpTimeout
            )
        }
        install(HttpRequestRetry) {
            exponentialDelay()
            maxRetries = httpMaxRetries
        }
        engine {
            threadsCount = httpThreadCount
        }
    }

    /**
     * Assuming that fulfillment provider can reject double fulfillment requests based on order ID,
     * so it is not required to validate that before sending order for fulfillment
     */
    suspend fun sendFulfillmentRequest(orderId: Int): Either<OrderManagementError, Boolean> {
        return Either.catch { sendRequest(orderId) }
            .mapLeft { FulfillmentError("Error during sending order with ID $orderId for fulfillment", it) }
    }

    private suspend fun sendRequest(orderId: Int): Boolean {
        val response = client.post("$fulfillmentProviderBaseUrl$fulfillmentRequestUrl") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(FulfillmentRequest(orderId)))
        }

        return when (response.status) {
            HttpStatusCode.Accepted -> true
            else -> false
        }
    }
}
