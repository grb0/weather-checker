package ba.grbo.weatherchecker.util

import android.util.Log
import ba.grbo.weatherchecker.util.Constants.COMMON_TAG
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

open class Tree private constructor() : Timber.DebugTree() {
    private lateinit var element: StackTraceElement

    private val StackTraceElement.info: String
        get() = String.format(
            "%s.%s (%s:%s)",
            className.substring(className.lastIndexOf(".") + 1),
            methodName,
            fileName,
            lineNumber
        )

    private val StackTraceElement.next: StackTraceElement
        get() {
            val stackTrace = Throwable().stackTrace
            val index = stackTrace.indexOf(this)
            return stackTrace[index + 1]
        }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, createModifiedMessage(message, priority), t)
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        // We need the next, because we wrapped Timber function calls inside of DefaultLogger
        this.element = element.next
        return COMMON_TAG
    }

    open fun createModifiedMessage(
        message: String,
        priority: Int
    ) = if (priority == Log.DEBUG) String.format(
        "%s - %s - %s",
        Thread.currentThread().name,
        element.info,
        message
    ) else String.format(
        "%s - %s",
        Thread.currentThread().name,
        message
    )

    companion object {
        val Timber.Forest.DEBUG_TREE: Tree
            get() = DEBUG

        val Timber.Forest.RELEASE_TREE: Tree
            get() = RELEASE

        private val DEBUG: Tree = object : Tree() {
            private var counter = AtomicInteger(1)

            override fun createModifiedMessage(
                message: String,
                priority: Int
            ) = String.format(
                "%d. %s",
                counter.getAndIncrement(),
                super.createModifiedMessage(message, priority)
            )
        }

        private val RELEASE: Tree = object : Tree() {
            override fun log(
                priority: Int,
                tag: String?,
                message: String,
                t: Throwable?
            ) {
                if (priority == Log.ERROR) t?.let {
                    FirebaseCrashlytics.getInstance().run {
                        log(createModifiedMessage(message, priority))
                        recordException(t)
                    }
                }
            }
        }
    }
}