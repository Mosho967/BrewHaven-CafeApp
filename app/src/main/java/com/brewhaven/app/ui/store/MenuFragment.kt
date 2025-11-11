package com.brewhaven.app.ui.store

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore

class MenuFragment : Fragment(R.layout.fragment_menu) {

    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: MenuSectionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavVisible(true)


        val rv = view.findViewById<RecyclerView>(R.id.menuRecycler)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val rows = mutableListOf<MenuRow>().apply {
            add(MenuRow.Header("Drinks"))
            add(MenuRow.Category("Bottled Drinks", R.drawable.still_water_500ml, 0))
            add(MenuRow.Category("Espresso Drinks", R.drawable.espresso, 0))
            add(MenuRow.Category("Teas", R.drawable.green_tea, 0))
            add(MenuRow.Header("Food"))
            add(MenuRow.Category("Breakfast", R.drawable.bacon_roll, 0))
            add(MenuRow.Category("Sandwiches", R.drawable.blt_sandwich, 0))
            add(MenuRow.Category("Snacks", R.drawable.chocolate_chip_cookie, 0))
        }


        adapter = MenuSectionAdapter(rows) { categoryTitle ->
            parentFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, StoreListFragment.newInstance(categoryTitle))
                .addToBackStack(null)
                .commit()
        }


        rv.adapter = adapter

        // fetch counts in parallel
        rows.forEachIndexed { index, row ->
            if (row is MenuRow.Category) {
                db.collection("menu_items")
                    .whereEqualTo("category", row.title)
                    .count()
                    .get(AggregateSource.SERVER)
                    .addOnSuccessListener { snap ->
                        adapter.updateCountAt(index, snap.count.toInt())
                    }
                    .addOnFailureListener {

                    }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }
    override fun onStart() {
        super.onStart()
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }
}

// data for adapter
sealed class MenuRow {
    data class Header(val title: String) : MenuRow()
    data class Category(val title: String, val imageRes: Int, val count: Int) : MenuRow()
}
