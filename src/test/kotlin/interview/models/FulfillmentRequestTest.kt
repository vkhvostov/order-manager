package interview.models

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

class FulfillmentRequestTest {

    @Test
    fun `SHOULD deserialize correctly fulfillment request`() {
        val orderId = 123

        val json = /*language=json*/ """
            {
              "orderId": $orderId
            }
        """.trimIndent()

        val expected = FulfillmentRequest(orderId)
        val actual = Json.decodeFromString<FulfillmentRequest>(json)

        assertEquals(expected, actual)
    }

    @Test
    fun `SHOULD serialize correctly fulfillment request`() {
        val orderId = 123
        val fulfillmentRequest = FulfillmentRequest(orderId)

        val expected = /*language=json*/ """
            {
              "orderId": $orderId
            }
        """.trimIndent()

        val actual = Json.encodeToString(fulfillmentRequest)

        JSONAssert.assertEquals(expected, actual, true)
    }
}
