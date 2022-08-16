package interview.models

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

class FulfillmentConfirmationTest {

    @Test
    fun `SHOULD deserialize correctly fulfillment confirmation`() {
        val orderId = 123

        val json = /*language=json*/ """
            {
              "orderId": $orderId
            }
        """.trimIndent()

        val expected = FulfillmentConfirmation(orderId)
        val actual = Json.decodeFromString<FulfillmentConfirmation>(json)

        assertEquals(expected, actual)
        assertEquals(expected.orderId, actual.orderId)
    }

    @Test
    fun `SHOULD serialize correctly fulfillment confirmation`() {
        val orderId = 123
        val fulfillmentConfirmation = FulfillmentConfirmation(orderId)

        val expected = /*language=json*/ """
            {
              "orderId": $orderId
            }
        """.trimIndent()

        val actual = Json.encodeToString(fulfillmentConfirmation)

        JSONAssert.assertEquals(expected, actual, true)
    }
}
