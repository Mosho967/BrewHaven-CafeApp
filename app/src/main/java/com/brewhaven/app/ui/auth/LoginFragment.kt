package com.brewhaven.app.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth

/**
 * LoginFragment
 *
 * Handles user sign-in using Firebase Authentication.
 * Provides input validation for email/password, manages UI state
 * during login attempts, and leaves navigation to MainActivity’s
 * auth state listener to avoid double-navigation problems.
 */
class LoginFragment : Fragment(R.layout.fragment_login) {

    // Firebase Auth instance used for sign-in operations
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(false)

        // Configure toolbar for back navigation
        view.findViewById<MaterialToolbar?>(R.id.toolbar)?.apply {
            title = "Sign in"
            setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        }

        val email = view.findViewById<EditText>(R.id.editEmail)
        val password = view.findViewById<EditText>(R.id.editPassword)
        val btnLogin = view.findViewById<Button>(R.id.buttonLoginSubmit)

        /**
         * Validates email and password fields.
         * Ensures non-empty fields and correct email format before login.
         */
        fun validate(): Boolean {
            var ok = true
            email.error = null; password.error = null
            val e = email.text?.toString()?.trim().orEmpty()
            val p = password.text?.toString().orEmpty()

            if (e.isEmpty()) {
                email.error = "Required"
                ok = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                email.error = "Invalid email"
                ok = false
            }

            if (p.isEmpty()) {
                password.error = "Required"
                ok = false
            }

            return ok
        }

        btnLogin.setOnClickListener {
            if (!validate()) return@setOnClickListener

            val e = email.text.toString().trim()
            val p = password.text.toString()
            btnLogin.isEnabled = false  // Prevent duplicate login taps

            auth.signInWithEmailAndPassword(e, p)
                .addOnSuccessListener {
                    // Guard against UI updates after fragment detaches
                    if (!isAdded) return@addOnSuccessListener

                    btnLogin.isEnabled = true
                    // Navigation handled by MainActivity's AuthStateListener
                }
                .addOnFailureListener { err ->
                    if (isAdded) {
                        Toast.makeText(requireContext(), err.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                    btnLogin.isEnabled = true
                }
        }
    }

    /**
     * Ensures bottom navigation stays hidden when returning
     * to the login screen.
     */
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }
}
