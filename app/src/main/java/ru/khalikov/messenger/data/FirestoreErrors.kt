package ru.khalikov.messenger.data

import com.google.firebase.firestore.FirebaseFirestoreException

object FirestoreErrors {
    const val PERMISSION_DENIED_MESSAGE =
        "Нет доступа к Firestore. В Firebase Console откройте Build → Firestore Database → Rules и опубликуйте правила для авторизованных пользователей. Storage подключать не нужно."

    const val DATABASE_MISSING_MESSAGE =
        "Cloud Firestore ещё не создан. В Firebase Console откройте Build → Firestore Database и нажмите Create database (режим production, затем вставьте правила). Storage не нужен и остаётся платным."

    fun messageOf(error: Throwable): String {
        val firestoreError = error as? FirebaseFirestoreException
            ?: error.cause as? FirebaseFirestoreException
        return when (firestoreError?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> PERMISSION_DENIED_MESSAGE
            FirebaseFirestoreException.Code.NOT_FOUND,
            FirebaseFirestoreException.Code.FAILED_PRECONDITION,
            FirebaseFirestoreException.Code.UNAVAILABLE -> DATABASE_MISSING_MESSAGE
            else -> error.message ?: "Something went wrong"
        }
    }
}
