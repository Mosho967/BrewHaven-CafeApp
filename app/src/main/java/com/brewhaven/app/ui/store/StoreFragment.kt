package com.brewhaven.app.ui.store

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.brewhaven.app.R
import com.google.firebase.firestore.FirebaseFirestore

class StoreFragment : Fragment(R.layout.fragment_store) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val label = view.findViewById<TextView?>(R.id.storeSubtitle) ?: return

        // Simple proof it works: load available items and print name + price
        FirebaseFirestore.getInstance()
            .collection("menu_items")
            .whereEqualTo("available", true)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    label.text = "No items yet."
                    return@addOnSuccessListener
                }

                val lines = snap.documents.map { doc ->
                    val name = doc.getString("name") ?: "Unnamed"
                    val price = (doc.getDouble("price") ?: 0.0)
                    val category = doc.getString("category") ?: ""
                    "$name — £${"%.2f".format(price)}  [$category]"
                }
                label.text = lines.joinToString("\n")
            }
            .addOnFailureListener { e ->
                label.text = "Failed to load menu: ${e.localizedMessage}"
            }
    }
}
