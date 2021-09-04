package ba.grbo.weatherchecker.util

import android.util.Log
import ba.grbo.weatherchecker.util.Constants.COMMON_TAG
import ba.grbo.weatherchecker.util.Tree.Pattern.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

abstract class Tree private constructor() : Timber.DebugTree() {
    private lateinit var element: StackTraceElement
    private val debugMessagePattern = "message: %s\n  info:    %s\n  thread:  %s\n "
    protected open val errorMessagePattern =
        "exception:  %s\n  message:    %s\n  info:       %s\n  thread:     %s\n  suppressed: %s\n "

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

    private val Throwable?.simpleClassNameOrNone: String
        get() = if (this != null) this::class.simpleName ?: "None" else "None"

    private val Throwable?.messageOrNone: String
        get() = this?.message ?: "None"

    protected open fun getSuppressedPattern(
        position: Int,
        lastPosition: Int
    ): String = when (position) {
        0 -> FIRST.value
        lastPosition -> LAST.value
        else -> MIDDLE.value
    }

    protected open val Throwable?.suppressedInfoOrNone: String
        get() {
            return if (this != null) {
                if (suppressed.isEmpty()) "None"
                else suppressed.run {
                    val sB = StringBuilder()
                    forEachIndexed { i, t ->
                        sB.append(
                            String.format(
                                getSuppressedPattern(i, lastIndex),
                                i + 1,
                                t.simpleClassNameOrNone,
                                t.messageOrNone,
                                t.stackTrace[0].info
                            )
                        )

                    }
                    sB.toString()
                }
            } else "None"
        }

    private enum class Pattern(val value: String) {
        FIRST("%d. exception: %s\n                 message: %s\n                 info: %s\n"),
        LAST("              %d. exception: %s\n                 message: %s\n                 info: %s"),
        MIDDLE("              %d. exception: %s\n                 message: %s\n                 info: %s\n"),
        UNSPACED("%d. exception: %s message: %s info: %s")
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, createModifiedMessage(message, t, priority), t)
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        // We need the next, because we wrapped Timber function calls inside of DefaultLogger
        this.element = element.next
        return COMMON_TAG
    }

    open fun createDebugMessage(message: String) = String.format(
        debugMessagePattern,
        message,
        element.info,
        Thread.currentThread().name
    )

    open fun createErrorMessage(throwable: Throwable?) = String.format(
        errorMessagePattern,
        throwable.simpleClassNameOrNone,
        throwable.messageOrNone,
        element.info,
        Thread.currentThread().name,
        throwable.suppressedInfoOrNone
    )

    open fun createModifiedMessage(
        message: String,
        throwable: Throwable?,
        priority: Int
    ) = if (priority == Log.DEBUG) createDebugMessage(message) else createErrorMessage(throwable)

    companion object {
        val Timber.Forest.DEBUG_TREE: Tree
            get() = DEBUG

        val Timber.Forest.RELEASE_TREE: Tree
            get() = RELEASE

        private val DEBUG: Tree by lazy {
            object : Tree() {
                private var counter = AtomicInteger(1)

                override fun createModifiedMessage(
                    message: String,
                    throwable: Throwable?,
                    priority: Int
                ) = String.format(
                    "%d. %s",
                    counter.getAndIncrement(),
                    super.createModifiedMessage(message, throwable, priority)
                )
            }
        }

        private val RELEASE: Tree by lazy {
            object : Tree() {
                override val errorMessagePattern =
                    "exception: %s, message: %s, info: %s, thread: %s, suppressed: %s"

                override fun getSuppressedPattern(
                    position: Int,
                    lastPosition: Int
                ): String = UNSPACED.value

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
                            log(createModifiedMessage(message, t, priority))
                            recordException(t)
                        }
                    }
                }
            }
        }
    }
}