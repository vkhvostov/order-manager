package interview.routes

import interview.models.*
import interview.service.OrderService
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
                call.respond(orders)
            } else {
                call.respondText("No orders found", status = HttpStatusCode.OK)
            }
        }
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )
            val order =
                OrderService.find(id) ?: return@get call.respondText(
                    "No order with id $id",
                    status = HttpStatusCode.NotFound
                )
            call.respond(order)
        }
        put {
            val order = call.receive<OrderCreationRequest>()
            OrderService.create(order)
            call.respondText("Order stored correctly", status = HttpStatusCode.Created)
        }
//        delete("{id?}") {
//            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
//            if (OrderRepository.delete(id)) {
//                call.respondText("Order removed correctly", status = HttpStatusCode.Accepted)
//            } else {
//                call.respondText("Not Found", status = HttpStatusCode.NotFound)
//            }
//        }
    }
}

@Serializable
data class OrderCreationRequest(val positions: List<OrderPosition>)
