package com.brewhaven.app.ui.store

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R
import com.google.firebase.firestore.FirebaseFirestore

class StoreListFragment : Fragment(R.layout.fragment_store_list) {

    companion object {
        private const val ARG_CATEGORY = "category"
        fun newInstance(category: String) = StoreListFragment().apply {
            arguments = Bundle().apply { putString(ARG_CATEGORY, category) }
        }
    }

    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var adapter: StoreAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = view.findViewById<TextView>(R.id.storeTitle)
        val empty = view.findViewById<TextView>(R.id.storeEmpty)
        val progress = view.findViewById<ProgressBar>(R.id.storeProgress)
        val list = view.findViewById<RecyclerView>(R.id.storeRecycler)

        val category = requireArguments().getString(ARG_CATEGORY) ?: ""
        title.text = category

        adapter = StoreAdapter(emptyList()) { item ->
            // open detail
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, ItemDetailFragment.newInstance(item))
                .addToBackStack(null)
                .commit()
        }
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter

        progress.visibility = View.VISIBLE
        empty.visibility = View.GONE

        db.collection("menu_items")
            .whereEqualTo("category", category)
            // show both available and unavailable; adapter already dims/labels
            .get()
            .addOnSuccessListener { snap ->
                val items = snap.documents.map { MenuItemModel.from(it) }
                adapter.submit(items)
                empty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                empty.text = "Failed: ${e.localizedMessage}"
                empty.visibility = View.VISIBLE
            }
            .addOnCompleteListener { progress.visibility = View.GONE }
    }
}
