package interview.persistence

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.some
import com.zaxxer.hikari.HikariDataSource
import interview.PersistenceError
import interview.models.Order
import interview.models.OrderPosition
import interview.models.OrderStatus
import org.slf4j.LoggerFactory
import java.sql.ResultSet

open class OrderRepository(
    private val dataSource: HikariDataSource,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun find(orderId: Int): Either<PersistenceError, Option<Order>> =
        Either.catch { read(orderId) }
            .mapLeft { PersistenceError("Error during searching for an order $orderId in the database", it) }

    fun findAll(status: OrderStatus? = null): Either<PersistenceError, List<Order>> =
        Either.catch { readAll(status) }
            .mapLeft { PersistenceError("Error during searching for all orders in the database", it) }

    fun save(order: Order): Either<PersistenceError, Int> =
        Either.catch { store(order) }
            .mapLeft { PersistenceError("Error during saving order $order to the database", it) }

    fun updateStatus(orderId: Int, status: OrderStatus): Either<PersistenceError, Unit> =
        Either.catch { update(orderId, status) }
            .mapLeft { PersistenceError("Error during updating order $orderId with status $status", it) }

    fun removeAll(): Either<PersistenceError, Unit> =
        Either.catch { deleteAll() }.mapLeft { PersistenceError("Error during deleting orders", it) }

    private fun read(orderId: Int): Option<Order> {
        logger.debug("Searching for an order with ID $orderId")
        val connection = dataSource.connection

        val query = connection.prepareStatement(
            """
            SELECT o.id as order_id, o.status as status, p.id as position_id, p.article_id as article_id, p.amount as amount 
            FROM orders o 
                JOIN order_to_position otp on o.id = otp.order_id
                JOIN positions p on p.id = otp.position_id
            WHERE o.id = $orderId;
            """.trimIndent()
        )

        val result = query.executeQuery()
        val orders = toOrders(result)

        return if (orders.isEmpty()) None else orders.first().some()
    }

    private fun readAll(status: OrderStatus?): List<Order> {
        logger.debug("Searching for all orders")
        val connection = dataSource.connection

        val statusWhereClause = if (status != null) "WHERE o.status = '$status'" else ""

        val query = connection.prepareStatement(
            """
            SELECT o.id as order_id, o.status as status, p.id as position_id, p.article_id as article_id, p.amount as amount 
            FROM orders o 
                JOIN order_to_position otp on o.id = otp.order_id
                JOIN positions p on p.id = otp.position_id 
            $statusWhereClause;
            """.trimIndent()
        )

        val result = query.executeQuery()

        return toOrders(result)
    }

    private fun store(order: Order): Int {
        logger.debug("Storing order $order in the database")
        val connection = dataSource.connection

        val orderQuery = connection.prepareStatement(
            "INSERT INTO orders (status) VALUES ('${order.status}') RETURNING id;"
        )
        val orderQueryResult = orderQuery.executeQuery()
        orderQueryResult.next()
        val orderId = orderQueryResult.getInt("id")

        order.positions.forEach { position ->
            val positionQuery = connection.prepareStatement(
                "INSERT INTO positions (article_id, amount) VALUES ('${position.articleId}', ${position.amount}) RETURNING id;"
            )
            val positionQueryResult = positionQuery.executeQuery()
            positionQueryResult.next()
            val positionId = positionQueryResult.getInt("id")

            val orderToPositionQuery = connection.prepareStatement(
                "INSERT INTO order_to_position (order_id, position_id) VALUES ($orderId, $positionId);"
            )

            orderToPositionQuery.executeUpdate()
        }

        return orderId
    }

    private fun update(orderId: Int, status: OrderStatus) {
        logger.debug("Updating for the order $orderId status to $status")
        val connection = dataSource.connection

        val query = connection.prepareStatement("UPDATE orders SET status = '$status' where id = $orderId;")

        query.executeUpdate()
    }

    private fun deleteAll() {
        logger.debug("Deleting all orders")
        val connection = dataSource.connection

        val query = connection.prepareStatement("DELETE from orders;")

        query.executeUpdate()
    }

    private fun toOrders(result: ResultSet): List<Order> {
        val rows = mutableListOf<OrderPositionRowProjection>()

        while (result.next()) {
            val orderId = result.getInt("order_id")
            val status = result.getString("status")
            val positionId = result.getInt("position_id")
            val articleId = result.getString("article_id")
            val articleAmount = result.getInt("amount")

            val orderStatus = OrderStatus.fromString(status)
            orderStatus.map { rows.add(OrderPositionRowProjection(orderId, it, positionId, articleId, articleAmount)) }
            if (orderStatus.isEmpty()) logger.warn("Ignoring order $orderId due to invalid status $status")
        }

        return toOrders(rows)
    }

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
