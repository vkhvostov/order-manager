package interview.routes

import arrow.core.None
import arrow.core.Some
import interview.models.OrderPosition
import interview.respond
import interview.services.OrderService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

fun Route.orderRouting(orderService: OrderService) {
    route("/order") {
        get {
            val orders = orderService.findAll()
            orders.respond(OK)
        }
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respond(BadRequest)
            orderService.find(id).map {
                when (it) {
                    is Some -> it.value
                    is None -> return@get call.respond(NotFound)
                }
            }.respond(OK)
        }
        put {
            val order = call.receive<OrderCreationRequest>()
            val orderId = orderService.create(order)
            orderId.respond(Created)
        }
    }
}

@Serializable
data class OrderCreationRequest(val positions: List<OrderPosition>)
