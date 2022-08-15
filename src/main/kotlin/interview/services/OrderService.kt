package interview.services

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.left
import interview.OrderManagementError
import interview.ValidationError
import interview.models.Order
import interview.models.OrderStatus
import interview.persistence.OrderRepository
import interview.routes.OrderCreationRequest
import org.slf4j.LoggerFactory

class OrderService(
    private val orderRepository: OrderRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun find(orderId: String): Either<OrderManagementError, Option<Order>> {
        return Either.catch { orderId.toInt() }
            .mapLeft { ValidationError("Order ID is not an integer") }
            .flatMap { orderRepository.find(it) }
    }

    fun findAll(status: OrderStatus? = null): Either<OrderManagementError, List<Order>> {
        return orderRepository.findAll(status)
    }

    fun create(orderCreationRequest: OrderCreationRequest): Either<OrderManagementError, Int> {
        val order = Order(positions = orderCreationRequest.positions, status = OrderStatus.CREATED)
        return orderRepository.save(order)
    }

    fun updateOrderStatus(orderId: Int?, orderStatus: OrderStatus): Either<OrderManagementError, Unit> {
        return if (orderId != null) {
            logger.info("Updating order $orderId to status $orderStatus")
            orderRepository.updateStatus(orderId, orderStatus)
        } else {
            logger.warn("Updating is not possible due to the null order ID")
            ValidationError("Order ID is null").left()
        }
    }
}
