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

/**
 * FavouritesFragment
 *
 * Displays the user's list of favourite items.
 * The fragment renders its UI directly from the set of favourite item IDs held
 * in [FavoritesRepository], then queries Firestore to retrieve full item data.
 *
 * Key points:
 * - Uses repository callbacks to stay in sync with favourite changes.
 * - Performs Firestore lookups in batches due to the 10-item limit of `whereIn`.
 * - Shows loading, empty state, and populated state depending on the repository.
 * - Navigates to [ItemDetailFragment] when a favourite item is tapped.
 */
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

        // 2-column grid for favourite cards
        list.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = FavouritesAdapter(
            onClick = { item ->
                // Open item details
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
                // Remove favourite; UI update handled through repository callback
                FavoritesRepository.remove(itemId)
            }
        )
        list.adapter = adapter

        // Initial render using whatever IDs are cached
        renderFromIds(FavoritesRepository.allIds())

        // Live updates from the repository
        FavoritesRepository.onChange = { ids ->
            renderFromIds(ids)
        }
    }

    /**
     * Clear callback binding to avoid leaking the fragment after view destruction.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        if (FavoritesRepository.onChange != null) FavoritesRepository.onChange = null
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }

    /**
     * Renders the favourites list based on a set of item IDs.
     * Handles:
     * - empty state
     * - loading state
     * - Firestore lookups in batches (because `whereIn` max = 10)
     */
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

        // Firestore whereIn supports max 10 IDs -> chunking required
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

                    // When all batches finish, update UI once
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
