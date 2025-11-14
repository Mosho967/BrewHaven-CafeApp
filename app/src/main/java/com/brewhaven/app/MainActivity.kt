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

/**
 * MainActivity
 *
 * Central activity managing:
 * - Authentication state routing (Splash → Welcome → Tabs)
 * - Persistent bottom navigation tabs
 * - Fragment lifecycle and tab switching
 * - User-bound repositories (cart, favourites, orders)
 *
 * The activity owns all top-level navigation flows so fragments stay simple.
 */
class MainActivity : AppCompatActivity() {

    // Fragment tags for tab persistence
    private val TAG_MENU   = "frag_menu"
    private val TAG_FAV    = "frag_fav"
    private val TAG_CART   = "frag_cart"
    private val TAG_ORDERS = "frag_orders"

    private lateinit var auth: FirebaseAuth

    /**
     * AuthStateListener
     *
     * Responds to login/logout events and routes the user to:
     * - Welcome screen (signed out)
     * - Tabbed UI (signed in)
     *
     * Rebinds repositories each time the authenticated user changes.
     */
    private val authListener = FirebaseAuth.AuthStateListener { fb ->
        val user = fb.currentUser

        if (user == null) {
            bindUserRepositories()    // clear repositories for null uid
            showWelcome()
        } else {
            bindUserRepositories()
            showTabs()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        /**
         * Handles tab switching.
         * Clears any deep navigation stack before switching to a tab.
         */
        bottom.setOnItemSelectedListener { item ->
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

        // Ignore reselect events (avoids refreshing fragments unnecessarily)
        bottom.setOnItemReselectedListener { }

        // Initial launch: show splash animation screen
        if (savedInstanceState == null) {
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

    // ------------------------------------------------------------
    // Public helpers used by fragments
    // ------------------------------------------------------------

    /**
     * Show or hide bottom navigation bar.
     */
    fun setBottomNavVisible(visible: Boolean) {
        findViewById<BottomNavigationView>(R.id.bottomNav).visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    fun selectTab(itemId: Int) {
        findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = itemId
    }

    /**
     * Called after successful Login/Signup to reset the fragment stack
     * and return to the Menu tab.
     */
    fun startAppFromAuth() {
        val fm = supportFragmentManager

        // Clear any auth flow fragments (Welcome, Login, Signup, etc.)
        fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        fm.commit {
            fm.fragments
                .filter { it.tag !in setOf(TAG_MENU, TAG_FAV, TAG_CART, TAG_ORDERS) }
                .forEach { remove(it) }
        }

        bindUserRepositories()

        setBottomNavVisible(true)
        selectTab(R.id.nav_menu)
    }

    /**
     * Triggered by the Menu toolbar sign-out action.
     * The auth listener handles the actual navigation change.
     */
    fun signOutToWelcome() {
        FirebaseAuth.getInstance().signOut()
    }

    /**
     * Binds repositories to current user uid or clears them if uid is null.
     * Also ensures the user's base Firestore profile exists.
     */
    fun bindUserRepositories() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        // Create/merge customer profile in Firestore on login
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

    // ------------------------------------------------------------
    // Internal navigation owned by MainActivity
    // ------------------------------------------------------------

    /**
     * Shows the Welcome screen for signed-out users.
     * Clears all fragments and hides bottom navigation.
     */
    private fun showWelcome() {
        val fm = supportFragmentManager
        fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        fm.commit {
            fm.fragments.forEach { remove(it) }
            replace(R.id.fragment_container, WelcomeFragment())
        }

        setBottomNavVisible(false)
    }

    /**
     * Shows the main tab UI for signed-in users.
     * Ensures tab fragments are created once and reused thereafter.
     */
    private fun showTabs() {
        val fm = supportFragmentManager
        fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Remove non-tab fragments (e.g. Splash, Welcome, Login, SignUp)
        fm.commit {
            fm.fragments
                .filter { it.tag !in setOf(TAG_MENU, TAG_FAV, TAG_CART, TAG_ORDERS) }
                .forEach { remove(it) }
        }

        // Ensure Menu fragment exists; create only once
        val menu = fm.findFragmentByTag(TAG_MENU) ?: MenuFragment().also {
            fm.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container, it, TAG_MENU)
            }
        }

        // Show only the Menu fragment initially, hide others
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

    /**
     * Switches between persistent tab fragments.
     * Creates them once and hides/shows as needed (no recreation).
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
            listOf(TAG_MENU, TAG_FAV, TAG_CART, TAG_ORDERS)
                .mapNotNull { fm.findFragmentByTag(it) }
                .forEach { frag ->
                    if (frag == target) show(frag) else hide(frag)
                }
        }

        setBottomNavVisible(true)
    }
}
