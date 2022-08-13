package interview.service

import interview.models.Order
import interview.models.OrderStatus
import interview.persistence.OrderRepository
import interview.routes.OrderCreationRequest

object OrderService {

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
}
