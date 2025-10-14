package com.example.saweather

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var preferences: Preferences
    private lateinit var auth: FirebaseAuth
    private lateinit var languageManager: LanguageManager
    private lateinit var db: FirebaseFirestore  // Add Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase and check authentication IMMEDIATELY
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()  // Initialize Firestore
        preferences = Preferences(this)

        // Initialize language manager
        languageManager = LanguageManager(this)
        languageManager.updateConfiguration(this)

        // Check if user is already logged in - do this BEFORE setting content view
        if (preferences.isLoggedIn() || auth.currentUser != null) {
            // User is logged in, go directly to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return  // Exit onCreate early
        }

        // Only show login screen if user is not logged in
        setContentView(R.layout.activity_login)
        setupViews()
    }

    override fun onResume() {
        super.onResume()
        // Ensure language is applied when returning to activity
        languageManager.updateConfiguration(this)
    }

    private fun setupViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerTextView = findViewById(R.id.registerTextView)

        loginButton.setOnClickListener {
            loginUser()
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            return
        }

        // Show loading
        "Logging in...".also { loginButton.text = it }
        loginButton.isEnabled = false

        // Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful - get user data from Firestore
                    val user = auth.currentUser
                    user?.let {
                        getUserDataFromFirestore(it.uid, email)
                    }
                } else {
                    // Login failed
                    "Login".also { loginButton.text = it }
                    loginButton.isEnabled = true
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun getUserDataFromFirestore(uid: String, email: String) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        // Use the display name from Firestore
                        val displayName = it.displayName.ifEmpty { email.substringBefore("@") }
                        preferences.saveUser(email, displayName, "Johannesburg")
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    // If user document doesn't exist, fall back to email-based name
                    val nameFromEmail = email.substringBefore("@")
                    preferences.saveUser(email, nameFromEmail, "Johannesburg")
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                // If Firestore fails, fall back to email-based name
                val nameFromEmail = email.substringBefore("@")
                preferences.saveUser(email, nameFromEmail, "Johannesburg")
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}