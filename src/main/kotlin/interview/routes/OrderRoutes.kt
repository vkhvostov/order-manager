package interview.routes

import interview.models.*
import interview.respond
import interview.services.OrderService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Route.orderRouting() {
    route("/order") {
        get {
            val orders = OrderService.findAll()
            if (orders.isNotEmpty()) {
                orders.respond(HttpStatusCode.OK)
            } else {
                call.respondText("No orders found", status = HttpStatusCode.OK)
            }
        }
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respondText(text = "Missing id", status = HttpStatusCode.BadRequest)
            val order = OrderService.find(id)
            order.tap { if (it.isEmpty()) return@get call.respondText(text = "No order with id $id", status = HttpStatusCode.NotFound) }
            order.respond(HttpStatusCode.OK)
        }
        put {
            val order = call.receive<OrderCreationRequest>()
            val orderId = OrderService.create(order)
            orderId.respond(HttpStatusCode.Created)
        }
    }
}

@Serializable
data class OrderCreationRequest(val positions: List<OrderPosition>)
