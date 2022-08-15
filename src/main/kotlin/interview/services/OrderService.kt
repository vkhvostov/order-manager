package interview.services

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.left
import arrow.core.sequence
import interview.OrderManagementError
import interview.ValidationError
import interview.clients.FulfillmentProviderClient
import interview.models.Order
import interview.models.OrderStatus
import interview.models.OrderStatus.IN_FULFILLMENT
import interview.models.OrderStatus.PAID
import interview.persistence.OrderRepository
import interview.routes.OrderCreationRequest
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

// TODO: naming: maybe order processor?
class OrderService(
    private val fulfillmentProviderClient: FulfillmentProviderClient,
    private val orderRepository: OrderRepository,
    private val coroutineDispatcher: ExecutorCoroutineDispatcher,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // TODO: check if this can be simplified
    /**
     * This function processes PAID orders
     * 1. Finds all PAID orders in the database
     * 2. In parallel sends fulfillment requests to the external dependency
     * 3. In parallel updates order statuses to IN_FULFILLMENT
     */
    fun processPaidOrders(): Either<List<OrderManagementError>, Unit> =
        findAll(PAID).mapLeft { listOf(it) }.flatMap { orders ->
            logger.debug("Starting order processing for ${orders.size} orders")
            runBlocking(coroutineDispatcher) {
                orders.filter { it.id != null }.map { order ->
                    async {
                        fulfillmentProviderClient.sendFulfillmentRequest(order.id!!)
                            .flatMap { updateOrderStatus(order.id, IN_FULFILLMENT) }
                    }
                }.awaitAll()
            }.map { it.swap() }.sequence().swap()
        }

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

    fun updateOrderStatus(orderId: String, orderStatus: OrderStatus): Either<OrderManagementError, Unit> {
        return Either.catch { orderId.toInt() }
            .mapLeft { ValidationError("Order ID is not an integer") }
            .flatMap { updateOrderStatus(it, orderStatus) }
    }

    private fun updateOrderStatus(orderId: Int?, orderStatus: OrderStatus): Either<OrderManagementError, Unit> {
        return if (orderId != null) {
            logger.info("Updating order $orderId to status $orderStatus")
            orderRepository.updateStatus(orderId, orderStatus)
        } else {
            logger.warn("Updating is not possible due to the null order ID")
            ValidationError("Order ID is null").left()
        }
    }
}
