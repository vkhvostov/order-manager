package interview.services

import arrow.core.left
import arrow.core.right
import interview.FulfillmentError
import interview.PersistenceError
import interview.clients.FulfillmentProviderClient
import interview.models.Order
import interview.models.OrderPosition
import interview.models.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Test
import java.sql.SQLException

class OrderProcessorTest {

    private val fulfillmentProviderClient: FulfillmentProviderClient = mockk()
    private val orderService: OrderService = mockk()
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default

    private val orderProcessor = OrderProcessor(fulfillmentProviderClient, orderService, coroutineDispatcher)

    private val positions = listOf(
        OrderPosition(positionId = 1, articleId = "article-1", amount = 1),
        OrderPosition(positionId = 2, articleId = "article-2", amount = 2)
    )
    private val firstOrderId = 123
    private val secondOrderId = 456
    private val firstOrder = Order(id = firstOrderId, positions = positions, status = OrderStatus.PAID)
    private val secondOrder = firstOrder.copy(id = secondOrderId, positions = positions.map { it.copy(it.positionId!! + 2) })

    @Test
    fun `SHOULD successfully process all PAID orders`() {
        every { orderService.findAll(OrderStatus.PAID) } returns listOf(firstOrder, secondOrder).right()
        every { orderService.updateOrderStatus(firstOrder.id, OrderStatus.IN_FULFILLMENT) } returns Unit.right()
        every { orderService.updateOrderStatus(secondOrder.id, OrderStatus.IN_FULFILLMENT) } returns Unit.right()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId) } returns true.right()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(secondOrderId) } returns true.right()

        orderProcessor.processPaidOrders().shouldBeRight()

        coVerify {
            orderService.updateOrderStatus(firstOrder.id, OrderStatus.IN_FULFILLMENT)
            orderService.updateOrderStatus(secondOrder.id, OrderStatus.IN_FULFILLMENT)
            fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId)
            fulfillmentProviderClient.sendFulfillmentRequest(secondOrderId)
        }
    }

    @Test
    fun `SHOULD return persistence error WHEN order service returns persistence error`() {
        val error = PersistenceError("Test error", SQLException("Test SQL exception"))
        every { orderService.findAll(OrderStatus.PAID) } returns error.left()

        orderProcessor.processPaidOrders().shouldBeLeft(listOf(error))

        verify {
            fulfillmentProviderClient wasNot Called
        }
    }

    @Test
    fun `SHOULD successfully process only PAID orders with non-null ID`() {
        every { orderService.findAll(OrderStatus.PAID) } returns listOf(firstOrder, secondOrder.copy(id = null)).right()
        every { orderService.updateOrderStatus(firstOrder.id, OrderStatus.IN_FULFILLMENT) } returns Unit.right()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId) } returns true.right()

        orderProcessor.processPaidOrders().shouldBeRight()

        coVerify(exactly = 1) {
            orderService.updateOrderStatus(firstOrder.id, OrderStatus.IN_FULFILLMENT)
            fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId)
        }
    }

    @Test
    fun `SHOULD return fulfillment error for one of orders and process another WHEN fulfillment client returns an error only for one order`() {
        val error = FulfillmentError("Error during sending order with ID ${firstOrder.id} for fulfillment", RuntimeException("Test exception"))
        every { orderService.findAll(OrderStatus.PAID) } returns listOf(firstOrder, secondOrder).right()
        every { orderService.updateOrderStatus(secondOrder.id, OrderStatus.IN_FULFILLMENT) } returns Unit.right()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId) } returns error.left()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(secondOrderId) } returns true.right()

        orderProcessor.processPaidOrders().shouldBeLeft(listOf(error))

        coVerify {
            orderService.updateOrderStatus(secondOrder.id, OrderStatus.IN_FULFILLMENT)
            fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId)
            fulfillmentProviderClient.sendFulfillmentRequest(secondOrderId)

            orderService.updateOrderStatus(firstOrder.id, OrderStatus.IN_FULFILLMENT) wasNot Called
        }
    }

    @Test
    fun `SHOULD return fulfillment error all orders WHEN fulfillment client returns errors for all orders`() {
        val firstError = FulfillmentError("Error during sending order with ID ${firstOrder.id} for fulfillment", RuntimeException("Test exception"))
        val secondError = FulfillmentError("Error during sending order with ID ${secondOrder.id} for fulfillment", RuntimeException("Test exception"))
        every { orderService.findAll(OrderStatus.PAID) } returns listOf(firstOrder, secondOrder).right()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId) } returns firstError.left()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(secondOrderId) } returns secondError.left()

        orderProcessor.processPaidOrders().shouldBeLeft(listOf(firstError, secondError))

        coVerify {
            fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId)
            fulfillmentProviderClient.sendFulfillmentRequest(secondOrderId)

            orderService.updateOrderStatus(firstOrder.id, OrderStatus.IN_FULFILLMENT) wasNot Called
            orderService.updateOrderStatus(secondOrder.id, OrderStatus.IN_FULFILLMENT) wasNot Called
        }
    }

    @Test
    fun `SHOULD return order status update error for one of orders and process another WHEN order service returns an error only for one order`() {
        val error = PersistenceError("Error during status update", SQLException("Test SQL exception"))
        every { orderService.findAll(OrderStatus.PAID) } returns listOf(firstOrder, secondOrder).right()
        every { orderService.updateOrderStatus(firstOrder.id, OrderStatus.IN_FULFILLMENT) } returns Unit.right()
        every { orderService.updateOrderStatus(secondOrder.id, OrderStatus.IN_FULFILLMENT) } returns error.left()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId) } returns true.right()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(secondOrderId) } returns true.right()

        orderProcessor.processPaidOrders().shouldBeLeft(listOf(error))

        coVerify {
            orderService.updateOrderStatus(firstOrder.id, OrderStatus.IN_FULFILLMENT)
            orderService.updateOrderStatus(secondOrder.id, OrderStatus.IN_FULFILLMENT)
            fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId)
            fulfillmentProviderClient.sendFulfillmentRequest(secondOrderId)
        }
    }

    @Test
    fun `SHOULD return order status update errors for all orders WHEN order service returns errors for all orders`() {
        val error = PersistenceError("Error during status update", SQLException("Test SQL exception"))
        every { orderService.findAll(OrderStatus.PAID) } returns listOf(firstOrder, secondOrder).right()
        every { orderService.updateOrderStatus(firstOrder.id, OrderStatus.IN_FULFILLMENT) } returns error.left()
        every { orderService.updateOrderStatus(secondOrder.id, OrderStatus.IN_FULFILLMENT) } returns error.left()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId) } returns true.right()
        coEvery { fulfillmentProviderClient.sendFulfillmentRequest(secondOrderId) } returns true.right()

        orderProcessor.processPaidOrders().shouldBeLeft(listOf(error, error))

        coVerify {
            orderService.updateOrderStatus(firstOrder.id, OrderStatus.IN_FULFILLMENT)
            orderService.updateOrderStatus(secondOrder.id, OrderStatus.IN_FULFILLMENT)
            fulfillmentProviderClient.sendFulfillmentRequest(firstOrderId)
            fulfillmentProviderClient.sendFulfillmentRequest(secondOrderId)
        }
    }
}
