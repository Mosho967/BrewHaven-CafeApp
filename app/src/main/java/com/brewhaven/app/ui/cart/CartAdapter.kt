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

class CartAdapter(
    private val onPlus: (CartRepository.Line) -> Unit,
    private val onMinus: (CartRepository.Line) -> Unit,
    private val onRemove: (CartRepository.Line) -> Unit,
    private val nameToDrawable: (String) -> Int
) : RecyclerView.Adapter<CartAdapter.VH>() {

    private val data = mutableListOf<CartRepository.Line>()

    fun submit(items: List<CartRepository.Line>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_cart_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val line = data[position]
        holder.title.text = line.name
        holder.price.text = "£" + String.format("%.2f", line.price)
        holder.qty.text = line.qty.toString()
        holder.image.setImageResource(nameToDrawable(line.name))

        holder.btnPlus.setOnClickListener { onPlus(line) }
        holder.btnMinus.setOnClickListener { onMinus(line) }
        holder.btnRemove.setOnClickListener { onRemove(line) }
    }

    override fun getItemCount(): Int = data.size

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
