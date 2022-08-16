package interview.plugins

import interview.mock.fulfillment.fulfillmentProviderRouting
import interview.routes.fulfillmentConfirmationRouting
import interview.routes.orderRouting
import interview.routes.paymentConfirmationRouting
import interview.services.OrderService
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting(orderService: OrderService) {
    routing {
        orderRouting(orderService)
        paymentConfirmationRouting(orderService)
        fulfillmentConfirmationRouting(orderService)

        // mocking fulfillment provider
        fulfillmentProviderRouting()
    }
}
