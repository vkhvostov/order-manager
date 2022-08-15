package interview.models

import interview.models.OrderStatus.CREATED
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

class OrderTest {

    @Test
    fun `SHOULD deserialize correctly order object with order ID`() {
        val positions = listOf(
            OrderPosition(positionId = 1, articleId = "article-1", amount = 1),
            OrderPosition(positionId = 2, articleId = "article-2", amount = 2)
        )
        val order = Order(id = 123, positions = positions, status = CREATED)

        val json = /*language=json*/ """
            {
              "id": ${order.id},
              "positions": [
                ${positions.joinToString {
                    """
                        {
                          "positionId": ${it.positionId},
                          "articleId": "${it.articleId}",
                          "amount": ${it.amount}
                        }
                    """.trimIndent()
                }}
              ],
              "status": "${order.status}"
            }
        """.trimIndent()

        val actual = Json.decodeFromString<Order>(json)

        Assertions.assertEquals(order, actual)
    }

    @Test
    fun `SHOULD deserialize correctly order object without order ID and position ID`() {
        val positions = listOf(
            OrderPosition(articleId = "article-1", amount = 1),
            OrderPosition(positionId = 2, articleId = "article-2", amount = 2)
        )
        val order = Order(positions = positions, status = CREATED)

        val json = /*language=json*/ """
            {
              "positions": [
                ${positions.joinToString {
            """
                        {
                          ${if (it.positionId != null) "\"positionId\": ${it.positionId}," else ""}
                          "articleId": "${it.articleId}",
                          "amount": ${it.amount}
                        }
                    """.trimIndent()
        }}
              ],
              "status": "${order.status}"
            }
        """.trimIndent()

        val actual = Json.decodeFromString<Order>(json)

        Assertions.assertEquals(order, actual)
    }

    @Test
    fun `SHOULD serialize correctly order object with order ID`() {
        val positions = listOf(
            OrderPosition(positionId = 1, articleId = "article-1", amount = 1),
            OrderPosition(positionId = 2, articleId = "article-2", amount = 2)
        )
        val order = Order(id = 123, positions = positions, status = CREATED)

        val expected = /*language=json*/ """
            {
              "id": ${order.id},
              "positions": [
                ${positions.joinToString {
            """
                        {
                          "positionId": ${it.positionId},
                          "articleId": "${it.articleId}",
                          "amount": ${it.amount}
                        }
                    """.trimIndent()
        }}
              ],
              "status": "${order.status}"
            }
        """.trimIndent()

        val actual = Json.encodeToString(order)

        JSONAssert.assertEquals(expected, actual, true)
    }

    @Test
    fun `SHOULD serialize correctly order object without order ID and position ID`() {
        val positions = listOf(
            OrderPosition(positionId = 1, articleId = "article-1", amount = 1),
            OrderPosition(articleId = "article-2", amount = 2)
        )
        val order = Order(positions = positions, status = CREATED)

        val expected = /*language=json*/ """
            {
              "positions": [
                ${positions.joinToString {
            """
                        {
                          ${if (it.positionId != null) "\"positionId\": ${it.positionId}," else ""}
                          "articleId": "${it.articleId}",
                          "amount": ${it.amount}
                        }
                    """.trimIndent()
        }}
              ],
              "status": "${order.status}"
            }
        """.trimIndent()

        val actual = Json.encodeToString(order)

        JSONAssert.assertEquals(expected, actual, true)
    }
}
