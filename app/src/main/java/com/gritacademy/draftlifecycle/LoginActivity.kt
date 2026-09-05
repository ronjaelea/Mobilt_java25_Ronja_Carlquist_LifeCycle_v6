package com.gritacademy.draftlifecycle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var loginBtn: Button
    private lateinit var progressSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Firebase Auth återställer sin egen session från disk. Finns en användare
        // redan -> hoppa direkt till User space utan att visa login.
        if (auth.currentUser != null) {
            goToUserSpace()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        passwordLayout = findViewById(R.id.loginPassword)
        loginBtn = findViewById(R.id.loginBtn)
        progressSpinner = findViewById(R.id.progressSpinner)

        loginBtn.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        val email = emailInput.text?.toString()?.trim().orEmpty()
        val password = passwordInput.text?.toString().orEmpty()

        passwordLayout.error = null

        if (email.isEmpty() || password.isEmpty()) {
            passwordLayout.error = getString(R.string.login_error_empty)
            return
        }

        setLoading(true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                setLoading(false)
                if (task.isSuccessful) {
                    goToUserSpace()
                } else {
                    passwordLayout.error = getString(R.string.login_error_failed)
                }
            }
    }

    private fun setLoading(loading: Boolean) {
        progressSpinner.visibility = if (loading) View.VISIBLE else View.GONE
        loginBtn.isEnabled = !loading
    }

    private fun goToUserSpace() {
        startActivity(Intent(this, UserSpaceActivity::class.java))
        finish()
    }
}
