package com.mediremind.app.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val isGuest: Boolean = false
)

class AuthRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("mediremind_auth", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserProfile?>(loadSavedUser())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private fun loadSavedUser(): UserProfile? {
        val email = prefs.getString("user_email", null) ?: return null
        val name = prefs.getString("user_name", "Usuario") ?: "Usuario"
        val id = prefs.getString("user_id", "") ?: ""
        val photo = prefs.getString("user_photo", null)
        val isGuest = prefs.getBoolean("user_is_guest", false)

        return UserProfile(
            id = id,
            displayName = name,
            email = email,
            photoUrl = photo,
            isGuest = isGuest
        )
    }

    private fun persistUser(user: UserProfile) {
        prefs.edit()
            .putString("user_id", user.id)
            .putString("user_name", user.displayName)
            .putString("user_email", user.email)
            .putString("user_photo", user.photoUrl)
            .putBoolean("user_is_guest", user.isGuest)
            .apply()
        _currentUser.value = user
    }

    /**
     * Inicia sesión con Google utilizando la API moderna de Credential Manager de Android.
     */
    suspend fun signInWithGoogle(webClientId: String): Result<UserProfile> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val user = UserProfile(
                    id = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName ?: "Usuario Google",
                    email = googleIdTokenCredential.id,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    isGuest = false
                )
                persistUser(user)
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Credencial no reconocida"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Permite iniciar sesión de prueba o como invitado local sin conexión a internet.
     */
    fun signInAsGuest(customName: String = "Paciente Invitado") {
        val guest = UserProfile(
            id = "guest_${System.currentTimeMillis()}",
            displayName = customName,
            email = "modo.local@mediremind.app",
            photoUrl = null,
            isGuest = true
        )
        persistUser(guest)
    }

    /**
     * Inicia sesión simulando una cuenta de Google para pruebas en emuladores sin Play Services configurados.
     */
    fun signInMockGoogle(name: String, email: String) {
        val mock = UserProfile(
            id = "google_mock_${email.hashCode()}",
            displayName = name,
            email = email,
            photoUrl = null,
            isGuest = false
        )
        persistUser(mock)
    }

    fun signOut() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }
}
