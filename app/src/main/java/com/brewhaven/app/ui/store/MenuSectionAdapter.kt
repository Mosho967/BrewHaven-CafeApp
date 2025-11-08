package com.brewhaven.app.ui.store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.R

class MenuSectionAdapter(
    private val rows: MutableList<MenuRow>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CATEGORY = 1
    }

    override fun getItemViewType(position: Int): Int =
        when (rows[position]) {
            is MenuRow.Header -> TYPE_HEADER
            is MenuRow.Category -> TYPE_CATEGORY
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val v = inf.inflate(R.layout.item_menu_header, parent, false)
            HeaderVH(v)
        } else {
            val v = inf.inflate(R.layout.item_menu_category, parent, false)
            CategoryVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is MenuRow.Header -> (holder as HeaderVH).bind(row)
            is MenuRow.Category -> (holder as CategoryVH).bind(row, onCategoryClick)
        }
    }

    override fun getItemCount(): Int = rows.size

    fun updateCountAt(index: Int, newCount: Int) {
        val row = rows.getOrNull(index) as? MenuRow.Category ?: return
        rows[index] = row.copy(count = newCount)
        notifyItemChanged(index)
    }

    class HeaderVH(v: View) : RecyclerView.ViewHolder(v) {
        private val title = v.findViewById<TextView>(R.id.headerTitle)
        fun bind(m: MenuRow.Header) { title.text = m.title }
    }

    class CategoryVH(v: View) : RecyclerView.ViewHolder(v) {
        private val img = v.findViewById<ImageView>(R.id.image)
        private val title = v.findViewById<TextView>(R.id.title)
        private val count = v.findViewById<TextView>(R.id.count)

        fun bind(m: MenuRow.Category, click: (String) -> Unit) {
            img.setImageResource(m.imageRes)
            title.text = m.title
            count.text = "(${m.count})"
            itemView.setOnClickListener { click(m.title) }
        }
    }
}
