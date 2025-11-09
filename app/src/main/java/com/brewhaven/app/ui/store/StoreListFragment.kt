package com.brewhaven.app.ui.store

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R
import com.google.firebase.firestore.FirebaseFirestore

class StoreListFragment : Fragment(R.layout.fragment_store_list) {

    companion object {
        private const val ARG_CATEGORY = "arg_category"

        fun newInstance(category: String) = StoreListFragment().apply {
            arguments = Bundle().apply { putString(ARG_CATEGORY, category) }
        }
    }

    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: StoreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = requireArguments().getString(ARG_CATEGORY)
            ?: error("Category missing")

        val list = view.findViewById<RecyclerView>(R.id.storeList)
        val progress = view.findViewById<View>(R.id.progress)
        val errorText = view.findViewById<TextView>(R.id.errorText)

        adapter = StoreAdapter(
            emptyList(),
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
            showCategory = false
        )

        list.adapter = adapter

        progress.visibility = View.VISIBLE
        errorText.visibility = View.GONE

        // Load ALL items for this category (available + sold-out)
        db.collection("menu_items")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { snap ->
                progress.visibility = View.GONE

                val mapped = snap.documents.map { d ->
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
                // Sort: available first, then by name
                val sorted = mapped.sortedWith(
                    compareByDescending<MenuItemModel> { it.available }.thenBy { it.name }
                )

                if (sorted.isEmpty()) {
                    errorText.text = "No items in $category yet."
                    errorText.visibility = View.VISIBLE
                } else {
                    adapter.submit(sorted)
                }
            }
            .addOnFailureListener { e ->
                progress.visibility = View.GONE
                errorText.text = "Failed to load $category: ${e.localizedMessage}"
                errorText.visibility = View.VISIBLE
            }
    }
}
