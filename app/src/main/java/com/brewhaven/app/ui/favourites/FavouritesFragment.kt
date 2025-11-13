package com.brewhaven.app.ui.favourites

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.brewhaven.app.data.FavoritesRepository
import com.brewhaven.app.ui.store.ItemDetailFragment
import com.brewhaven.app.ui.store.MenuItemModel
import com.google.firebase.firestore.FirebaseFirestore

class FavouritesFragment : Fragment(R.layout.fragment_favourites) {

    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: FavouritesAdapter
    private lateinit var list: RecyclerView
    private lateinit var empty: TextView
    private lateinit var progress: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list = view.findViewById(R.id.favList)
        empty = view.findViewById(R.id.emptyText)
        progress = view.findViewById(R.id.progress)

        list.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = FavouritesAdapter(
            onClick = { item ->
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        R.anim.fade_in, R.anim.fade_out,
                        R.anim.fade_in, R.anim.fade_out
                    )
                    .replace(R.id.fragment_container, ItemDetailFragment.newInstance(item))
                    .addToBackStack(null)
                    .commit()
            },
            onToggleHeart = { itemId ->
                FavoritesRepository.remove(itemId)
                // Repo listener calls back into render and update UI
            }
        )
        list.adapter = adapter

        // Initial paint with whatever repo has cached
        renderFromIds(FavoritesRepository.allIds())

        // Live updates
        FavoritesRepository.onChange = { ids ->
            renderFromIds(ids)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent leaking the fragment as a callback target
        if (FavoritesRepository.onChange != null) FavoritesRepository.onChange = null
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }

    private fun renderFromIds(ids: Set<String>) {
        if (ids.isEmpty()) {
            progress.visibility = View.GONE
            empty.visibility = View.VISIBLE
            adapter.submit(emptyList())
            return
        }

        progress.visibility = View.VISIBLE
        empty.visibility = View.GONE

        val results = mutableListOf<MenuItemModel>()
        val batches = ids.toList().chunked(10)
        var done = 0

        batches.forEach { chunk ->
            db.collection("menu_items")
                .whereIn("__name__", chunk)
                .get()
                .addOnSuccessListener { snap ->
                    results += snap.documents.map { d ->
                        MenuItemModel(
                            id = d.id,
                            name = d.getString("name") ?: "Unnamed",
                            price = d.getDouble("price") ?: 0.0,
                            category = d.getString("category") ?: "",
                            available = d.getBoolean("available") ?: true,
                            description = d.getString("description"),
                            calories = d.getDouble("calories"),
                            allergens = (d.get("allergens") as? List<*>)?.mapNotNull { it as? String }
                        )
                    }
                }
                .addOnCompleteListener {
                    done++
                    if (done == batches.size) {
                        progress.visibility = View.GONE
                        if (results.isEmpty()) {
                            empty.visibility = View.VISIBLE
                            adapter.submit(emptyList())
                        } else {
                            adapter.submit(results.sortedBy { it.name })
                        }
                    }
                }
        }
    }
}
