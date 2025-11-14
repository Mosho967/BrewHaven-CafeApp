package com.brewhaven.app.ui.store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R

/**
 * StoreAdapter
 *
 * RecyclerView adapter responsible for displaying menu items inside a
 * category-specific list (StoreListFragment).
 *
 * Responsibilities:
 * - Bind item name, price, availability, and image.
 * - Dim and mark items that are sold out.
 * - Forward item-click events to the parent fragment via [onClick].
 *
 * Uses [ImageResolver] to automatically map item names to drawable resources.
 */
class StoreAdapter(
    private var items: List<MenuItemModel>,
    private val onClick: (MenuItemModel) -> Unit,
    private val showCategory: Boolean = false // reserved for future UI enhancements
) : RecyclerView.Adapter<StoreAdapter.VH>() {

    /**
     * ViewHolder describing the menu card layout.
     */
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

    /**
     * Binds a single menu item card with name, price, and image.
     * Sold-out items appear dimmed and show a "Sold Out" label.
     */
    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]

        h.name.text = item.name
        h.price.text = "£" + String.format("%.2f", item.price)

        val ctx = h.itemView.context
        h.image.setImageResource(ImageResolver.imageResFor(ctx, item.name))

        // Availability formatting
        h.soldOut.visibility = if (item.available) View.GONE else View.VISIBLE
        h.itemView.alpha = if (item.available) 1f else 0.5f

        h.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    /**
     * Replaces the dataset and refreshes the list.
     * (Simple approach, sufficient here due to small datasets.)
     */
    fun submit(newItems: List<MenuItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
