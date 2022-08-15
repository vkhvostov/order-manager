package interview.mock.fulfillment

import org.slf4j.LoggerFactory

class FulfillmentService(
    private val randomProvider: RandomProvider,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun isFulfillmentRequestAccepted(): Boolean {
        val randomValue = randomProvider.nextBoolean()
        logger.debug("Order fulfillment started ${if (randomValue) "successfully" else "unsuccessfully"}")
        return randomValue
    }
}
