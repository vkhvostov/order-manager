package interview.routes

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import interview.PersistenceError
import interview.TestConfiguration.orderService
import interview.ValidationError
import interview.main
import interview.models.Order
import interview.models.OrderPosition
import interview.models.OrderStatus
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.mockk.every
import java.sql.SQLException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import kotlin.test.assertEquals

class OrderRoutesKtTest {

    private val positions = listOf(
        OrderPosition(positionId = 1, articleId = "article-1", amount = 1),
        OrderPosition(positionId = 2, articleId = "article-2", amount = 2)
    )
    private val orderId = 123
    private val order = Order(id = orderId, positions = positions, status = OrderStatus.CREATED)
    private val orderCreationRequest = OrderCreationRequest(positions)

    @Test
    fun `SHOULD return order WHEN when there is an order stored`() = testApplication {
        application {
            main()
            every { orderService.findAll() } returns listOf(order).right()
        }

        val response = client.get("/order")
        assertEquals(HttpStatusCode.OK, response.status)
        JSONAssert.assertEquals(Json.encodeToString(listOf(order)), response.bodyAsText(), true)
    }

    @Test
    fun `SHOULD return order by ID WHEN there is an order stored`() = testApplication {
        application {
            main()
            every { orderService.find(orderId.toString()) } returns order.some().right()
        }

        val response = client.get("/order/$orderId")
        assertEquals(HttpStatusCode.OK, response.status)
        JSONAssert.assertEquals(Json.encodeToString(order), response.bodyAsText(), true)
    }

    @Test
    fun `SHOULD successfully create an order`() = testApplication {
        application {
            main()
            every { orderService.create(orderCreationRequest) } returns orderId.right()
        }

        val response = client.put("/order") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(orderCreationRequest))
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `SHOULD return an internal error WHEN database lookup was unsuccessful`() = testApplication {
        val error = PersistenceError("Test error", SQLException("Test SQL exception"))
        application {
            main()
            every { orderService.findAll() } returns error.left()
        }

        val response = client.get("/order")
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `SHOULD return a bad request WHEN there is no ID parameter`() = testApplication {
        application {
            main()
        }

        val response = client.get("/order/")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `SHOULD return a bad request WHEN ID is not valid`() = testApplication {
        val error = ValidationError("Test error")
        val id = "invalid"
        application {
            main()
            every { orderService.find(id) } returns error.left()
        }

        val response = client.get("/order/$id")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `SHOULD return a not found WHEN there is no order in the database`() = testApplication {
        application {
            main()
            every { orderService.find(orderId.toString()) } returns None.right()
        }

        val response = client.get("/order/$orderId")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
