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
    private val onClick: (MenuItemModel) -> Unit
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

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]

        h.name.text = item.name
        h.price.text = "£%.2f".format(item.price)

        // show "Sold Out" tag when not available
        if (!item.available) {
            h.soldOut.visibility = View.VISIBLE
            h.itemView.alpha = 0.5f
        } else {
            h.soldOut.visibility = View.GONE
            h.itemView.alpha = 1f
        }

        // TODO: set h.image.setImageResource(...) once mapping drawable → item is added

        h.itemView.setOnClickListener {
            if (item.available) onClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<MenuItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
