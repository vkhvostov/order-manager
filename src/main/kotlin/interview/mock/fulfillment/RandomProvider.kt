package interview.mock.fulfillment

import kotlin.random.Random

open class RandomProvider {
    fun nextBoolean(): Boolean = Random.nextBoolean()
}
