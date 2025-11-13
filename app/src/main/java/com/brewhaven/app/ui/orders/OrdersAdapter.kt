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

class OrdersAdapter(
    private val onClick: (OrdersRepository.OrderSummary) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.VH>() {

    private val data = mutableListOf<OrdersRepository.OrderSummary>()
    private val fmt = SimpleDateFormat("dd MMM HH:mm", Locale.UK)

    fun submit(items: List<OrdersRepository.OrderSummary>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_order_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val o = data[pos]
        h.orderId.text = "#${o.id.takeLast(6)}"
        h.total.text = "£" + String.format("%.2f", o.total)
        h.subline.text = "${o.itemCount} items • ${o.paymentType} • ${fmt.format(Date(o.createdAt))}"
        h.itemView.setOnClickListener { onClick(o) }
    }

    override fun getItemCount() = data.size

    class VH(v: View): RecyclerView.ViewHolder(v) {
        val orderId: TextView = v.findViewById(R.id.orderId)
        val total: TextView = v.findViewById(R.id.total)
        val subline: TextView = v.findViewById(R.id.subline)
    }
}
