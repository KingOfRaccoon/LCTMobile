package ru.skittens.shared.network.workflow

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.errors.*
import ru.skittens.shared.model.workflow.*

/**
 * Реализация WorkflowApiClient с использованием Ktor HTTP Client
 */
class KtorWorkflowApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://localhost:8000"
) : WorkflowApiClient {

    override suspend fun saveWorkflow(request: SaveWorkflowRequest): Result<SaveWorkflowResponse> {
        return safeApiCall {
            httpClient.post("$baseUrl/workflow/save") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<SaveWorkflowResponse>()
        }
    }

    override suspend fun executeWorkflow(request: WorkflowExecutionRequest): Result<WorkflowExecutionResponse> {
        return safeApiCall {
            httpClient.post("$baseUrl/client/workflow") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<WorkflowExecutionResponse>()
        }
    }

    override suspend fun healthCheck(): Result<HealthCheckResponse> {
        return safeApiCall {
            httpClient.get("$baseUrl/healthcheck")
                .body<HealthCheckResponse>()
        }
    }

    /**
     * Обертка для безопасного выполнения API запросов с обработкой ошибок
     */
    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: IOException) {
            Result.failure(
                WorkflowError.NetworkError("Network error: ${e.message ?: "Unknown network error"}")
            )
        } catch (e: Exception) {
            when {
                e.message?.contains("400") == true -> {
                    Result.failure(
                        WorkflowError.ValidationError("Validation error: ${e.message}")
                    )
                }
                e.message?.contains("500") == true -> {
                    Result.failure(
                        WorkflowError.ServerError(500, "Server error: ${e.message}")
                    )
                }
                else -> {
                    Result.failure(
                        WorkflowError.UnknownError("Unknown error: ${e.message ?: "No details"}")
                    )
                }
            }
        }
    }
}
