package com.brewhaven.app.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.brewhaven.app.R

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = view.findViewById<EditText>(R.id.editEmail)
        val password = view.findViewById<EditText>(R.id.editPassword)
        val btnLogin = view.findViewById<Button>(R.id.buttonLoginSubmit)

        btnLogin.setOnClickListener {
            val e = email.text.toString().trim()
            val p = password.text.toString()

            if (e.isEmpty() || p.isEmpty()) {

                password.error = if (p.isEmpty()) "Required" else null
                email.error = if (e.isEmpty()) "Required" else null
                return@setOnClickListener
            }

            // TODO: swap with your real auth
            parentFragmentManager.popBackStack()
        }
    }
}
