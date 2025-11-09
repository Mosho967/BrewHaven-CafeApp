package com.brewhaven.app.ui.store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R

class StoreAdapter(
    private var items: List<MenuItemModel>,
    private val onClick: (MenuItemModel) -> Unit,
    private val showCategory: Boolean = false // kept for future, unused in slim row
) : RecyclerView.Adapter<StoreAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.name)
        val price: TextView = v.findViewById(R.id.price)
        val soldOut: TextView = v.findViewById(R.id.soldOut)
        val image: ImageView = v.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]
        h.name.text = item.name
        h.price.text = "£" + String.format("%.2f", item.price)

        h.soldOut.visibility = if (item.available) View.GONE else View.VISIBLE
        h.itemView.alpha = if (item.available) 1f else 0.5f

        // placeholder until we wire up images:
        // h.image.setImageResource(R.drawable.ic_image_placeholder)

        h.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<MenuItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
