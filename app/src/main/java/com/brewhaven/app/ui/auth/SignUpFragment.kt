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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(false)

        val first  = view.findViewById<TextInputEditText>(R.id.editFirst)
        val last   = view.findViewById<TextInputEditText>(R.id.editLast)
        val email  = view.findViewById<TextInputEditText>(R.id.editEmail)
        val pass1  = view.findViewById<TextInputEditText>(R.id.editPass)
        val pass2  = view.findViewById<TextInputEditText>(R.id.editPass2)
        val btn    = view.findViewById<Button>(R.id.btnCreate)
        val bar    = view.findViewById<MaterialToolbar>(R.id.toolbar)

        bar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        btn.setOnClickListener {
            val f  = first.text?.toString()?.trim().orEmpty()
            val l  = last.text?.toString()?.trim().orEmpty()
            val e  = email.text?.toString()?.trim().orEmpty()
            val p1 = pass1.text?.toString().orEmpty()
            val p2 = pass2.text?.toString().orEmpty()

            if (f.isEmpty() || l.isEmpty() || e.isEmpty() || p1.length < 6) {
                Toast.makeText(requireContext(), "Fill all fields (password ≥ 6)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (p1 != p2) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btn.isEnabled = false

            auth.createUserWithEmailAndPassword(e, p1)
                .addOnSuccessListener { task ->
                    val uid = task.user?.uid ?: return@addOnSuccessListener

                    // Seed customers/{uid} now
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
                            Toast.makeText(requireContext(), "Account created.", Toast.LENGTH_SHORT).show()

                        }
                }
                .addOnFailureListener { err ->
                    Toast.makeText(requireContext(), err.localizedMessage ?: "Signup failed", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener { btn.isEnabled = true }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }
}
