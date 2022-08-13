package interview.routes

import interview.models.OrderStatus
import interview.models.PaymentConfirmation
import interview.services.OrderService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.paymentConfirmationRouting() {
    route("/payment-confirmation") {
        put {
            val paymentConfirmation = call.receive<PaymentConfirmation>()
            val orderId = paymentConfirmation.orderId
            OrderService.updateOrderStatus(orderId, OrderStatus.PAID)
            call.respond(HttpStatusCode.NoContent)
            OrderService.processOrder(orderId)
        }
    }
}
