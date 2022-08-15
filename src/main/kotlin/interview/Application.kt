package interview

import interview.Configuration.orderService
import interview.plugins.configureRouting
import interview.plugins.configureSerialization
import io.ktor.server.application.Application
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureRouting()
    configureSerialization()
}

fun Application.main() {
    val appConfig = environment.config.config("ktor.properties")
    val orderProcessingInitialDelay = Duration.parse(appConfig.property("order-service.initial-delay").getString())
    val orderProcessingFixedDelay = Duration.parse(appConfig.property("order-service.fixed-delay").getString())

    Configuration.initialize(appConfig)

    val logger = LoggerFactory.getLogger(javaClass)

    fun triggerPaidOrdersProcessing() {
        logger.debug("Starting processing of PAID orders")
        val processingResult = orderService.processPaidOrders()
        processingResult.tapLeft {
            logger.warn("Following errors occurred during processing PAID orders\n ${it.joinToString("\n")}")
        }
    }

    launch {
        while (true) {
            delay(orderProcessingInitialDelay)
            triggerPaidOrdersProcessing()
            delay(orderProcessingFixedDelay)
        }
    }
}
