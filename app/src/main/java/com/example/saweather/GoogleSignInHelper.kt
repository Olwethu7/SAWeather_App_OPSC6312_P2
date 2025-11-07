package com.example.saweather

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class GoogleSignInHelper(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val preferences = Preferences(context)

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("945154804367-xxxxxxxx.apps.googleusercontent.com") // You'll need to update this
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    fun handleSignInResult(data: Intent?, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!, onSuccess, onFailure)
        } catch (e: ApiException) {
            onFailure("Google sign in failed: ${e.statusCode}")
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        saveUserToFirestore(it, onSuccess)
                    }
                } else {
                    onFailure("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserToFirestore(user: com.google.firebase.auth.FirebaseUser, onSuccess: () -> Unit) {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val userData = User(
            uid = user.uid,
            email = user.email ?: "",
            displayName = user.displayName ?: user.email?.substringBefore("@") ?: "User",
            createdAt = currentTime,
            lastLogin = currentTime
        )

        db.collection("users")
            .document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                preferences.saveUser(user.email ?: "", user.displayName ?: "User", "Johannesburg")
                onSuccess()
            }
            .addOnFailureListener {
                // Still proceed even if Firestore fails
                preferences.saveUser(user.email ?: "", user.displayName ?: "User", "Johannesburg")
                onSuccess()
            }
    }
}