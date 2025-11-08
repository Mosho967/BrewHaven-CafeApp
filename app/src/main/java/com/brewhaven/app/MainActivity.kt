package com.brewhaven.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.brewhaven.app.ui.store.MenuFragment
import com.brewhaven.app.ui.favourites.FavouritesFragment
import com.brewhaven.app.ui.cart.CartFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.setOnItemSelectedListener { item ->
            val frag = when (item.itemId) {
                R.id.nav_menu -> MenuFragment()
                R.id.nav_favourites -> FavouritesFragment()
                R.id.nav_cart -> CartFragment()
                else -> MenuFragment()
            }
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, frag)
                .commit()
            true
        }

        if (savedInstanceState == null) {
            bottom.selectedItemId = R.id.nav_menu
        }
    }
}
