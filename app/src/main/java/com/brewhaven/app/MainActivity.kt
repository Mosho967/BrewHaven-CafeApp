package com.brewhaven.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.brewhaven.app.data.CartRepository
import com.brewhaven.app.data.FavoritesRepository
import com.brewhaven.app.ui.auth.SplashFragment
import com.brewhaven.app.ui.auth.WelcomeFragment
import com.brewhaven.app.ui.cart.CartFragment
import com.brewhaven.app.ui.favourites.FavouritesFragment
import com.brewhaven.app.ui.orders.OrdersFragment
import com.brewhaven.app.ui.store.MenuFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions

class MainActivity : AppCompatActivity() {

    private val TAG_MENU   = "frag_menu"
    private val TAG_FAV    = "frag_fav"
    private val TAG_CART   = "frag_cart"
    private val TAG_ORDERS = "frag_orders"

    private lateinit var auth: FirebaseAuth

    private val authListener = FirebaseAuth.AuthStateListener { fb ->
        val user = fb.currentUser
        if (user == null) {
            // signed out
            bindUserRepositories() // clears with null
            showWelcome()
        } else {
            // signed in
            bindUserRepositories()
            showTabs()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.setOnItemSelectedListener { item ->
            // clear any pushed detail screens before switching tabs
            supportFragmentManager.popBackStack(
                null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            when (item.itemId) {
                R.id.nav_menu       -> switchTo(TAG_MENU)   { MenuFragment() }
                R.id.nav_favourites -> switchTo(TAG_FAV)    { FavouritesFragment() }
                R.id.nav_cart       -> switchTo(TAG_CART)   { CartFragment() }
                R.id.nav_orders     -> switchTo(TAG_ORDERS) { OrdersFragment() }
                else                -> switchTo(TAG_MENU)   { MenuFragment() }
            }
            true
        }
        bottom.setOnItemReselectedListener { /* no-op */ }

        if (savedInstanceState == null) {
            // Shows a Splash visual.
            setBottomNavVisible(false)
            supportFragmentManager.commit {
                replace(R.id.fragment_container, SplashFragment())
            }
        }
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(authListener)
    }

    // ============== Public helpers used by frags ==============

    fun setBottomNavVisible(visible: Boolean) {
        findViewById<BottomNavigationView>(R.id.bottomNav).visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    fun selectTab(itemId: Int) {
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = itemId
    }

    fun startAppFromAuth() {
        val fm = supportFragmentManager

        // Clear any leftover fragments from auth flow
        fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        fm.commit {
            fm.fragments
                .filter { it.tag !in setOf(TAG_MENU, TAG_FAV, TAG_CART, TAG_ORDERS) }
                .forEach { remove(it) }
        }

        // Rebinds user-specific repositories (Cart, Favourites, etc.)
        bindUserRepositories()

        // Show bottom navigation and switch to Menu tab
        setBottomNavVisible(true)
        selectTab(R.id.nav_menu)
    }

    /** Called by Menu toolbar “Sign out”. Do NOT navigate here; listener will handle it. */
    fun signOutToWelcome() {
        FirebaseAuth.getInstance().signOut()
        // authListener fires -> showWelcome()
    }

    /** Attach or clear repositories based on current user */
    fun bindUserRepositories() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        // Ensures customers/{uid} once logged in
        if (uid != null) {
            val doc = mapOf(
                "email"     to (user.email ?: ""),
                "isActive"  to true,
                "createdAt" to System.currentTimeMillis()
            )
            com.brewhaven.app.data.FirePaths.customer(uid)
                .set(doc, SetOptions.merge())
        }

        CartRepository.bindUser(uid)
        FavoritesRepository.bindUser(uid)
        com.brewhaven.app.data.OrdersRepository.bindUser(uid)
    }

    // ============== Internal UI routing owned by MainActivity ==============

    private fun showWelcome() {
        val fm = supportFragmentManager
        fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        fm.commit {
            fm.fragments.forEach { remove(it) }
            replace(R.id.fragment_container, WelcomeFragment())
        }
        setBottomNavVisible(false)
    }

    private fun showTabs() {
        val fm = supportFragmentManager
        fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Remove any non-tab/auth fragments (Splash, Welcome, Login, SignUp, etc.)
        fm.commit {
            fm.fragments
                .filter { it.tag !in setOf(TAG_MENU, TAG_FAV, TAG_CART, TAG_ORDERS) }
                .forEach { remove(it) }
        }

        // Ensure Menu fragment exists
        val menu = fm.findFragmentByTag(TAG_MENU) ?: MenuFragment().also {
            fm.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container, it, TAG_MENU)
            }
        }

        // Hides others, show menu by default
        fm.commit {
            setReorderingAllowed(true)
            listOf(TAG_MENU, TAG_FAV, TAG_CART, TAG_ORDERS)
                .mapNotNull { fm.findFragmentByTag(it) }
                .forEach { frag ->
                    if (frag == menu) show(frag) else hide(frag)
                }
        }

        setBottomNavVisible(true)
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = R.id.nav_menu
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
            listOf(TAG_MENU, TAG_FAV, TAG_CART, TAG_ORDERS)
                .mapNotNull { fm.findFragmentByTag(it) }
                .forEach { frag -> if (frag == target) show(frag) else hide(frag) }
        }
        setBottomNavVisible(true)
    }
}
