package com.brewhaven.app.ui.favourites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R
import com.brewhaven.app.ui.store.MenuItemModel

/**
 * FavouritesAdapter
 *
 * Displays the user's favourite menu items in a card grid.
 * Each card shows the item image, name, price, and a heart icon
 * that allows users to toggle the favourite state.
 *
 * All business logic (adding/removing favourites) is handled outside
 * the adapter via the provided callbacks, keeping this class UI-focused.
 *
 * @param onClick           Triggered when a user taps the card to open details.
 * @param onToggleHeart     Triggered when the favourite heart is tapped.
 */
class FavouritesAdapter(
    private val onClick: (MenuItemModel) -> Unit,
    private val onToggleHeart: (String) -> Unit
) : RecyclerView.Adapter<FavouritesAdapter.VH>() {

    // Current list of items the adapter should display
    private var items: List<MenuItemModel> = emptyList()

    /**
     * ViewHolder representing one favourite card.
     */
    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.image)
        val name: TextView = v.findViewById(R.id.name)
        val price: TextView = v.findViewById(R.id.price)
        val btnHeart: ImageView = v.findViewById(R.id.btnHeart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]

        h.name.text = item.name
        h.price.text = "£" + String.format("%.2f", item.price)
        h.image.setImageResource(nameToDrawable(h.itemView, item.name))

        // Open details screen
        h.itemView.setOnClickListener { onClick(item) }

        // Toggle favourite status
        h.btnHeart.setOnClickListener { onToggleHeart(item.id) }
    }

    override fun getItemCount() = items.size

    /**
     * Updates the adapter dataset and refreshes the view.
     */
    fun submit(newItems: List<MenuItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    /**
     * Converts item names into drawable resource identifiers.
     * Falls back to a placeholder if no matching drawable exists.
     */
    private fun nameToDrawable(v: View, name: String): Int {
        val slug = name.lowercase()
            .replace("&", "and")
            .replace("[^a-z0-9]+".toRegex(), "_")
            .trim('_')

        val res = v.resources.getIdentifier(slug, "drawable", v.context.packageName)
        return if (res != 0) res else R.drawable.ic_image_placeholder
    }
}
