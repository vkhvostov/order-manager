package interview.plugins

import interview.mock.fulfillment.fulfillmentProviderRouting
import interview.routes.fulfillmentConfirmationRouting
import interview.routes.orderRouting
import interview.routes.paymentConfirmationRouting
import io.ktor.server.application.Application
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        orderRouting()
        paymentConfirmationRouting()
        fulfillmentConfirmationRouting()

        // mocking fulfillment provider
        fulfillmentProviderRouting()
    }
}
