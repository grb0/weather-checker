package ba.grbo.weatherchecker.util

import android.util.Log
import ba.grbo.weatherchecker.util.Constants.BOSNIAN_LOCALE
import ba.grbo.weatherchecker.util.Constants.COMMON_TAG
import ba.grbo.weatherchecker.util.Constants.EXCEPTIONS_COLLECTION
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.text.SimpleDateFormat
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
        "%s - %s - %s",
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
            private val timestamp: String
                get() {
                    val time = System.currentTimeMillis()
                    return String.format(
                        "%s, %s",
                        time.toFormattedDate(BOSNIAN_LOCALE),
                        time.toFormattedTime(SimpleDateFormat.MEDIUM, BOSNIAN_LOCALE)
                    )
                }

            override fun log(
                priority: Int,
                tag: String?,
                message: String,
                t: Throwable?
            ) {
                if (priority == Log.ERROR || priority == Log.ASSERT || priority == Log.WARN) {
                    reportToFirebase(createModifiedMessage(message), t)
                }
            }

            private fun reportToFirebase(message: String, t: Throwable?) {
                FirebaseCrashlytics.getInstance().run {
                    log(message)
                    if (t != null) recordException(t)
                }
                recordExceptionToFirestore(message, t)
            }

            private fun recordExceptionToFirestore(message: String, t: Throwable?) {
                Firebase.firestore.collection(EXCEPTIONS_COLLECTION)
                    .add(
                        mapOf(
                            "time" to timestamp,
                            "message" to message,
                            "exception" to t.toDocumentOrNull()
                        )
                    )
            }

            private fun Throwable?.toDocumentOrNull(): Map<String, String?>? {
                return if (this != null) mapOf(
                    "type" to this::class.simpleName,
                    "message" to message
                ) else null
            }
        }
    }
}