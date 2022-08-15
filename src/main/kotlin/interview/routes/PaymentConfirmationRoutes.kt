package interview.routes

import interview.Context.orderService
import interview.models.OrderStatus.PAID
import interview.models.PaymentConfirmation
import interview.respond
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.paymentConfirmationRouting() {
    route("/payment-confirmation") {
        put {
            val paymentConfirmation = call.receive<PaymentConfirmation>()
            val orderId = paymentConfirmation.orderId
            orderService.updateOrderStatus(orderId, PAID).respond(OK)
        }
    }
}
