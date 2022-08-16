package interview.services

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.separateEither
import interview.OrderManagementError
import interview.clients.FulfillmentProviderClient
import interview.models.OrderStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class OrderProcessor(
    private val fulfillmentProviderClient: FulfillmentProviderClient,
    private val orderService: OrderService,
    private val coroutineDispatcher: CoroutineDispatcher,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * This function processes PAID orders
     * 1. Finds all PAID orders in the database
     * 2. In parallel sends fulfillment requests to the external dependency
     * 3. In parallel updates order statuses to IN_FULFILLMENT
     */
    fun processPaidOrders(): Either<List<OrderManagementError>, Unit> =
        orderService.findAll(OrderStatus.PAID).mapLeft { listOf(it) }.flatMap { orders ->
            logger.debug("Starting processing for ${orders.size} orders")
            runBlocking(coroutineDispatcher) {
                orders.filter { it.id != null }.map { order ->
                    async {
                        fulfillmentProviderClient.sendFulfillmentRequest(order.id!!)
                            .flatMap { orderService.updateOrderStatus(order.id, OrderStatus.IN_FULFILLMENT) }
                    }
                }.awaitAll()
            }.separateEither().let { if (it.first.isNotEmpty()) it.first.left() else Unit.right() }
        }
}
