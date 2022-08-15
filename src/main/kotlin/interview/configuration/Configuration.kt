package interview.configuration

import com.zaxxer.hikari.HikariDataSource
import interview.clients.FulfillmentProviderClient
import interview.mock.fulfillment.FulfillmentService
import interview.mock.fulfillment.RandomProvider
import interview.persistence.OrderRepository
import interview.services.OrderProcessor
import interview.services.OrderService
import io.ktor.server.config.ApplicationConfig
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import org.slf4j.LoggerFactory
import kotlin.time.Duration

object Configuration {
    private val logger = LoggerFactory.getLogger(javaClass)

    lateinit var orderService: OrderService
    lateinit var orderProcessor: OrderProcessor
    lateinit var fulfillmentService: FulfillmentService

    // TODO: what to do at initialization exception?
    fun initialize(appConfig: ApplicationConfig) {
        fun property(propertyPath: String): String = appConfig.property(propertyPath).getString()

        logger.info("Starting initialization")

        val dataSource = createDataSource(
            property("database.jdbc-url"),
            property("database.driver-class-name"),
            property("database.username"),
            property("database.password"),
        )

        val httpTimeout: Long = Duration.parse(property("http-request.timeout")).inWholeMilliseconds
        val httpRetries = property("http-request.retries").toInt()
        val fulfillmentProviderBaseUrl = property("fulfillment-provider.base-url")
        val orderProcessingThreadPoolSize = property("order-processing.thread-pool-size").toInt()
        val orderProcessingCoroutineDispatcher = Executors.newFixedThreadPool(orderProcessingThreadPoolSize).asCoroutineDispatcher()

        val orderRepository = OrderRepository(dataSource)
        val fulfillmentProviderClient = FulfillmentProviderClient(httpTimeout, httpRetries, fulfillmentProviderBaseUrl)
        orderService = OrderService(orderRepository)

        orderProcessor = OrderProcessor(fulfillmentProviderClient, orderService, orderProcessingCoroutineDispatcher)

        val randomProvider = RandomProvider()
        fulfillmentService = FulfillmentService(randomProvider)

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
