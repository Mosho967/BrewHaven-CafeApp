package com.brewhaven.app.ui.store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R

class StoreAdapter(
    private var items: List<MenuItemModel>,
    private val onClick: (MenuItemModel) -> Unit
) : RecyclerView.Adapter<StoreAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.name)
        val price: TextView = v.findViewById(R.id.price)
        val category: TextView = v.findViewById(R.id.category)
        val description: TextView = v.findViewById(R.id.description)
        val nutrition: TextView = v.findViewById(R.id.nutrition)
        val allergens: TextView = v.findViewById(R.id.allergens)
        val soldOut: TextView = v.findViewById(R.id.soldOut)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]

        h.name.text = item.name
        h.price.text = "£" + String.format("%.2f", item.price)
        h.category.text = item.category
        h.description.text = item.description ?: ""
        h.description.visibility = if (item.description.isNullOrBlank()) View.GONE else View.VISIBLE

        if (item.calories != null) {
            h.nutrition.text = "${item.calories.toInt()} kcal"
            h.nutrition.visibility = View.VISIBLE
        } else {
            h.nutrition.visibility = View.GONE
        }

        val chips = item.allergens?.filter { s -> s.isNotBlank() } ?: emptyList()
        if (chips.isNotEmpty()) {
            h.allergens.text = "Allergens: " + chips.joinToString(", ")
            h.allergens.visibility = View.VISIBLE
        } else {
            h.allergens.visibility = View.GONE
        }

        h.soldOut.visibility = if (item.available) View.GONE else View.VISIBLE
        h.itemView.alpha = if (item.available) 1f else 0.5f

        h.itemView.setOnClickListener { onClick(item) }
    }


    override fun getItemCount() = items.size

    fun submit(newItems: List<MenuItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
