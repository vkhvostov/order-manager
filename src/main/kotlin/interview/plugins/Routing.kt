package interview.plugins

import interview.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        orderRouting()
        paymentConfirmationRouting()
    }
}
