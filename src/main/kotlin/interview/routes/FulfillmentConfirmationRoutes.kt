package interview.routes

import interview.Configuration.orderService
import interview.models.FulfillmentConfirmation
import interview.models.OrderStatus.CLOSED
import interview.respond
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.fulfillmentConfirmationRouting() {
    route("/fulfillment-confirmation") {
        put {
            val paymentConfirmation = call.receive<FulfillmentConfirmation>()
            val orderId = paymentConfirmation.orderId
            orderService.updateOrderStatus(orderId, CLOSED).respond(OK)
        }
    }
}
