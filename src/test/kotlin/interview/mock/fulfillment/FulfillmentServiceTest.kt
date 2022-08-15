package interview.mock.fulfillment

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FulfillmentServiceTest {

    private val randomProvider: RandomProvider = mockk()
    private val fulfillmentService = FulfillmentService(randomProvider)

    @Test
    fun `SHOULD accept a fulfillment request WHEN random provider returns true`() {
        every { randomProvider.nextBoolean() } returns true

        assertTrue(fulfillmentService.isFulfillmentRequestAccepted())
    }

    @Test
    fun `SHOULD not accept a fulfillment request WHEN random provider returns false`() {
        every { randomProvider.nextBoolean() } returns false

        assertFalse(fulfillmentService.isFulfillmentRequestAccepted())
    }
}
