package interview.models

import kotlinx.serialization.Serializable

@Serializable
data class FulfillmentConfirmation(val orderId: String)