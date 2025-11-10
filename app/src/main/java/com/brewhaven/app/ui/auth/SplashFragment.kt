// SplashFragment.kt
package com.brewhaven.app.ui.auth

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.brewhaven.app.ui.store.MenuFragment
import com.brewhaven.app.BuildConfig
import com.google.firebase.auth.FirebaseAuth

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hide bottom nav on splash
        (activity as? MainActivity)?.setBottomNavVisible(false)

        // Dev-only: force sign out so you can test the flow repeatedly
        if (BuildConfig.DEBUG) {
            FirebaseAuth.getInstance().signOut()
        }

        // Play fade-in (don’t crash if anim missing)
        runCatching {
            val fade = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
            view.startAnimation(fade)
        }

        // Navigate after delay to Welcome (no user) or Menu (has user)
        // SplashFragment.kt
        view.postDelayed({
            val authed = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
            if (!authed) {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, WelcomeFragment())
                    .commit()
            } else {
                (activity as? MainActivity)?.apply {
                    setBottomNavVisible(true)
                    findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNav)
                        .selectedItemId = R.id.nav_menu   // triggers MainActivity tab switch
                }
            }
        }, 1600)

    }

    override fun onResume() {
        super.onResume()
        // Keep bottom nav hidden while splash is visible
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }
}
