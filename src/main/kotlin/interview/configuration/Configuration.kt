package interview.configuration

import com.zaxxer.hikari.HikariDataSource
import interview.clients.FulfillmentProviderClient
import interview.mock.fulfillment.FulfillmentService
import interview.mock.fulfillment.RandomProvider
import interview.persistence.OrderRepository
import interview.services.OrderProcessor
import interview.services.OrderService
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.asCoroutineDispatcher
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.time.Duration

interface Configuration {

    val orderRepository: OrderRepository
    val orderService: OrderService
    val orderProcessor: OrderProcessor
    val fulfillmentService: FulfillmentService
    fun initialize(appConfig: ApplicationConfig)
}

object ProductionConfiguration : Configuration {
    private val logger = LoggerFactory.getLogger(javaClass)

    override lateinit var orderRepository: OrderRepository
    override lateinit var orderService: OrderService
    override lateinit var orderProcessor: OrderProcessor
    override lateinit var fulfillmentService: FulfillmentService

    override fun initialize(appConfig: ApplicationConfig) {
        fun property(propertyPath: String): String = appConfig.property(propertyPath).getString()

        logger.info("Starting initialization")

        try {
            val dataSource = createDataSource(
                property("database.jdbc-url"),
                property("database.driver-class-name"),
                property("database.username"),
                property("database.password"),
            )

            val httpTimeout: Long = Duration.parse(property("http-request.timeout")).inWholeMilliseconds
            val httpRetries = property("http-request.retries").toInt()
            val httpThreadCount = property("http-request.thread-count").toInt()
            val fulfillmentProviderBaseUrl = property("fulfillment-provider.base-url")
            val orderProcessingThreadPoolSize = property("order-processing.thread-pool-size").toInt()
            val orderProcessingMaxOrderSize = property("order-processing.max-order-size").toInt()
            val orderProcessingCoroutineDispatcher =
                Executors.newFixedThreadPool(orderProcessingThreadPoolSize).asCoroutineDispatcher()

            orderRepository = OrderRepository(dataSource)
            val fulfillmentProviderClient =
                FulfillmentProviderClient(httpTimeout, httpRetries, fulfillmentProviderBaseUrl, httpThreadCount)
            orderService = OrderService(orderRepository, orderProcessingMaxOrderSize)

            orderProcessor = OrderProcessor(fulfillmentProviderClient, orderService, orderProcessingCoroutineDispatcher)

            val randomProvider = RandomProvider()
            fulfillmentService = FulfillmentService(randomProvider)
        } catch (e: Exception) {
            logger.error("Initialization has failed", e)
        }

        logger.info("Initialization is finished")
    }

    private fun createDataSource(jdbcUrl: String, driverClassName: String, username: String, password: String): HikariDataSource {
        logger.info("Initializing the database datasource")

        val dataSource = HikariDataSource()

        dataSource.jdbcUrl = jdbcUrl
        dataSource.driverClassName = driverClassName
        dataSource.username = username
        dataSource.password = password

        logger.info("Datasource max pool size: ${dataSource.maximumPoolSize}")

        return dataSource
    }
}
