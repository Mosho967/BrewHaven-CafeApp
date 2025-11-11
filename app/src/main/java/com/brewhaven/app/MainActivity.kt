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


    private val TAG_MENU = "frag_menu"
    private val TAG_FAV  = "frag_fav"
    private val TAG_CART = "frag_cart"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottom.setOnItemSelectedListener { item ->
            setBottomNavVisible(true)
            when (item.itemId) {
                R.id.nav_menu       -> switchTo(TAG_MENU) { MenuFragment() }
                R.id.nav_favourites -> switchTo(TAG_FAV)  { FavouritesFragment() }
                R.id.nav_cart       -> switchTo(TAG_CART) { CartFragment() }
                else                -> switchTo(TAG_MENU) { MenuFragment() }
            }
            true
        }
        bottom.setOnItemReselectedListener { item ->

            setBottomNavVisible(true)
            when (item.itemId) {
                R.id.nav_menu       -> switchTo(TAG_MENU) { MenuFragment() }
                R.id.nav_favourites -> switchTo(TAG_FAV)  { FavouritesFragment() }
                R.id.nav_cart       -> switchTo(TAG_CART) { CartFragment() }
            }
        }



        if (savedInstanceState == null) {

            setBottomNavVisible(false)
            supportFragmentManager.commit {
                replace(R.id.fragment_container, SplashFragment())
            }
        }
    }

    /** Show or hide the bottom nav from anywhere */
    fun setBottomNavVisible(visible: Boolean) {
        findViewById<BottomNavigationView>(R.id.bottomNav).visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    fun selectTab(itemId: Int) {
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = itemId
    }
    private inline fun switchTo(tag: String, factory: () -> Fragment) {
        val fm = supportFragmentManager


        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)


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
                .forEach { hide(it) }
            show(target)
        }

        setBottomNavVisible(true)
    }
}
