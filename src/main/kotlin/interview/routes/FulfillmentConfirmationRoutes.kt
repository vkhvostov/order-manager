package interview.routes

import arrow.core.Either
import arrow.core.flatMap
import interview.ValidationError
import interview.models.FulfillmentConfirmation
import interview.models.OrderStatus.CLOSED
import interview.respond
import interview.services.OrderService
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.fulfillmentConfirmationRouting(orderService: OrderService) {
    route("/fulfillment-confirmation") {
        put {
            val fulfillmentConfirmation = Either.catch { call.receive<FulfillmentConfirmation>() }
                .mapLeft { ValidationError("Invalid input: ${it.cause}") }
            fulfillmentConfirmation.map { it.orderId }
                .flatMap { orderService.updateOrderStatus(it, CLOSED) }
                .respond(OK)
        }
    }
}
