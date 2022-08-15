package interview.models

import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeSome
import org.junit.jupiter.api.Test

class OrderStatusTest {

    @Test
    fun `SHOULD parse order status from string`() {
        OrderStatus.fromString("CREATED").shouldBeSome(OrderStatus.CREATED)
        OrderStatus.fromString("paid").shouldBeSome(OrderStatus.PAID)
        OrderStatus.fromString("ClOsEd").shouldBeSome(OrderStatus.CLOSED)
        OrderStatus.fromString("INVALID").shouldBeNone()
    }
}
