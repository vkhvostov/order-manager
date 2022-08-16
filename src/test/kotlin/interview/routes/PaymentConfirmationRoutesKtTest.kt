package interview.routes

import arrow.core.left
import arrow.core.right
import interview.PersistenceError
import interview.TestConfiguration
import interview.ValidationError
import interview.main
import interview.models.PaymentConfirmation
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.mockk.every
import java.sql.SQLException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PaymentConfirmationRoutesKtTest {

    private val orderId = 1

    @Test
    fun `SHOULD return OK WHEN payment is successfully confirmed`() = testApplication {
        application {
            main()
            every { TestConfiguration.orderService.updateOrderStatus(any(), any()) } returns Unit.right()
        }

        val response = client.put("/payment-confirmation") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(PaymentConfirmation(orderId)))
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `SHOULD return internal error WHEN status update was unsuccessful`() = testApplication {
        val error = PersistenceError("Test error", SQLException("Test SQL exception"))
        application {
            main()
            every { TestConfiguration.orderService.updateOrderStatus(any(), any()) } returns error.left()
        }

        val response = client.put("/payment-confirmation") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(PaymentConfirmation(orderId)))
        }
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `SHOULD return unprocessable error WHEN request doesn't pass validation`() = testApplication {
        val error = ValidationError("Test error")
        application {
            main()
            every { TestConfiguration.orderService.updateOrderStatus(any(), any()) } returns error.left()
        }

        val response = client.put("/payment-confirmation") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(PaymentConfirmation(orderId)))
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `SHOULD return bad request WHEN input is malformed`() = testApplication {
        application {
            main()
        }

        val response = client.put("/payment-confirmation") {
            contentType(ContentType.Application.Json)
            setBody("invalid json")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
