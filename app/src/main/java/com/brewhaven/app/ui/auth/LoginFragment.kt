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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide bottom nav on auth screens
        (activity as? MainActivity)?.setBottomNavVisible(false)

        val toolbar = view.findViewById<MaterialToolbar?>(R.id.toolbar)
        toolbar?.apply {
            title = "Sign in"
            setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        }

        val email = view.findViewById<EditText>(R.id.editEmail)
        val password = view.findViewById<EditText>(R.id.editPassword)
        val btnLogin = view.findViewById<Button>(R.id.buttonLoginSubmit)

        fun validate(): Boolean {
            var ok = true
            email.error = null
            password.error = null

            val e = email.text?.toString()?.trim().orEmpty()
            val p = password.text?.toString().orEmpty()

            if (e.isEmpty()) { email.error = "Required"; ok = false }
            else if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) { email.error = "Invalid email"; ok = false }

            if (p.isEmpty()) { password.error = "Required"; ok = false }
            return ok
        }

        btnLogin.setOnClickListener {
            if (!validate()) return@setOnClickListener

            val e = email.text.toString().trim()
            val p = password.text.toString()

            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(e, p)
                .addOnSuccessListener {
                    // Clear auth stack (no back to Welcome/Login)
                    parentFragmentManager.popBackStack(
                        null,
                        androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    // Show bottom nav and route via tab selection
                    (activity as? MainActivity)?.apply {
                        setBottomNavVisible(true)
                        findViewById<BottomNavigationView>(R.id.bottomNav)
                            .selectedItemId = R.id.nav_menu
                    }
                }
                .addOnFailureListener { err ->
                    Toast.makeText(
                        requireContext(),
                        err.localizedMessage ?: "Login failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnCompleteListener {
                    // Always re-enable
                    btnLogin.isEnabled = true
                }
        }
    }

    override fun onResume() {
        super.onResume()
        // Belt and braces: hide bar if we got here from anywhere weird
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }
}
