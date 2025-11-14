package com.brewhaven.app.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * SignUpFragment
 *
 * Handles creation of new user accounts using Firebase Authentication.
 * Performs full client-side validation (names, email, password strength,
 * and password confirmation) before attempting signup. After successful
 * account creation, the fragment stores basic user profile data in Firestore
 * under /customers/{uid}. Navigation after signup is delegated to
 * MainActivity’s AuthStateListener to avoid conflicting transitions.
 */
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    // Firebase authentication instance for account creation
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Firestore instance used to store user profile details
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(false)

        // TextInputLayouts for form error display
        val layFirst = view.findViewById<TextInputLayout>(R.id.layFirst)
        val layLast  = view.findViewById<TextInputLayout>(R.id.layLast)
        val layEmail = view.findViewById<TextInputLayout>(R.id.layEmail)
        val layPass  = view.findViewById<TextInputLayout>(R.id.layPass)
        val layPass2 = view.findViewById<TextInputLayout>(R.id.layPass2)

        // EditTexts for user input
        val first  = view.findViewById<TextInputEditText>(R.id.editFirst)
        val last   = view.findViewById<TextInputEditText>(R.id.editLast)
        val email  = view.findViewById<TextInputEditText>(R.id.editEmail)
        val pass1  = view.findViewById<TextInputEditText>(R.id.editPass)
        val pass2  = view.findViewById<TextInputEditText>(R.id.editPass2)
        val btn    = view.findViewById<Button>(R.id.btnCreate)
        val bar    = view.findViewById<MaterialToolbar>(R.id.toolbar)

        // Return to previous screen
        bar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        /**
         * Validates password strength according to basic security rules.
         * Requirements: at least 8 chars, one upper, one lower, one digit,
         * and one non-alphanumeric symbol.
         */
        fun isStrongPassword(p: String): Boolean {
            if (p.length < 8) return false
            val hasUpper = p.any { it.isUpperCase() }
            val hasLower = p.any { it.isLowerCase() }
            val hasDigit = p.any { it.isDigit() }
            val hasSpecial = p.any { !it.isLetterOrDigit() }
            return hasUpper && hasLower && hasDigit && hasSpecial
        }

        btn.setOnClickListener {
            // Clear previous validation errors
            layFirst.error = null
            layLast.error  = null
            layEmail.error = null
            layPass.error  = null
            layPass2.error = null

            val f  = first.text?.toString()?.trim().orEmpty()
            val l  = last.text?.toString()?.trim().orEmpty()
            val e  = email.text?.toString()?.trim().orEmpty()
            val p1 = pass1.text?.toString().orEmpty()
            val p2 = pass2.text?.toString().orEmpty()

            var ok = true

            // Required field validation
            if (f.isEmpty()) {
                layFirst.error = "First name is required"
                ok = false
            }
            if (l.isEmpty()) {
                layLast.error = "Last name is required"
                ok = false
            }
            if (e.isEmpty()) {
                layEmail.error = "Email is required"
                ok = false
            }

            // Password strength validation
            if (p1.isEmpty()) {
                layPass.error = "Password is required"
                ok = false
            } else if (!isStrongPassword(p1)) {
                layPass.error = "Min 8 chars, upper, lower, number & symbol\nExample: CafeBrew!1"
                ok = false
            }

            // Confirm password validation
            if (p2.isEmpty()) {
                layPass2.error = "Please confirm your password"
                ok = false
            } else if (p1 != p2) {
                layPass2.error = "Passwords do not match"
                ok = false
            }

            if (!ok) {
                if (isAdded) {
                    Toast.makeText(
                        requireContext(),
                        "Fix the highlighted fields.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnClickListener
            }

            btn.isEnabled = false  // Prevent multiple taps

            // Create Firebase account
            auth.createUserWithEmailAndPassword(e, p1)
                .addOnSuccessListener { task ->
                    val uid = task.user?.uid ?: return@addOnSuccessListener

                    // Basic profile stored in Firestore for later use
                    val userDoc = mapOf(
                        "firstName" to f,
                        "lastName"  to l,
                        "email"     to e,
                        "createdAt" to System.currentTimeMillis(),
                        "isActive"  to true
                    )

                    db.collection("customers").document(uid)
                        .set(userDoc, SetOptions.merge())
                        .addOnSuccessListener {
                            if (!isAdded) return@addOnSuccessListener
                            Toast.makeText(
                                requireContext(),
                                "Account created.",
                                Toast.LENGTH_SHORT
                            ).show()
                            // MainActivity's AuthStateListener handles navigation after signup
                        }
                        .addOnFailureListener { err ->
                            if (isAdded) {
                                Toast.makeText(
                                    requireContext(),
                                    err.localizedMessage ?: "Failed to save profile",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnCompleteListener {
                            if (isAdded) btn.isEnabled = true
                        }
                }
                .addOnFailureListener { err ->
                    if (isAdded) {
                        Toast.makeText(
                            requireContext(),
                            err.localizedMessage ?: "Signup failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        btn.isEnabled = true
                    }
                }
        }
    }

    /**
     * Ensures bottom navigation remains hidden while the user is on the
     * signup screen or returns to it.
     */
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }
}
