package xyz.ksharma.sumi.log

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

/**
 * Lightweight cross-platform logger. Prints to stderr (visible in Logcat / Xcode
 * console) and forwards throwables to Firebase Crashlytics so non-fatal errors
 * caught by [xyz.ksharma.sumi.coroutines.ext.coroutineExceptionHandler] still
 * land in the dashboard.
 *
 * Crashlytics calls are wrapped in a try/catch — if Firebase isn't initialised
 * yet (very early startup) the logger should not itself throw.
 */
object SumiLog {

    fun error(message: String, throwable: Throwable? = null) {
        println("[Sumi:E] $message${throwable?.let { " — ${it::class.simpleName}: ${it.message}" } ?: ""}")
        if (throwable != null) {
            runCatching { Firebase.crashlytics.recordException(throwable) }
        } else {
            runCatching { Firebase.crashlytics.log("[Sumi:E] $message") }
        }
    }

    fun warn(message: String) {
        println("[Sumi:W] $message")
        runCatching { Firebase.crashlytics.log("[Sumi:W] $message") }
    }
}

/** Convenience top-level matching the Krail API so call sites stay terse. */
fun logError(message: String, throwable: Throwable? = null) = SumiLog.error(message, throwable)
