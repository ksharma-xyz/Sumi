package xyz.ksharma.sumi.coroutines.ext

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ksharma.sumi.log.logError
import kotlin.coroutines.cancellation.CancellationException

/**
 * Launches a coroutine on [dispatcher] under a [CoroutineExceptionHandler] that
 * routes any uncaught throwable to Crashlytics + logs and runs [errorBlock].
 * The reified [T] is used purely to label the report with the caller's class
 * name (e.g. `launchWithExceptionHandler<ZenViewModel>(...)`), making post-mortem
 * debugging trivially easy in the dashboard.
 *
 * Use this anywhere a `viewModelScope.launch { ... }` does work that could throw
 * (network, file IO, third-party SDKs). Without a handler the throwable
 * propagates to the parent scope and crashes the app.
 *
 * Ported from Krail's `core/coroutines-ext` module — same shape so muscle memory
 * carries across projects.
 */
inline fun <reified T> CoroutineScope.launchWithExceptionHandler(
    dispatcher: CoroutineDispatcher,
    noinline errorBlock: () -> Unit = {},
    crossinline block: suspend CoroutineScope.() -> Unit,
): Job = launch(
    context = dispatcher + coroutineExceptionHandler(
        message = T::class.simpleName,
        errorBlock = errorBlock,
    ),
) {
    block()
}

/** Builds a [CoroutineExceptionHandler] that logs (Crashlytics + console) and invokes [errorBlock]. */
fun coroutineExceptionHandler(
    message: String? = null,
    errorBlock: () -> Unit = {},
): CoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    logError(message = message ?: throwable.message ?: "uncaught coroutine exception", throwable = throwable)
    errorBlock()
}

/**
 * Runs [block] on [dispatcher] and returns a [Result]. Catches all throwables
 * EXCEPT [CancellationException], which must always propagate so structured
 * concurrency keeps working.
 */
@Suppress("TooGenericExceptionCaught")
suspend fun <T, R> T.safeResult(
    dispatcher: CoroutineDispatcher,
    block: T.() -> R,
): Result<R> = withContext(dispatcher) {
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        logError("safeResult failed", e)
        coroutineContext.ensureActive()
        Result.failure(e)
    }
}

/** Suspending counterpart of [safeResult]. Use when [block] needs to suspend. */
@Suppress("TooGenericExceptionCaught")
suspend fun <T, R> T.suspendSafeResult(
    dispatcher: CoroutineDispatcher,
    block: suspend T.() -> R,
): Result<R> = withContext(dispatcher) {
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        logError("suspendSafeResult failed", e)
        coroutineContext.ensureActive()
        Result.failure(e)
    }
}
