package interview.models

import kotlinx.serialization.Serializable

@Serializable
data class FulfillmentRequest(val orderId: Int)
