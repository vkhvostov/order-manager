package interview.persistence

import interview.models.Order
import interview.models.OrderPosition
import interview.models.OrderStatus

object OrderRepository {

    private val dataSource = PostgresDataSource.dataSource

    fun find(orderId: Int): Order? {
        val connection = dataSource.connection

        val query = connection.prepareStatement("""
            SELECT o.id as order_id, o.status as status, p.id as position_id, p.article_id as article_id, p.amount as amount 
            FROM orders o 
                JOIN order_to_position otp on o.id = otp.order_id
                JOIN positions p on p.id = otp.position_id
            WHERE o.id = $orderId;
        """.trimIndent())
        val result = query.executeQuery()

        // TODO: Implement the case when no order is found
        val rows = mutableListOf<OrderPositionRowProjection>()

        while (result.next()) {
            // TODO: handle invalid statuses
            val status = result.getString("status")
            val positionId = result.getInt("position_id")
            val articleId = result.getString("article_id")
            val articleAmount = result.getInt("amount")

            rows.add(OrderPositionRowProjection(orderId, OrderStatus.valueOf(status), positionId, articleId, articleAmount))
        }

        return toOrders(rows).first()
    }

    fun findAll(): List<Order> {
        val connection = dataSource.connection

        val query = connection.prepareStatement("""
            SELECT o.id as order_id, o.status as status, p.id as position_id, p.article_id as article_id, p.amount as amount 
            FROM orders o 
                JOIN order_to_position otp on o.id = otp.order_id
                JOIN positions p on p.id = otp.position_id;
        """.trimIndent())

        val result = query.executeQuery()
        val rows = mutableListOf<OrderPositionRowProjection>()

        while (result.next()) {
            val orderId = result.getInt("order_id")
            // TODO: handle invalid statuses
            val status = result.getString("status")
            val positionId = result.getInt("position_id")
            val articleId = result.getString("article_id")
            val articleAmount = result.getInt("amount")

            rows.add(OrderPositionRowProjection(orderId, OrderStatus.valueOf(status), positionId, articleId, articleAmount))
        }

        return toOrders(rows)
    }

    fun save(order: Order): Int {
        val connection = dataSource.connection

        val orderQuery = connection.prepareStatement(
            "INSERT INTO orders (status) VALUES ('${order.status}') RETURNING id;"
        )
        val orderId = orderQuery.executeQuery()
        orderId.next()

        order.positions.forEach { position ->
            val positionQuery = connection.prepareStatement(
                "INSERT INTO positions (article_id, amount) VALUES ('${position.articleId}', ${position.amount}) RETURNING id;"
            )
            val positionId = positionQuery.executeQuery()
            positionId.next()

            val orderToPositionQuery = connection.prepareStatement(
                "INSERT INTO order_to_position (order_id, position_id) VALUES (${orderId.getInt("id")}, ${positionId.getInt("id")});"
            )

            orderToPositionQuery.executeUpdate()
        }

        // TODO: Finish
        return 0
    }

    fun updateStatus(orderId: Int, status: OrderStatus) {
        val connection = dataSource.connection

        val query = connection.prepareStatement("UPDATE orders SET status = '$status' where id = $orderId;")
        query.executeUpdate()
    }

//    fun delete(orderId: String): Boolean {
//        val connection = dataSource.connection
//
//        val query = connection.prepareStatement(
//            "DELETE FROM orders WHERE id = $orderId"
//        )
//
//        val result = query.executeUpdate()
//
//        println(result)
//
//        // TODO: Finish
//        return true
//    }

    private fun toOrders(rowProjections: List<OrderPositionRowProjection>): List<Order> =
        rowProjections.groupBy { it.orderId to it.orderStatus }
            .map { orderProjection ->
                Order(
                    id = orderProjection.key.first,
                    positions = orderProjection.value.map { positionRow ->
                        OrderPosition(
                            positionId = positionRow.positionId,
                            articleId = positionRow.articleId,
                            amount = positionRow.articleAmount
                        )
                    },
                    status = orderProjection.key.second
                )
            }
}

data class OrderPositionRowProjection(val orderId: Int, val orderStatus: OrderStatus, val positionId: Int, val articleId: String, val articleAmount: Int)
