package com.brewhaven.app.ui.store

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R
import com.brewhaven.app.data.FavoritesRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath


class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: FavoriteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialToolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        }

        val list = view.findViewById<RecyclerView>(R.id.favList)
        val empty = view.findViewById<TextView>(R.id.emptyText)
        list.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = FavoriteAdapter(
            onClick = { item ->
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, ItemDetailFragment.newInstance(item))
                    .addToBackStack(null).commit()
            },
            onToggleHeart = { id ->
                FavoritesRepository.toggle(id)
                loadData(list, empty) // refresh after un-heart
            }
        )
        list.adapter = adapter

        loadData(list, empty)
    }

    private fun loadData(list: RecyclerView, empty: TextView) {
        val ids = FavoritesRepository.all()
        if (ids.isEmpty()) {
            adapter.submit(emptyList())
            empty.visibility = View.VISIBLE
            return
        }
        // Firestore whereIn supports 10 per call; batch if needed
        val chunks = ids.chunked(10)
        val results = mutableListOf<MenuItemModel>()
        var remaining = chunks.size
        chunks.forEach { chunk ->
            db.collection("menu_items")
                .whereIn(FieldPath.documentId(), chunk)
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
                    remaining--
                    if (remaining == 0) {
                        results.sortBy { it.name }
                        adapter.submit(results)
                        empty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
        }
    }
}
