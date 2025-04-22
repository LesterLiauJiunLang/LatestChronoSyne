package com.example.chronosyne.repository

import com.example.chronosyne.database.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Email/password authentication
    suspend fun signUpWithEmail(email: String, password: String, username: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = User(
                userId = authResult.user?.uid ?: throw Exception("User creation failed"),
                username = username,
                email = email
            )
            saveUserToFirestore(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Google Sign-In
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user?.let {
                User(
                    userId = it.uid,
                    username = it.displayName ?: it.email?.substringBefore("@") ?: "User",
                    email = it.email ?: ""
                )
            } ?: throw Exception("Authentication failed")
            saveUserToFirestore(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithEmail(email: String, password: String) {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val userDoc = firestore.collection("users")
            .document(result.user?.uid ?: "")
            .get()
            .await()

        if (!userDoc.exists()) {
            throw FirebaseAuthInvalidUserException(
                "User not found",
                "No user record corresponding to this identifier"
            )
        }
    }


    suspend fun getUserByEmail(email: String): User? {
        val querySnapshot = firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        return if (!querySnapshot.isEmpty) {
            querySnapshot.documents[0].toObject(User::class.java)
        } else null
    }

    suspend fun deleteUserAccount(): Boolean {
        val currentUser = auth.currentUser
        return try {
            // Delete from Firestore
            currentUser?.uid?.let { uid ->
                firestore.collection("users").document(uid).delete().await()
            }

            // Delete from Auth (must be recently logged in)
            currentUser?.delete()?.await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUsername(newUsername: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            firestore.collection("users").document(uid)
                .update("username", newUsername)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection("users")
            .document(user.userId)
            .set(user)
            .await()
    }

    fun getCurrentUser() = auth.currentUser?.run {
        User(
            userId = uid,
            email = email ?: "",
            username = displayName ?: email?.substringBefore("@") ?: "User"
        )
    }
}