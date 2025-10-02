package ru.skittens.shared.util

import kotlinx.coroutines.delay

/**
 * Утилита для retry логики при сетевых запросах
 */
object RetryUtils {
    /**
     * Выполнить операцию с retry при ошибках
     * 
     * @param times Количество попыток
     * @param initialDelay Начальная задержка между попытками (мс)
     * @param factor Множитель для exponential backoff
     * @param maxDelay Максимальная задержка между попытками (мс)
     * @param block Операция для выполнения
     * @return Result с результатом или ошибкой
     */
    suspend fun <T> retryIO(
        times: Int = 3,
        initialDelay: Long = 1000,
        factor: Double = 2.0,
        maxDelay: Long = 10000,
        block: suspend () -> T
    ): Result<T> {
        var currentDelay = initialDelay
        repeat(times - 1) { attempt ->
            try {
                return Result.success(block())
            } catch (e: Exception) {
                // Exponential backoff
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
        }
        
        // Последняя попытка без задержки
        return try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
