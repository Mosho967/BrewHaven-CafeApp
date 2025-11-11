package com.brewhaven.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.brewhaven.app.ui.auth.SplashFragment
import com.brewhaven.app.ui.auth.WelcomeFragment
import com.brewhaven.app.ui.cart.CartFragment
import com.brewhaven.app.ui.favourites.FavouritesFragment
import com.brewhaven.app.ui.store.MenuFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private val TAG_MENU = "frag_menu"
    private val TAG_FAV  = "frag_fav"
    private val TAG_CART = "frag_cart"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottom.setOnItemSelectedListener { item ->
            // Clear any pushed detail screens before switching tabs
            supportFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )

            when (item.itemId) {
                R.id.nav_menu       -> switchTo(TAG_MENU) { MenuFragment() }
                R.id.nav_favourites -> switchTo(TAG_FAV)  { FavouritesFragment() }
                R.id.nav_cart       -> switchTo(TAG_CART) { CartFragment() }
                else                -> switchTo(TAG_MENU) { MenuFragment() }
            }
            true
        }
        bottom.setOnItemReselectedListener { /* no-op */ }

        if (savedInstanceState == null) {
            setBottomNavVisible(false)
            supportFragmentManager.commit {
                replace(R.id.fragment_container, SplashFragment())
            }
        }
    }

    fun setBottomNavVisible(visible: Boolean) {
        findViewById<BottomNavigationView>(R.id.bottomNav).visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    fun selectTab(itemId: Int) {
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = itemId
    }

    /** Called by Login/Signup on success. */
    fun startAppFromAuth() {
        val fm = supportFragmentManager

        // 1) Obliterate any auth stack
        fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // 2) Remove any non-tab fragments (like Welcome, Splash, random detail)
        fm.commit {
            fm.fragments
                .filter { it.tag !in setOf(TAG_MENU, TAG_FAV, TAG_CART) }
                .forEach { remove(it) }
        }

        // 3) Hard-replace with Menu as the visible root so nothing masks it
        fm.commit {
            replace(R.id.fragment_container, MenuFragment(), TAG_MENU)
        }

        // 4) Reveal bar and sync the tab UI
        setBottomNavVisible(true)
        selectTab(R.id.nav_menu)
    }

    /** Called by Menu toolbar “Sign out”. */
    fun signOutToWelcome() {
        FirebaseAuth.getInstance().signOut()

        val fm = supportFragmentManager
        fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Remove every fragment so we truly start fresh
        fm.commit {
            fm.fragments.forEach { remove(it) }
        }

        fm.commit {
            replace(R.id.fragment_container, WelcomeFragment())
        }
        setBottomNavVisible(false)
    }

    private inline fun switchTo(tag: String, factory: () -> Fragment) {
        val fm = supportFragmentManager

        val target = fm.findFragmentByTag(tag) ?: factory().also {
            fm.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container, it, tag)
            }
        }

        fm.commit {
            setReorderingAllowed(true)
            listOf(TAG_MENU, TAG_FAV, TAG_CART)
                .mapNotNull { fm.findFragmentByTag(it) }
                .forEach { frag -> if (frag == target) show(frag) else hide(frag) }
        }

        setBottomNavVisible(true)
    }
}
