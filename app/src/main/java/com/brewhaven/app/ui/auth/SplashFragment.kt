package com.brewhaven.app.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R

/**
 * SplashFragment
 *
 * Displays the initial splash screen while the app prepares navigation.
 * This fragment does not perform navigation itself; MainActivity or the
 * next fragment (e.g., WelcomeFragment or MenuFragment) handles the flow.
 *
 * Its only responsibility is UI presentation and ensuring the bottom
 * navigation bar stays hidden during the splash display.
 */
class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Splash should always hide bottom navigation
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }

    /**
     * Ensures the bottom navigation remains hidden when the user returns
     * to this fragment (e.g., due to lifecycle events).
     */
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }
}
