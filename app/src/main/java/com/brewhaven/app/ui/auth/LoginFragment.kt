package com.brewhaven.app.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.brewhaven.app.R
import com.brewhaven.app.ui.store.MenuFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = view.findViewById<EditText>(R.id.editEmail)
        val password = view.findViewById<EditText>(R.id.editPassword)
        val btnLogin = view.findViewById<Button>(R.id.buttonLoginSubmit)

        btnLogin.setOnClickListener {
            val e = email.text?.toString()?.trim().orEmpty()
            val p = password.text?.toString().orEmpty()

            var invalid = false
            if (e.isEmpty()) { email.error = "Required"; invalid = true }
            if (p.isEmpty()) { password.error = "Required"; invalid = true }
            if (invalid) return@setOnClickListener

            btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener { task ->
                    btnLogin.isEnabled = true
                    if (task.isSuccessful) {

                        parentFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                            .replace(R.id.fragment_container, MenuFragment())
                            .commit()

                    } else {
                        val msg = task.exception?.localizedMessage ?: "Login failed"
                        Snackbar.make(view, msg, Snackbar.LENGTH_LONG).show()
                    }
                }
        }
    }
}
