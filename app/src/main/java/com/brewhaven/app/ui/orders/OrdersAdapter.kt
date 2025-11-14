package com.brewhaven.app.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R
import com.brewhaven.app.data.OrdersRepository
import java.text.SimpleDateFormat
import java.util.*

/**
 * OrdersAdapter
 *
 * RecyclerView adapter for displaying past order summaries.
 * Each row shows:
 *  - A shortened order ID
 *  - Total price
 *  - Item count, payment type, and formatted timestamp
 *
 * The adapter is UI-only. Business logic (loading, filtering, sorting orders)
 * lives inside [OrdersRepository] or the fragment using this adapter.
 *
 * @param onClick Triggered when a user taps an order to view its details.
 */
class OrdersAdapter(
    private val onClick: (OrdersRepository.OrderSummary) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.VH>() {

    // Backing dataset for order summaries
    private val data = mutableListOf<OrdersRepository.OrderSummary>()

    // Formatter used for displaying order timestamps
    private val fmt = SimpleDateFormat("dd MMM HH:mm", Locale.UK)

    /**
     * Replaces all current items with a new dataset.
     * Uses full notify since the list is small and updated infrequently.
     */
    fun submit(items: List<OrdersRepository.OrderSummary>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val o = data[pos]

        // Shortened readable order ID (last 6 chars)
        h.orderId.text = "#${o.id.takeLast(6)}"

        h.total.text = "£" + String.format("%.2f", o.total)

        // Summary line with item count, payment method, and formatted creation time
        h.subline.text =
            "${o.itemCount} items • ${o.paymentType} • ${fmt.format(Date(o.createdAt))}"

        // Navigate to order detail view
        h.itemView.setOnClickListener { onClick(o) }
    }

    override fun getItemCount() = data.size

    /**
     * ViewHolder binding layout for a single order row.
     */
    class VH(v: View): RecyclerView.ViewHolder(v) {
        val orderId: TextView = v.findViewById(R.id.orderId)
        val total: TextView = v.findViewById(R.id.total)
        val subline: TextView = v.findViewById(R.id.subline)
    }
}
