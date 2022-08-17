package interview.persistence

import arrow.core.getOrElse
import com.zaxxer.hikari.HikariDataSource
import interview.PersistenceError
import interview.configuration.ProductionConfiguration
import interview.models.Order
import interview.models.OrderPosition
import interview.models.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.SQLException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OrderRepositoryTest {

    private lateinit var orderRepository: OrderRepository

    private val positions = listOf(
        OrderPosition(articleId = "article-1", amount = 1),
        OrderPosition(articleId = "article-2", amount = 2)
    )
    private val order = Order(positions = positions, status = OrderStatus.CREATED)

    @BeforeEach
    fun setUp() = testApplication {
        environment {
            config = ApplicationConfig("application.conf")
            ProductionConfiguration.initialize(config.config("ktor.properties"))
            orderRepository = ProductionConfiguration.orderRepository
            orderRepository.removeAll()
        }
    }

    @Test
    fun `SHOULD find an order in the database WHEN it was stored`() {
        val orderId = orderRepository.save(order).getOrElse { -1 }

        val actual = orderRepository.find(orderId).shouldBeRight().shouldBeSome()
        assertEquals(order.status, actual.status)
        assertNotNull(actual.id)
        actual.positions.forEachIndexed { index, position ->
            assertEquals(positions[index].articleId, position.articleId)
            assertEquals(positions[index].amount, position.amount)
            assertNotNull(position.positionId)
        }
    }

    @Test
    fun `SHOULD not find an order in the database WHEN it doesn't exist`() {
        orderRepository.find(-1).shouldBeRight().shouldBeNone()
    }

    @Test
    fun `SHOULD find all orders in the database WHEN it exists`() {
        orderRepository.save(order).getOrElse { -1 }
        val secondOrder = order.copy(positions = positions.map { it.copy(articleId = it.articleId + "-2") }, status = OrderStatus.PAID)
        orderRepository.save(secondOrder).getOrElse { -1 }
        val expected = listOf(order, secondOrder)

        val actual = orderRepository.findAll().shouldBeRight()
        actual.forEachIndexed { i, actualOrder ->
            assertEquals(expected[i].status, actualOrder.status)
            assertNotNull(actualOrder.id)
        }
    }

    @Test
    fun `SHOULD find all orders with particular status in the database WHEN status is provided`() {
        orderRepository.save(order).getOrElse { -1 }
        val secondOrder = order.copy(positions = positions.map { it.copy(articleId = it.articleId + "-2") }, status = OrderStatus.PAID)
        orderRepository.save(secondOrder).getOrElse { -1 }

        val actual = orderRepository.findAll(OrderStatus.PAID).shouldBeRight()
        assertEquals(1, actual.size)
        assertEquals(secondOrder.status, actual[0].status)
        assertNotNull(actual[0].id)
        actual[0].positions.forEachIndexed { index, position ->
            assertEquals(secondOrder.positions[index].articleId, position.articleId)
            assertEquals(secondOrder.positions[index].amount, position.amount)
            assertNotNull(position.positionId)
        }
    }

    @Test
    fun `SHOULD find no orders in the database WHEN no order exists`() {
        orderRepository.findAll().shouldBeRight(emptyList())
    }

    @Test
    fun `SHOULD successfully update the order status`() {
        val orderId = orderRepository.save(order).getOrElse { -1 }

        val storedOrder = orderRepository.find(orderId).shouldBeRight().shouldBeSome()
        assertEquals(order.status, storedOrder.status)

        val newStatus = OrderStatus.PAID
        orderRepository.updateStatus(orderId, newStatus)

        val updatedOrder = orderRepository.find(orderId).shouldBeRight().shouldBeSome()
        assertEquals(newStatus, updatedOrder.status)
    }

    @Test
    fun `SHOULD return an error WHEN searching for an order and pool connection throws an exception`() {
        val dataSource: HikariDataSource = mockk()
        orderRepository = OrderRepository(dataSource)

        val exception = SQLException("Test SQL exception")
        val error = PersistenceError("Error during searching for an order 1 in the database", exception)
        every { dataSource.connection } throws exception

        orderRepository.find(1).shouldBeLeft(error)
    }

    @Test
    fun `SHOULD return an error WHEN searching for all orders and pool connection throws an exception`() {
        val dataSource: HikariDataSource = mockk()
        orderRepository = OrderRepository(dataSource)

        val exception = SQLException("Test SQL exception")
        val error = PersistenceError("Error during searching for all orders in the database", exception)
        every { dataSource.connection } throws exception

        orderRepository.findAll().shouldBeLeft(error)
    }

    @Test
    fun `SHOULD return an error WHEN storing an orders and pool connection throws an exception`() {
        val dataSource: HikariDataSource = mockk()
        orderRepository = OrderRepository(dataSource)

        val exception = SQLException("Test SQL exception")
        val error = PersistenceError("Error during saving order $order to the database", exception)
        every { dataSource.connection } throws exception

        orderRepository.save(order).shouldBeLeft(error)
    }

    @Test
    fun `SHOULD return an error WHEN updating the order status and pool connection throws an exception`() {
        val dataSource: HikariDataSource = mockk()
        orderRepository = OrderRepository(dataSource)

        val exception = SQLException("Test SQL exception")
        val error = PersistenceError("Error during updating order 1 with status PAID", exception)
        every { dataSource.connection } throws exception

        orderRepository.updateStatus(1, OrderStatus.PAID).shouldBeLeft(error)
    }

    @Test
    fun `SHOULD return an error WHEN deleting all orders and pool connection throws an exception`() {
        val dataSource: HikariDataSource = mockk()
        orderRepository = OrderRepository(dataSource)

        val exception = SQLException("Test SQL exception")
        val error = PersistenceError("Error during deleting orders", exception)
        every { dataSource.connection } throws exception

        orderRepository.removeAll().shouldBeLeft(error)
    }
}
