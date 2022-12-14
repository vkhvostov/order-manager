package interview

import interview.configuration.Configuration
import interview.configuration.ProductionConfiguration
import interview.configuration.ProductionConfiguration.orderProcessor
import interview.configuration.ProductionConfiguration.orderService
import interview.plugins.configureRouting
import interview.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.main() {
    val appConfig = environment.config.config("ktor.properties")

    initialization(appConfig, ProductionConfiguration)
    module()
    scheduleJobs(appConfig)
}

fun Application.module() {
    configureRouting(orderService)
    configureSerialization()
}

fun Application.initialization(appConfig: ApplicationConfig, configuration: Configuration) {
    configuration.initialize(appConfig)
}

fun Application.scheduleJobs(appConfig: ApplicationConfig) {
    val logger = LoggerFactory.getLogger(javaClass)

    val orderProcessingInitialDelay = Duration.parse(appConfig.property("order-processing.initial-delay").getString())
    val orderProcessingFixedDelay = Duration.parse(appConfig.property("order-processing.fixed-delay").getString())

    fun triggerPaidOrdersProcessing() {
        logger.info("Starting processing of PAID orders")
        val processingResult = orderProcessor.processPaidOrders()
        processingResult.tapLeft {
            logger.warn("Following errors occurred during processing PAID orders\n ${it.joinToString("\n")}")
        }
        logger.info("Processing of PAID order is finished")
    }

    launch {
        while (true) {
            delay(orderProcessingInitialDelay)
            triggerPaidOrdersProcessing()
            delay(orderProcessingFixedDelay)
        }
    }
}
