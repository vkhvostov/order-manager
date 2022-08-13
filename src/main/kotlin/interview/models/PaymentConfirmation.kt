package interview.models

import kotlinx.serialization.Serializable

@Serializable
data class PaymentConfirmation(val orderId: String)
