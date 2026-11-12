package edu.uniquindio.co.tribooo.data.remote

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import edu.uniquindio.co.tribooo.domain.model.DataErrorReason
import edu.uniquindio.co.tribooo.domain.model.DataException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "Firestore"

/** Máximo de operaciones que Firestore admite en un mismo lote de escritura. */
private const val MAX_BATCH_OPERATIONS = 500

/** Tiempo que un flujo compartido sigue escuchando a Firestore después de que nadie lo observa. */
internal const val STOP_TIMEOUT_MILLIS = 5_000L

/** Ejecuta [block] traduciendo las excepciones de Firebase a [DataException]; la cancelación se propaga. */
internal suspend inline fun <T> firestoreCall(block: () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: DataException) {
    throw e
} catch (e: Exception) {
    throw DataException(e.toDataErrorReason(), e)
}

internal fun Throwable.toDataErrorReason(): DataErrorReason = when (this) {
    is FirebaseNetworkException -> DataErrorReason.NETWORK
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.UNAVAILABLE,
        FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> DataErrorReason.NETWORK
        FirebaseFirestoreException.Code.PERMISSION_DENIED,
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> DataErrorReason.PERMISSION_DENIED
        FirebaseFirestoreException.Code.NOT_FOUND -> DataErrorReason.NOT_FOUND
        else -> DataErrorReason.UNKNOWN
    }
    else -> DataErrorReason.UNKNOWN
}

/** Convierte los documentos de la consulta a DTO; [withId] copia el id del documento dentro del DTO. */
internal fun <T : Any> QuerySnapshot.toDtos(type: Class<T>, withId: (T, String) -> T): List<T> =
    documents.mapNotNull { snapshot -> snapshot.toObject(type)?.let { withId(it, snapshot.id) } }

/**
 * Flujo frío con los documentos de la consulta: registra el listener al empezar a recolectarlo
 * y lo elimina con `awaitClose` cuando nadie lo recolecta.
 */
internal fun <T : Any> Query.documentsFlow(type: Class<T>, withId: (T, String) -> T = { dto, _ -> dto }): Flow<List<T>> =
    callbackFlow {
        val registration = addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listener cancelado", error)
                close(error)
                return@addSnapshotListener
            }
            snapshot?.let { trySend(it.toDtos(type, withId)) }
        }
        awaitClose { registration.remove() }
    }

/** Mantiene [target] al día con los documentos de la colección mientras viva la app. */
internal fun <T : Any> CollectionReference.listenInto(
    target: MutableStateFlow<List<T>>,
    type: Class<T>,
    withId: (T, String) -> T = { dto, _ -> dto }
): ListenerRegistration = addSnapshotListener { snapshot, error ->
    if (error != null) Log.w(TAG, "Listener de $path cancelado", error)
    snapshot?.let { target.value = it.toDtos(type, withId) }
}

/** Aplica [operation] a cada elemento en lotes de escritura (Firestore limita cada lote a 500 operaciones). */
internal suspend fun <T> FirebaseFirestore.commitInBatches(items: List<T>, operation: WriteBatch.(T) -> Unit) {
    items.chunked(MAX_BATCH_OPERATIONS).forEach { chunk ->
        batch().apply { chunk.forEach { operation(it) } }.commit().await()
    }
}
