package interview.routes

import interview.configuration.Configuration.orderService
import interview.models.OrderPosition
import interview.respond
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

fun Route.orderRouting() {
    route("/order") {
        get {
            val orders = orderService.findAll()
            orders.respond(OK)
        }
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respondText(text = "Missing id", status = BadRequest) // TODO: Unify them with text vs without text?
            val order = orderService.find(id)
            order.tap { if (it.isEmpty()) return@get call.respondText(text = "No order with id $id", status = NotFound) }
            order.respond(OK)
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
