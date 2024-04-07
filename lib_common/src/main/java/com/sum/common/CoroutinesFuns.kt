package com.sum.common

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.Continuation

suspend inline fun <T> suspendCoroutineWithTimeout(
    timeMills: Long,
    crossinline block: (Continuation<T>) -> Unit
): T? {
    return withTimeoutOrNull(timeMills) {
        suspendCancellableCoroutine(block)
    }
}