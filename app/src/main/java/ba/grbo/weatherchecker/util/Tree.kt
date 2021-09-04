package ba.grbo.weatherchecker.util

import android.util.Log
import ba.grbo.weatherchecker.util.Constants.COMMON_TAG
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

abstract class Tree private constructor() : Timber.DebugTree() {
    private lateinit var element: StackTraceElement

    protected val StackTraceElement.info: String
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

    protected val Throwable.throwableMessage: String
        get() = "${stackTrace[0].info} - ${this::class.simpleName}"

    protected abstract val Throwable.throwableMessages: String

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

            override val Throwable.throwableMessages: String
                get() {
                    val sB = StringBuilder("$throwableMessage\n  suppressed:")
                    suppressed.forEachIndexed { i, t ->
                        sB.append("\n             ${i + 1}. ${t.throwableMessage}")
                    }
                    return sB.toString()
                }

            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                if (priority == Log.ERROR && t != null) {
                    // Passing null as throwable since we don't want to pollute the message with
                    // its stackTrace
                    super.log(priority, tag, t.throwableMessages, null)
                } else super.log(priority, tag, message, t)
            }

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
            override val Throwable.throwableMessages: String
                get() {
                    val sB = StringBuilder("$throwableMessage | suppressed:")
                    suppressed.forEachIndexed { i, t ->
                        sB.append(" ${i + 1}. ${t.throwableMessage} |")
                    }
                    return sB.toString()
                }

            override fun log(
                priority: Int,
                tag: String?,
                message: String,
                t: Throwable?
            ) {
                if (priority == Log.ERROR) t?.let {
                    FirebaseCrashlytics.getInstance().run {
                        // Since recordException is not taking care of possible suppressed
                        // exceptions, we log those manually
                        log(createModifiedMessage(t.throwableMessages, priority))
                        recordException(t)
                    }
                }
            }
        }
    }
}