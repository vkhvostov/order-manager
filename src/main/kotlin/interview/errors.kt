package interview

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext

sealed interface OrderManagementError {
    val description: String
}

data class PersistenceError(override val description: String, val error: Throwable) : OrderManagementError

data class ValidationError(override val description: String) : OrderManagementError

data class FulfillmentError(override val description: String, val error: Throwable) : OrderManagementError

context(PipelineContext<Unit, ApplicationCall>)

suspend inline fun <reified A : Any> Either<OrderManagementError, A>.respond(status: HttpStatusCode): Unit =
    when (this) {
        is Either.Left -> respond(value)
        is Either.Right -> call.respond(status, value)
    }

suspend fun PipelineContext<Unit, ApplicationCall>.respond(error: OrderManagementError): Unit =
    when (error) {
        is PersistenceError -> internal(error)
        is ValidationError -> unprocessable(error)
        is FulfillmentError -> failedDependency(error)
    }

private suspend inline fun PipelineContext<Unit, ApplicationCall>.unprocessable(error: OrderManagementError): Unit =
    call.respond(HttpStatusCode.UnprocessableEntity, error.description)

private suspend inline fun PipelineContext<Unit, ApplicationCall>.internal(error: OrderManagementError): Unit =
    call.respond(HttpStatusCode.InternalServerError, error.description)

private suspend inline fun PipelineContext<Unit, ApplicationCall>.failedDependency(error: OrderManagementError): Unit =
    call.respond(HttpStatusCode.FailedDependency, error.description)
