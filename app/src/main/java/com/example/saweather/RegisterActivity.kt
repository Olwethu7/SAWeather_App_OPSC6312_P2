package com.example.saweather

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginTextView: TextView
    private lateinit var preferences: Preferences
    private lateinit var languageManager: LanguageManager  // Add this declaration

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply language settings first
        languageManager = LanguageManager(this)
        languageManager.updateConfiguration(this)

        setContentView(R.layout.activity_register)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        preferences = Preferences(this)

        initViews()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        // Ensure language is applied when returning to activity
        languageManager.updateConfiguration(this)
    }

    private fun initViews() {
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        registerButton = findViewById(R.id.registerButton)
        loginTextView = findViewById(R.id.loginTextView)
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (validateInput(name, email, password, confirmPassword)) {
                registerUser(name, email, password)
            }
        }

        loginTextView.setOnClickListener {
            finish() // Go back to login activity
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        // Show loading
        "Creating Account...".also { registerButton.text = it }
        registerButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    val user = auth.currentUser
                    user?.let {
                        // Update user profile with display name
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        it.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    // Save user data to Firestore
                                    saveUserToFirestore(it.uid, name, email)
                                } else {
                                    // Still save to Firestore even if profile update fails
                                    saveUserToFirestore(it.uid, name, email)
                                }
                            }
                    }
                } else {
                    // Registration failed
                    "Register".also { registerButton.text = it }
                    registerButton.isEnabled = true
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, name: String, email: String) {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val user = User(
            uid = uid,
            email = email,
            displayName = name,  // This should be the full name from registration
            createdAt = currentTime,
            lastLogin = currentTime
        )

        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener {
                // Save to local preferences - use the full name from registration
                preferences.saveUser(email, name, "Johannesburg")  // This uses the actual name, not email

                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                "Register".also { registerButton.text = it }
                registerButton.isEnabled = true
                Toast.makeText(
                    this,
                    "Registration completed but failed to save user data: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                // Still proceed to main activity with the full name
                preferences.saveUser(email, name, "Johannesburg")  // Use the actual name
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            nameEditText.error = "Name is required"
            isValid = false
        }

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email address"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordEditText.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }
}