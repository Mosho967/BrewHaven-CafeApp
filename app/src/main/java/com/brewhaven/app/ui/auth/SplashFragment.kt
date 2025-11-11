package com.brewhaven.app.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

// leave false in normal runs
private const val DEV_FORCE_SIGN_OUT = false

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(false)

        if (DEV_FORCE_SIGN_OUT) FirebaseAuth.getInstance().signOut()

        view.post {
            val authed = FirebaseAuth.getInstance().currentUser != null
            if (!authed) {
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, WelcomeFragment())
                    .commit()
            } else {
                // Drive tab UI and remove Splash so it can’t pop back
                requireActivity()
                    .findViewById<BottomNavigationView>(R.id.bottomNav)
                    .selectedItemId = R.id.nav_menu
                (activity as? MainActivity)?.setBottomNavVisible(true)

                parentFragmentManager.beginTransaction()
                    .remove(this@SplashFragment)
                    .commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(false)
    }
}
