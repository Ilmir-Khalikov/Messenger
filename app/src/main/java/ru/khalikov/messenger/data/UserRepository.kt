package ru.khalikov.messenger.data

import android.content.ContentResolver
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val users = firestore.collection(USERS_COLLECTION)
    private val publicIds = firestore.collection(PUBLIC_IDS_COLLECTION)

    suspend fun getProfile(uid: String): UserProfile? {
        val snapshot = users.document(uid).get().await()
        if (!snapshot.exists()) return null
        return snapshot.toObject(UserProfile::class.java)?.copy(uid = uid)
    }

    suspend fun getOrCreateProfile(uid: String, email: String?): UserProfile {
        val existing = getProfile(uid)
        if (existing != null && existing.publicId.isNotBlank()) {
            return existing
        }

        val publicId = allocatePublicId(uid, publicIdFromEmail(email))
        val profile = (existing ?: UserProfile(uid = uid)).copy(publicId = publicId)
        users.document(uid).set(profile).await()
        return profile
    }

    suspend fun saveProfile(
        uid: String,
        nickname: String,
        about: String,
        photoUri: Uri?,
        contentResolver: ContentResolver
    ): UserProfile {
        val current = getProfile(uid) ?: getOrCreateProfile(uid, null)
        val photoUrl = if (photoUri != null) {
            withContext(Dispatchers.IO) {
                AvatarEncoder.encode(contentResolver, photoUri)
            }
        } else {
            current.photoUrl
        }
        val updated = current.copy(
            nickname = nickname.trim(),
            about = about.trim(),
            photoUrl = photoUrl
        )
        users.document(uid).set(updated).await()
        return updated
    }

    private suspend fun allocatePublicId(uid: String, preferred: String): String {
        val base = preferred.ifBlank { "id${uid.take(8).lowercase()}" }
        repeat(40) { index ->
            val candidate = if (index == 0) base else "$base$index"
            if (tryReservePublicId(uid, candidate)) return candidate
        }
        val fallback = "id${uid.take(10).lowercase()}"
        tryReservePublicId(uid, fallback)
        return fallback
    }

    private suspend fun tryReservePublicId(uid: String, publicId: String): Boolean {
        val ref = publicIds.document(publicId)
        val snapshot = ref.get().await()
        val owner = snapshot.getString("uid")
        if (snapshot.exists() && owner != uid) {
            return false
        }
        ref.set(mapOf("uid" to uid)).await()
        return true
    }

    private fun publicIdFromEmail(email: String?): String {
        val localPart = email.orEmpty().substringBefore("@")
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .take(20)
        return localPart.ifBlank { "" }
    }

    private companion object {
        const val USERS_COLLECTION = "users"
        const val PUBLIC_IDS_COLLECTION = "publicIds"
    }
}
