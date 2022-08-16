//package interview
//
//import interview.configuration.ProductionConfiguration
//import interview.services.OrderService
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import org.slf4j.LoggerFactory
//import kotlin.time.Duration
//
//class JobScheduler(
//    private val orderService: OrderService,
//) {
//    val logger = LoggerFactory.getLogger(javaClass)
//
//    fun foo() {
//        val orderProcessingInitialDelay = Duration.parse(appConfig.property("order-processing.initial-delay").getString())
//        val orderProcessingFixedDelay = Duration.parse(appConfig.property("order-processing.fixed-delay").getString())
//
//        fun triggerPaidOrdersProcessing() {
//            logger.info("Starting processing of PAID orders")
//            val processingResult = ProductionConfiguration.orderProcessor.processPaidOrders()
//            processingResult.tapLeft {
//                logger.warn("Following errors occurred during processing PAID orders\n ${it.joinToString("\n")}")
//            }
//            logger.info("Processing of PAID order is finished")
//        }
//
//        launch {
//            while (true) {
//                delay(orderProcessingInitialDelay)
//                triggerPaidOrdersProcessing()
//                delay(orderProcessingFixedDelay)
//            }
//        }
//    }
//}
