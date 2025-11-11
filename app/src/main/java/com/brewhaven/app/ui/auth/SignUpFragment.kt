package com.brewhaven.app.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val first = view.findViewById<TextInputEditText>(R.id.editFirst)
        val last  = view.findViewById<TextInputEditText>(R.id.editLast)
        val email = view.findViewById<TextInputEditText>(R.id.editEmail)
        val pass1 = view.findViewById<TextInputEditText>(R.id.editPass)
        val pass2 = view.findViewById<TextInputEditText>(R.id.editPass2)
        val btnSignUp = view.findViewById<Button>(R.id.btnCreate)
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack() // go back to Welcome
        }

        btnSignUp.setOnClickListener {
            val f = first.text.toString().trim()
            val l = last.text.toString().trim()
            val e = email.text.toString().trim()
            val p1 = pass1.text.toString()
            val p2 = pass2.text.toString()

            if (f.isEmpty() || l.isEmpty() || e.isEmpty() || p1.length < 6) {
                Toast.makeText(requireContext(), "Fill all fields (password ≥ 6)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (p1 != p2) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Auth sign-up first
            auth.createUserWithEmailAndPassword(e, p1)
                .addOnSuccessListener { task ->
                    val uid = task.user?.uid ?: return@addOnSuccessListener
                    val user = hashMapOf(
                        "firstName" to f,
                        "lastName" to l,
                        "email" to e,
                        "createdAt" to System.currentTimeMillis(),
                        "isActive" to true
                    )

                    // Save user info to Firestore
                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Welcome, $f!", Toast.LENGTH_SHORT).show()

                            // clear auth stack and show menu
                            parentFragmentManager.popBackStack(
                                null,
                                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
                            )

                            (activity as? MainActivity)?.apply {
                                setBottomNavVisible(true)
                                findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
                                    .selectedItemId = R.id.nav_menu
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "User saved but Firestore failed.", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { err ->
                    Toast.makeText(requireContext(), err.localizedMessage ?: "Signup failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }
}
