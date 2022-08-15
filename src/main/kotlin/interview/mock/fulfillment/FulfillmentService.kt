package interview.mock.fulfillment

import kotlin.random.Random

class FulfillmentService {
    fun isSuccessfulFulfillment(): Boolean {
        val randomValue = Random.nextInt(0, 10)
        println("WARN ######################### Random value $randomValue")
        return true
//    return randomValue > 5
    }
}
