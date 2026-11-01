package com.fanguskdev.bakiface.domain.common

/**
 * The outcome of an operation that can fail in a way the app expects (ADR 0003): a [Success] with its value or a
 * [Failure] with a typed error. Bugs still throw; only the failures a person can meet travel as values.
 */
sealed interface AppResult<out T, out E> {
    data class Success<out T>(val value: T) : AppResult<T, Nothing>

    data class Failure<out E>(val error: E) : AppResult<Nothing, E>
}

/** The value of a [AppResult.Success], transformed; a failure stays as it is. */
inline fun <T, E, R> AppResult<T, E>.map(transform: (T) -> R): AppResult<R, E> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

/** The next step after a success; a failure stops the chain. */
inline fun <T, E, R> AppResult<T, E>.flatMap(transform: (T) -> AppResult<R, E>): AppResult<R, E> = when (this) {
    is AppResult.Success -> transform(value)
    is AppResult.Failure -> this
}

/** The value of a success, or `null` for a failure. */
fun <T, E> AppResult<T, E>.getOrNull(): T? = (this as? AppResult.Success)?.value

/** The error of a failure, or `null` for a success. */
fun <T, E> AppResult<T, E>.errorOrNull(): E? = (this as? AppResult.Failure)?.error
