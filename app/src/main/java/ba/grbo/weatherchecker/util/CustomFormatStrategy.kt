package ba.grbo.weatherchecker.util

import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.LogStrategy
import com.orhanobut.logger.LogcatLogStrategy
import com.orhanobut.logger.Logger

class CustomFormatStrategy : FormatStrategy {
    private val tag = "ba.grbo"
    private val logStrategy: LogStrategy = LogcatLogStrategy()
    private var methodCount = 1
    private val methodOffset = 5
    private val minStackOffset = 5
    private var counter = 1

    override fun log(priority: Int, tag: String?, message: String) {
        logStrategy.log(priority, this.tag, getMessage(message))
        counter++
    }

    private fun getExtras(): String {
        val trace = Thread.currentThread().stackTrace
        val stackOffset = getStackOffset(trace) + methodOffset
        if (methodCount + stackOffset > trace.size) methodCount = trace.size - stackOffset - 1
        val stackIndex = methodCount + stackOffset
        val element = trace[stackIndex]

        return String.format(
            "%s.%s (%s:%s)",
            element.simpleClassName,
            element.methodName,
            element.fileName,
            element.lineNumber
        )
    }

    private fun getMessage(message: String) = String.format(
        "%d. %s -> %s -> %s",
        counter,
        Thread.currentThread().name,
        getExtras(),
        message
    )

    private fun getStackOffset(trace: Array<StackTraceElement>): Int {
        for (i in minStackOffset..trace.lastIndex) {
            val e = trace[i]
            val name = e.className
            if (name != "com.orhanobut.logger.LoggerPrinter" && name != Logger::class.java.name) {
                return i - 1
            }
        }
        return -1
    }

    private val StackTraceElement.simpleClassName: String
        get() {
            val lastIndex = className.lastIndexOf(".")
            return className.substring(lastIndex + 1)
        }
}