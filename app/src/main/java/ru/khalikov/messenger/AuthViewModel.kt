package ru.khalikov.messenger

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.khalikov.messenger.data.FirestoreErrors
import ru.khalikov.messenger.data.UserProfile
import ru.khalikov.messenger.data.UserRepository
import ru.khalikov.messenger.data.isComplete

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _profileSaveState = MutableLiveData<ProfileSaveState>(ProfileSaveState.Idle)
    val profileSaveState: LiveData<ProfileSaveState> = _profileSaveState

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus() {
        val user = auth.currentUser
        if (user == null) {
            _profile.value = null
            _authState.value = AuthState.Unauthenticated
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            loadProfileAfterAuth(requireCompleteProfile = true)
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (error: Exception) {
                _authState.value = AuthState.Error(error.message ?: "Something went wrong")
                return@launch
            }
            loadProfileAfterAuth(requireCompleteProfile = true)
        }
    }

    fun signup(email: String, password: String, passwordConfirmation: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email or password can't be empty")
            return
        }

        if (password != passwordConfirmation) {
            _authState.value = AuthState.Error("The passwords do not match")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
            } catch (error: Exception) {
                _authState.value = AuthState.Error(error.message ?: "Something went wrong")
                return@launch
            }
            loadProfileAfterAuth(requireCompleteProfile = false)
        }
    }

    fun loadProfile() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                _profile.value = userRepository.getOrCreateProfile(user.uid, user.email)
            } catch (error: Exception) {
                _profileSaveState.value = ProfileSaveState.Error(FirestoreErrors.messageOf(error))
            }
        }
    }

    fun saveProfile(
        nickname: String,
        about: String,
        photoUri: Uri?,
        contentResolver: ContentResolver
    ) {
        val user = auth.currentUser
        if (user == null) {
            _profileSaveState.value = ProfileSaveState.Error("User is missing")
            return
        }
        if (nickname.isBlank()) {
            _profileSaveState.value = ProfileSaveState.Error("Введите никнейм")
            return
        }

        _profileSaveState.value = ProfileSaveState.Saving
        viewModelScope.launch {
            try {
                val updated = userRepository.saveProfile(
                    uid = user.uid,
                    nickname = nickname,
                    about = about,
                    photoUri = photoUri,
                    contentResolver = contentResolver
                )
                _profile.value = updated
                _authState.value = AuthState.Authenticated(profileComplete = true)
                _profileSaveState.value = ProfileSaveState.Saved
            } catch (error: Exception) {
                _profileSaveState.value = ProfileSaveState.Error(FirestoreErrors.messageOf(error))
            }
        }
    }

    fun consumeProfileSaveState() {
        _profileSaveState.value = ProfileSaveState.Idle
    }

    fun signout() {
        auth.signOut()
        _profile.value = null
        _profileSaveState.value = ProfileSaveState.Idle
        _authState.value = AuthState.Unauthenticated
    }

    private suspend fun loadProfileAfterAuth(requireCompleteProfile: Boolean) {
        val user = auth.currentUser
        if (user == null) {
            _authState.value = AuthState.Unauthenticated
            return
        }

        try {
            val profile = userRepository.getOrCreateProfile(user.uid, user.email)
            _profile.value = profile
            _authState.value = AuthState.Authenticated(
                profileComplete = requireCompleteProfile && profile.isComplete
            )
        } catch (error: Exception) {
            _authState.value = AuthState.Error(FirestoreErrors.messageOf(error))
        }
    }
}

sealed class AuthState {
    data class Authenticated(val profileComplete: Boolean) : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class ProfileSaveState {
    object Idle : ProfileSaveState()
    object Saving : ProfileSaveState()
    object Saved : ProfileSaveState()
    data class Error(val message: String) : ProfileSaveState()
}
