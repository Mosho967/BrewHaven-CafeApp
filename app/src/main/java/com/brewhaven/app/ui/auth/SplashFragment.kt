package com.brewhaven.app.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.brewhaven.app.R

class SplashFragment : Fragment(R.layout.fragment_splash) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Optional fade-in if you have res/anim/fade_in
        val fade = android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        view.startAnimation(fade)

        // After 1.6s, go to Welcome. Do NOT addToBackStack for splash.
        view.postDelayed({
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fade_in, R.anim.fade_out,   // enter/exit
                    R.anim.fade_in, R.anim.fade_out    // popEnter/popExit (future back)
                )
                .replace(R.id.fragment_container, WelcomeFragment())
                .commit()
        }, 1600)
    }
}
