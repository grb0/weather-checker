package ba.grbo.weatherchecker.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat

interface Firebase {
    suspend fun recordException(message: String, throwable: Throwable?)
}

object DefaultFirebase : Firebase {
    override suspend fun recordException(message: String, throwable: Throwable?) {
        // TODO wrap inside try-catch block
        recordExceptionToCrashlytics(message, throwable)
        recordExceptionToFirestore(message, throwable)
    }

    private fun recordExceptionToCrashlytics(message: String, throwable: Throwable?) {
        // log and recordException -> when actually sent, sending/uploading done automatically
        // on a background thread
        FirebaseCrashlytics.getInstance().run {
            log(message)
            if (throwable != null) recordException(throwable)
        }
    }

    private suspend fun recordExceptionToFirestore(
        message: String,
        throwable: Throwable?
    ) = suspendCancellableCoroutine<DocumentReference> { continuation ->
        com.google.firebase.ktx.Firebase.firestore.collection(Constants.EXCEPTIONS_COLLECTION)
            .add(
                mapOf(
                    "time" to timestamp,
                    "message" to message,
                    "exception" to throwable.toDocumentOrNull()
                )
            ).addOnSuccessListener { documentReference ->
                continuation.resumeWith(Result.success(documentReference))
            }
            .addOnFailureListener { exception ->
                continuation.resumeWith(Result.failure(exception))
            }
            .addOnCanceledListener {
                continuation.cancel()
            }
    }

    private val timestamp: String
        get() {
            val time = System.currentTimeMillis()
            return String.format(
                "%s, %s",
                time.toFormattedDate(Constants.BOSNIAN_LOCALE),
                time.toFormattedTime(SimpleDateFormat.MEDIUM, Constants.BOSNIAN_LOCALE)
            )
        }

    private fun Throwable?.toDocumentOrNull(): Map<String, String?>? {
        return if (this != null) mapOf(
            "type" to this::class.simpleName,
            "message" to message
        ) else null
    }
}