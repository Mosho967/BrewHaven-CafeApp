package com.brewhaven.app.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R
import com.brewhaven.app.data.CartRepository

/**
 * CartAdapter
 *
 * RecyclerView adapter responsible for displaying the list of items
 * currently in the user's cart. Each row shows item details along with
 * controls to increase quantity, decrease quantity, or remove the item.
 *
 * The adapter delegates all business logic through the callback
 * lambdas (`onPlus`, `onMinus`, `onRemove`), ensuring UI-only
 * responsibility and keeping cart logic inside the repository.
 *
 * @param onPlus   Called when the user taps the “+” button for a line item.
 * @param onMinus  Called when the user taps the “–” button for a line item.
 * @param onRemove Called when the user taps the remove button for a line item.
 * @param nameToDrawable Function converting an item name to its drawable resource.
 */
class CartAdapter(
    private val onPlus: (CartRepository.Line) -> Unit,
    private val onMinus: (CartRepository.Line) -> Unit,
    private val onRemove: (CartRepository.Line) -> Unit,
    private val nameToDrawable: (String) -> Int
) : RecyclerView.Adapter<CartAdapter.VH>() {

    // Backing list for current cart lines
    private val data = mutableListOf<CartRepository.Line>()

    /**
     * Replaces the current dataset with the latest cart contents.
     * Notifies the adapter to redraw the list.
     */
    fun submit(items: List<CartRepository.Line>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val line = data[position]

        holder.title.text = line.name
        holder.price.text = "£" + String.format("%.2f", line.price)
        holder.qty.text = line.qty.toString()
        holder.image.setImageResource(nameToDrawable(line.name))

        // Pass line-level events back to the fragment for processing
        holder.btnPlus.setOnClickListener { onPlus(line) }
        holder.btnMinus.setOnClickListener { onMinus(line) }
        holder.btnRemove.setOnClickListener { onRemove(line) }
    }

    override fun getItemCount(): Int = data.size

    /**
     * ViewHolder binding a single cart row.
     */
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.image)
        val title: TextView = v.findViewById(R.id.title)
        val price: TextView = v.findViewById(R.id.price)
        val qty: TextView = v.findViewById(R.id.qty)
        val btnPlus: ImageButton = v.findViewById(R.id.btnPlus)
        val btnMinus: ImageButton = v.findViewById(R.id.btnMinus)
        val btnRemove: ImageButton = v.findViewById(R.id.btnRemove)
    }
}
