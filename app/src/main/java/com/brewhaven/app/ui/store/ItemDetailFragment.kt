package com.brewhaven.app.ui.store

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.brewhaven.app.R

class ItemDetailFragment : Fragment(R.layout.fragment_item_detail) {

    companion object {
        private const val ARG_ITEM = "arg_item"
        fun newInstance(item: MenuItemModel) = ItemDetailFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_ITEM, item) }
        }
    }

    private lateinit var item: MenuItemModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Type-safe read
        item = requireArguments().getParcelable(ARG_ITEM, MenuItemModel::class.java)
            ?: error("ItemDetailFragment missing ARG_ITEM")

        view.findViewById<TextView>(R.id.title).text = item.name
        view.findViewById<TextView>(R.id.price).text = "£" + String.format(java.util.Locale.UK, "%.2f", item.price)
        view.findViewById<TextView>(R.id.description).apply {
            text = item.description.orEmpty()
            visibility = if (item.description.isNullOrBlank()) View.GONE else View.VISIBLE
        }
        view.findViewById<TextView>(R.id.kcal).apply {
            val kcal = item.calories?.toInt()
            text = if (kcal != null) "$kcal kcal" else ""
            visibility = if (kcal != null) View.VISIBLE else View.GONE
        }
        view.findViewById<TextView>(R.id.allergens).apply {
            val list = item.allergens?.filter { it.isNotBlank() }.orEmpty()
            text = if (list.isNotEmpty()) "Allergens: ${list.joinToString(", ")}" else ""
            visibility = if (list.isNotEmpty()) View.VISIBLE else View.GONE
        }

        view.findViewById<Button>(R.id.btnAddToCart).setOnClickListener {
            // TODO hook into cart
        }
        view.findViewById<Button>(R.id.btnFavorite).setOnClickListener {
            // TODO toggle favorite
        }
    }
}
