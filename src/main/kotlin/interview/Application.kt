package interview

import interview.plugins.configureRouting
import interview.plugins.configureSerialization
import interview.services.OrderService
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
    val logger = LoggerFactory.getLogger(javaClass)
    val orderService = Context.orderService
    launch {
        while (true) {
            // TODO: consider moving this somewhere else
            logger.debug("Starting processing of PAID orders")
            val processingResult = orderService.processPaidOrders()
            processingResult.tapLeft {
                logger.warn("Following errors occurred during processing PAID orders\n ${it.joinToString("\n")}")
            }
            delay(Duration.parse("PT2M"))
        }
    }
}
