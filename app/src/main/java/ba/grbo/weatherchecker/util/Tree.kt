package ba.grbo.weatherchecker.util

import android.util.Log
import ba.grbo.weatherchecker.util.Constants.COMMON_TAG
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

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, createModifiedMessage(message), t)
    }

    override fun createStackElementTag(element: StackTraceElement): String {
        this.element = element
        return COMMON_TAG
    }

    open fun createModifiedMessage(message: String) = String.format(
        "%s -> %s -> %s",
        Thread.currentThread().name,
        element.info,
        message
    )

    companion object {
        val Timber.Forest.DEBUG_TREE: Tree
            get() = DEBUG

        val Timber.Forest.RELEASE_TREE: Tree
            get() = RELEASE

        private val DEBUG: Tree = object : Tree() {
            private var counter = AtomicInteger(1)

            override fun createModifiedMessage(message: String) = String.format(
                "%d. %s",
                counter.getAndIncrement(),
                super.createModifiedMessage(message)
            )
        }

        private val RELEASE: Tree = object : Tree() {
            override fun log(
                priority: Int,
                tag: String?,
                message: String,
                t: Throwable?
            ) {
                if (priority == Log.ERROR || priority == Log.ASSERT || priority == Log.WARN) {
                    val modifiedMessaged = createModifiedMessage(message)
                    // Log this message to Firebase
                }
            }
        }
    }
}