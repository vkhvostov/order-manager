package interview.routes

import arrow.core.Either
import arrow.core.flatMap
import interview.ValidationError
import interview.models.OrderStatus.PAID
import interview.models.PaymentConfirmation
import interview.respond
import interview.services.OrderService
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.paymentConfirmationRouting(orderService: OrderService) {
    route("/payment-confirmation") {
        put {
            val paymentConfirmation = Either.catch { call.receive<PaymentConfirmation>() }
                .mapLeft { ValidationError("Invalid input: ${it.cause}") }
            paymentConfirmation.map { it.orderId }
                .flatMap { orderService.updateOrderStatus(it, PAID) }
                .respond(OK)
        }
    }
}
