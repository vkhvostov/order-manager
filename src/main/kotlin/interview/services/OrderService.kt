package interview.services

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.left
import interview.OrderManagementError
import interview.ValidationError
import interview.clients.FulfillmentProviderClient
import interview.models.Order
import interview.models.OrderStatus
import interview.persistence.OrderRepository
import interview.routes.OrderCreationRequest
import org.slf4j.LoggerFactory

// TODO: naming: maybe order processor?
object OrderService {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun find(orderId: String): Either<OrderManagementError, Option<Order>> {
        return Either.catch { orderId.toInt() }
            .mapLeft { ValidationError("Order ID is not an integer") }
            .flatMap { OrderRepository.find(it) }
    }

    fun findAll(): Either<OrderManagementError, List<Order>> {
        return OrderRepository.findAll()
    }

    fun create(orderCreationRequest: OrderCreationRequest): Either<OrderManagementError, Int> {
        val order = Order(positions = orderCreationRequest.positions, status = OrderStatus.CREATED)
        return OrderRepository.save(order)
    }

    fun updateOrderStatus(orderId: String, orderStatus: OrderStatus): Either<OrderManagementError, Int> {
        return Either.catch { orderId.toInt() }
            .mapLeft { ValidationError("Order ID is not an integer") }
            .flatMap { updateOrderStatus(it, orderStatus) }
    }

    fun updateOrderStatus(orderId: Int?, orderStatus: OrderStatus): Either<OrderManagementError, Int> {
        return if (orderId != null) {
            logger.info("Updating order $orderId to status $orderStatus")
            OrderRepository.updateStatus(orderId, orderStatus)
        } else {
            logger.warn("Updating is not possible due to the null order ID")
            ValidationError("Order ID is null").left()
        }
    }

    fun processOrder(orderId: String) {
        FulfillmentProviderClient.sendFulfillmentRequest(orderId.toInt())
    }
}
