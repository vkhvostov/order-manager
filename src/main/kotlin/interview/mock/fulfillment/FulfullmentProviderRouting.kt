package interview.mock.fulfillment

import interview.Context.fulfillmentService
import interview.models.FulfillmentRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.fulfillmentProviderRouting() {
    route("/fulfillment-request") {
        post {
            call.receive<FulfillmentRequest>()

            if (fulfillmentService.isSuccessfulFulfillment()) {
                call.respond(HttpStatusCode.Accepted)
            } else {
                call.respond(HttpStatusCode.NotAcceptable)
            }
        }
    }
}
