package com.brewhaven.app.ui.auth
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.brewhaven.app.R



class SplashFragment : Fragment(R.layout.fragment_splash) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // simple fade-in on splash root
        view.startAnimation(android.view.animation.AnimationUtils
            .loadAnimation(requireContext(), R.anim.fade_in))

        // 1.6s fade to Login
        view.postDelayed({
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fade_in,   // enter
                    R.anim.fade_out   // exit
                )
                .replace(android.R.id.content, LoginFragment())
                .commit()
        }, 1600)
    }
}