package interview.services

import interview.clients.FulfillmentProviderClient
import interview.models.Order
import interview.models.OrderStatus
import interview.persistence.OrderRepository
import interview.routes.OrderCreationRequest

// TODO: naming: maybe order processor?
object OrderService {

    // TODO: validate orderID
    fun find(orderId: String): Order? {
        return OrderRepository.find(orderId.toInt())
    }

    fun findAll(): List<Order> {
        return OrderRepository.findAll()
    }

    fun create(orderCreationRequest: OrderCreationRequest) {
        val order = Order(positions = orderCreationRequest.positions, status = OrderStatus.CREATED)
        OrderRepository.save(order)
    }

    // TODO: proper return value
    // TODO: validate orderID
    fun updateOrderStatus(orderId: String, orderStatus: OrderStatus) {
        updateOrderStatus(orderId.toInt(), orderStatus)
    }

    fun updateOrderStatus(orderId: Int?, orderStatus: OrderStatus) {
        if (orderId != null) {
            OrderRepository.updateStatus(orderId, orderStatus)
        }
    }

    fun processOrder(orderId: String) {
        FulfillmentProviderClient.sendFulfillmentRequest(orderId.toInt())
    }
}
