package interview.mock.fulfillment

import interview.models.FulfillmentRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlin.random.Random

fun Route.fulfillmentProviderRouting() {
    route("/fulfillment-request") {
        post {
            call.receive<FulfillmentRequest>()

            if (isSuccessfulFulfillment()) {
                println("WARN ######################### Successful fulfillment")
                call.respond(HttpStatusCode.Accepted)
            } else {
                println("WARN ######################### Unsuccessful fulfillment")
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
    }
}

private fun isSuccessfulFulfillment(): Boolean {
    val randomValue = Random.nextInt(0, 10)
    println("WARN ######################### Random value $randomValue")
    return true
//    return randomValue > 5
}
