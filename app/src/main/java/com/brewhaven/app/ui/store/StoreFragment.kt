package com.brewhaven.app.ui.store

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R
import com.google.firebase.firestore.FirebaseFirestore

class StoreFragment : Fragment(R.layout.fragment_store) {

    private lateinit var adapter: StoreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<RecyclerView>(R.id.storeList)
        val progress = view.findViewById<View>(R.id.progress)
        val errorText = view.findViewById<TextView>(R.id.errorText)

        adapter = StoreAdapter(emptyList()) { item ->
            // TODO: navigate to an ItemDetailFragment with this item.id
            // For now, do nothing.
        }
        list.adapter = adapter

        progress.visibility = View.VISIBLE
        errorText.visibility = View.GONE

        FirebaseFirestore.getInstance()
            .collection("menu_items")
            .whereEqualTo("available", true)      // show only available items
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
                }.sortedBy { it.category + it.name }

                if (mapped.isEmpty()) {
                    errorText.text = "No items available."
                    errorText.visibility = View.VISIBLE
                } else {
                    adapter.submit(mapped)
                }
            }
            .addOnFailureListener { e ->
                progress.visibility = View.GONE
                errorText.text = "Failed to load menu: ${e.localizedMessage}"
                errorText.visibility = View.VISIBLE
            }
    }
}
