package interview.models

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import kotlinx.serialization.Serializable

@Serializable
data class Order(val id: Int? = null, val positions: List<OrderPosition>, val status: OrderStatus)

@Serializable
data class OrderPosition(val positionId: Int? = null, val articleId: String, val amount: Int)

enum class OrderStatus {
    CREATED, PAID, IN_FULFILLMENT, CLOSED;

    companion object {
        fun fromString(value: String): Option<OrderStatus> = runCatching { valueOf(value.uppercase()).some() }.getOrElse { None }
    }
}
