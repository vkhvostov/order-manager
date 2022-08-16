package interview.services

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import interview.PersistenceError
import interview.ValidationError
import interview.models.Order
import interview.models.OrderPosition
import interview.models.OrderStatus
import interview.models.OrderStatus.CREATED
import interview.models.OrderStatus.PAID
import interview.persistence.OrderRepository
import interview.routes.OrderCreationRequest
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.mockk.every
import io.mockk.mockk
import java.sql.SQLException
import org.junit.jupiter.api.Test

class OrderServiceTest {

    private val orderRepository: OrderRepository = mockk()

    private val orderService = OrderService(orderRepository)

    private val positions = listOf(
        OrderPosition(positionId = 1, articleId = "article-1", amount = 1),
        OrderPosition(positionId = 2, articleId = "article-2", amount = 2)
    )
    private val orderId = 123
    private val order = Order(id = orderId, positions = positions, status = CREATED)
    private val orderCreationRequest = OrderCreationRequest(positions)

    @Test
    fun `SHOULD find some order by order ID`() {
        every { orderRepository.find(orderId) } returns order.some().right()

        val actual = orderService.find(order.id.toString())

        actual.shouldBeRight().shouldBeSome(order)
    }

    @Test
    fun `SHOULD find none order by order ID WHEN repository returns none`() {
        every { orderRepository.find(orderId) } returns None.right()

        val actual = orderService.find(order.id.toString())

        actual.shouldBeRight().shouldBeNone()
    }

    @Test
    fun `SHOULD return database error WHEN searching for an order and repository throws an error`() {
        val error = PersistenceError("Test error", SQLException("Test SQL exception"))
        every { orderRepository.find(orderId) } returns error.left()

        val actual = orderService.find(order.id.toString())

        actual.shouldBeLeft(error)
    }

    @Test
    fun `SHOULD return validation error WHEN order ID is not an integer`() {
        val error = ValidationError("Order ID is not an integer")

        val actual = orderService.find("invalid-id")

        actual.shouldBeLeft(error)
    }

    @Test
    fun `SHOULD find all orders`() {
        val orders = listOf(order)
        every { orderRepository.findAll() } returns orders.right()

        val actual = orderService.findAll()

        actual.shouldBeRight(orders)
    }

    @Test
    fun `SHOULD find all CLOSED orders WHEN provided status CLOSED`() {
        val orders = listOf(order.copy(status = OrderStatus.CLOSED))
        every { orderRepository.findAll() } returns orders.right()

        val actual = orderService.findAll()

        actual.shouldBeRight(orders)
    }

    @Test
    fun `SHOULD return empty list WHEN repository returns empty list`() {
        every { orderRepository.findAll() } returns emptyList<Order>().right()

        val actual = orderService.findAll()

        actual.shouldBeRight(emptyList())
    }

    @Test
    fun `SHOULD return database error WHEN searching for all orders and repository throws an error`() {
        val error = PersistenceError("Test error", SQLException("Test SQL exception"))
        every { orderRepository.findAll() } returns error.left()

        val actual = orderService.findAll()

        actual.shouldBeLeft(error)
    }

    @Test
    fun `SHOULD successfully create an order`() {
        every { orderRepository.save(order.copy(id = null, status = CREATED)) } returns orderId.right()

        val actual = orderService.create(orderCreationRequest)

        actual.shouldBeRight(order.id)
    }

    @Test
    fun `SHOULD return database error WHEN creating an order and repository throws an error`() {
        val error = PersistenceError("Test error", SQLException("Test SQL exception"))
        every { orderRepository.save(order.copy(id = null, status = CREATED)) } returns error.left()

        val actual = orderService.create(orderCreationRequest)

        actual.shouldBeLeft(error)
    }

    @Test
    fun `SHOULD successfully update order status`() {
        every { orderRepository.updateStatus(orderId, PAID) } returns Unit.right()

        val actual = orderService.updateOrderStatus(order.id, PAID)

        actual.shouldBeRight()
    }

    @Test
    fun `SHOULD return validation error WHEN updating order status and order ID is null`() {
        val error = ValidationError("Order ID is null")
        val actual = orderService.updateOrderStatus(null, PAID)

        actual.shouldBeLeft(error)
    }
}
