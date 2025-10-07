package com.example.saweather

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.yourteam.saweather.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize Firebase Auth
            auth = Firebase.auth

            Log.d(TAG, "LoginActivity created successfully - Firebase Auth initialized")
            setupClickListeners()

            // Check if we're coming from logout
            val fromLogout = intent.getBooleanExtra("FROM_LOGOUT", false)
            if (fromLogout) {
                showToast("You have been logged out successfully")
                // Clear any auto-login attempts
                clearFields()
            } else {
                // Only auto-login if not coming from logout
                checkCurrentUser()
            }

        } catch (e: Exception) {
            Log.e(TAG, "CRASH in LoginActivity onCreate: ${e.message}", e)
            showToast("App crashed during startup: ${e.message}")
        }
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "User already logged in: ${currentUser.email}")
            showToast("Welcome back, ${currentUser.email}!")
            safeNavigateToMainApp("AUTO_LOGIN")
        } else {
            Log.d(TAG, "No user currently logged in")
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (isValidEmail(email)) {
                    loginUserWithFirebase(email, password)
                } else {
                    showToast("Please enter a valid email address")
                }
            } else {
                showToast("Please fill all fields")
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (isValidEmail(email)) {
                    if (password.length >= 6) {
                        registerUserWithFirebase(email, password)
                    } else {
                        showToast("Password must be at least 6 characters")
                    }
                } else {
                    showToast("Please enter a valid email address")
                }
            } else {
                showToast("Please fill all fields")
            }
        }
    }

    private fun clearFields() {
        binding.etEmail.text.clear()
        binding.etPassword.text.clear()
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun loginUserWithFirebase(email: String, password: String) {
        Log.d(TAG, "Attempting FIREBASE LOGIN for: $email")

        // Disable buttons during network call
        setButtonsEnabled(false)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "Firebase login successful")
                    val user = auth.currentUser
                    showToast("Welcome back, ${user?.email}!")
                    safeNavigateToMainApp("FIREBASE_LOGIN")
                } else {
                    // Sign in failed
                    Log.e(TAG, "Firebase login failed: ${task.exception?.message}")
                    val errorMessage = when {
                        task.exception?.message?.contains("network", ignoreCase = true) == true ->
                            "Network error. Check your internet connection"
                        task.exception?.message?.contains("invalid", ignoreCase = true) == true ->
                            "Invalid email or password"
                        task.exception?.message?.contains("user", ignoreCase = true) == true ->
                            "No account found with this email"
                        else -> "Login failed: ${task.exception?.message}"
                    }
                    showToast(errorMessage)
                }
                setButtonsEnabled(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Firebase login failure: ${exception.message}")
                showToast("Login failed: ${exception.message}")
                setButtonsEnabled(true)
            }
    }

    private fun registerUserWithFirebase(email: String, password: String) {
        Log.d(TAG, "Attempting FIREBASE REGISTRATION for: $email")

        // Disable buttons during network call
        setButtonsEnabled(false)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration success
                    Log.d(TAG, "Firebase registration successful")
                    val user = auth.currentUser

                    // Send email verification (optional)
                    user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                        if (verificationTask.isSuccessful) {
                            Log.d(TAG, "Verification email sent")
                        }
                    }

                    showToast("Account created successfully!")
                    safeNavigateToMainApp("FIREBASE_REGISTRATION")
                } else {
                    // Registration failed
                    Log.e(TAG, "Firebase registration failed: ${task.exception?.message}")
                    val errorMessage = when {
                        task.exception?.message?.contains("network", ignoreCase = true) == true ->
                            "Network error. Check your internet connection"
                        task.exception?.message?.contains("email already", ignoreCase = true) == true ->
                            "An account with this email already exists"
                        task.exception?.message?.contains("weak", ignoreCase = true) == true ->
                            "Password is too weak"
                        else -> "Registration failed: ${task.exception?.message}"
                    }
                    showToast(errorMessage)
                }
                setButtonsEnabled(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Firebase registration failure: ${exception.message}")
                showToast("Registration failed: ${exception.message}")
                setButtonsEnabled(true)
            }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnLogin.isEnabled = enabled
        binding.btnRegister.isEnabled = enabled
        binding.btnLogin.text = if (enabled) "Login" else "Logging in..."
        binding.btnRegister.text = if (enabled) "Create Account" else "Creating Account..."
    }

    private fun safeNavigateToMainApp(source: String) {
        Log.d(TAG, "Navigation attempt from: $source")

        try {
            val intent = Intent(this, WeatherDetailActivity::class.java)
            startActivity(intent)
            finish()
            Log.d(TAG, "✅ NAVIGATION SUCCESSFUL from: $source")
        } catch (e: Exception) {
            Log.e(TAG, "❌ NAVIGATION FAILED from $source: ${e.message}", e)
            showToast("Navigation failed: ${e.message}")
        }
    }

    private fun showToast(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Toast failed: ${e.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "LoginActivity onStart - Current user: ${auth.currentUser?.email ?: "None"}")
    }
}