package ru.khalikov.messenger.data

data class UserProfile @JvmOverloads constructor(
    val uid: String = "",
    val publicId: String = "",
    val nickname: String = "",
    val about: String = "",
    val photoUrl: String = ""
)

val UserProfile.isComplete: Boolean
    get() = nickname.isNotBlank()
