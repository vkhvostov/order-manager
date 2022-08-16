package interview.persistence

import arrow.core.getOrElse
import interview.configuration.Configuration
import interview.models.Order
import interview.models.OrderPosition
import interview.models.OrderStatus
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
    fun setUp()  = testApplication {
        environment {
            config = ApplicationConfig("application.conf")
            Configuration.initialize(config.config("ktor.properties"))
            orderRepository = Configuration.orderRepository
            orderRepository.removeAll()
        }
    }

    @Test
    fun `SHOULD find an order in the database WHEN it exists`() {
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
            actualOrder.positions.forEachIndexed { j, position ->
                assertEquals(expected[i].positions[j].articleId, position.articleId)
                assertEquals(expected[i].positions[j].amount, position.amount)
                assertNotNull(position.positionId)
            }
        }
    }
}
