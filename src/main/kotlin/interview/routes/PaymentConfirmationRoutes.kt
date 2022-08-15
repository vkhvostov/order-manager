package interview.routes

import interview.configuration.Configuration.orderService
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
            // TODO: maybe catch here to catch parsing exception of type JsonDecodingException
            val paymentConfirmation = call.receive<PaymentConfirmation>()
            val orderId = paymentConfirmation.orderId
            orderService.updateOrderStatus(orderId, PAID).respond(OK)
        }
    }
}
