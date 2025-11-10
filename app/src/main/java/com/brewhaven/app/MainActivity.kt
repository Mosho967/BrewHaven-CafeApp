package com.brewhaven.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.brewhaven.app.ui.auth.SplashFragment
import com.brewhaven.app.ui.cart.CartFragment
import com.brewhaven.app.ui.favourites.FavouritesFragment
import com.brewhaven.app.ui.store.MenuFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // Tags so we can find/show existing fragments
    private val TAG_MENU = "frag_menu"
    private val TAG_FAV  = "frag_fav"
    private val TAG_CART = "frag_cart"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_menu       -> switchTo(TAG_MENU)       { MenuFragment() }
                R.id.nav_favourites -> switchTo(TAG_FAV)        { FavouritesFragment() }
                R.id.nav_cart       -> switchTo(TAG_CART)       { CartFragment() }
                else                -> switchTo(TAG_MENU)       { MenuFragment() }
            }
            true
        }

        if (savedInstanceState == null) {
            // Start with splash; hide bottom bar until we land on a main tab
            setBottomNavVisible(false)
            supportFragmentManager.commit {
                replace(R.id.fragment_container, SplashFragment())
            }
        }
    }

    /** Show or hide the bottom nav safely from anywhere */
    fun setBottomNavVisible(visible: Boolean) {
        findViewById<BottomNavigationView>(R.id.bottomNav).visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    /**
     * Show a fragment by tag. If it doesn't exist, create it with [factory].
     * Hides all other main-tab fragments to preserve state.
     */
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
            // Hide all known tab fragments
            listOf(TAG_MENU, TAG_FAV, TAG_CART)
                .mapNotNull { fm.findFragmentByTag(it) }
                .forEach { hide(it) }
            // Show the target
            show(target)
        }

        // We’re in a main tab now; ensure bottom bar is visible
        setBottomNavVisible(true)
    }
}
