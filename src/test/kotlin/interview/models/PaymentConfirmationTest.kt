package interview.models

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

class PaymentConfirmationTest {

    @Test
    fun `SHOULD deserialize correctly payment confirmation`() {
        val orderId = 123

        val json = /*language=json*/ """
            {
              "orderId": $orderId
            }
        """.trimIndent()

        val expected = PaymentConfirmation(orderId)
        val actual = Json.decodeFromString<PaymentConfirmation>(json)

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `SHOULD serialize correctly payment confirmation`() {
        val orderId = 123
        val paymentConfirmation = PaymentConfirmation(orderId)

        val expected = /*language=json*/ """
            {
              "orderId": $orderId
            }
        """.trimIndent()

        val actual = Json.encodeToString(paymentConfirmation)

        JSONAssert.assertEquals(expected, actual, true)
    }
}
