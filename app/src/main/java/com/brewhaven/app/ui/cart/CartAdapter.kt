package com.brewhaven.app.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R
import com.brewhaven.app.ui.store.MenuItemModel

class CartAdapter(
    private val onPlus: (MenuItemModel) -> Unit,
    private val onMinus: (MenuItemModel) -> Unit,
    private val onRemove: (MenuItemModel) -> Unit,
    private val nameToDrawable: (String) -> Int
) : RecyclerView.Adapter<CartAdapter.VH>() {

    data class Row(val item: MenuItemModel, val qty: Int, val lineTotal: Double)

    private var rows: List<Row> = emptyList()

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.image)
        val name: TextView = v.findViewById(R.id.name)
        val price: TextView = v.findViewById(R.id.price)
        val qty: TextView = v.findViewById(R.id.qty)
        val btnPlus: View = v.findViewById(R.id.btnPlus)
        val btnMinus: View = v.findViewById(R.id.btnMinus)
        val btnRemove: View = v.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val row = rows[pos]
        h.name.text = row.item.name
        h.qty.text = row.qty.toString()
        h.price.text = "£" + String.format("%.2f", row.lineTotal)
        h.image.setImageResource(nameToDrawable(row.item.name))

        h.btnPlus.setOnClickListener { onPlus(row.item) }
        h.btnMinus.setOnClickListener { onMinus(row.item) }
        h.btnRemove.setOnClickListener { onRemove(row.item) }
    }

    override fun getItemCount() = rows.size

    fun submit(lines: List<Pair<MenuItemModel, Int>>) {
        rows = lines.map { (item, q) -> Row(item, q, item.price * q) }
        notifyDataSetChanged()
    }
}
