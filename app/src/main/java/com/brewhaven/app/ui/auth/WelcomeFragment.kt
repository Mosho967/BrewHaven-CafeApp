package com.brewhaven.app.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R

/**
 * WelcomeFragment
 *
 * Acts as the entry screen for unauthenticated users.
 * Provides navigation to the Login and Sign-Up flows using
 * fragment transactions with fade animations.
 *
 * This fragment intentionally hides the bottom navigation bar
 * since the user should not access app features before signing in.
 */
class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(false)

        val btnLogin  = view.findViewById<Button>(R.id.button)
        val btnSignUp = view.findViewById<Button>(R.id.buttonSignUp)

        // Navigate to login screen
        btnLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out
                )
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        // Navigate to sign-up screen
        btnSignUp.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out
                )
                .replace(R.id.fragment_container, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    /**
     * Ensures bottom navigation stays hidden when returning
     * to the welcome screen via lifecycle events.
     */
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }
}
