// com.brewhaven.app.data.CartRepository
package com.brewhaven.app.data

import com.brewhaven.app.ui.store.MenuItemModel

object CartRepository {
    data class Line(val item: MenuItemModel, var qty: Int)

    private val lines = mutableListOf<Line>()

    fun add(item: MenuItemModel, qty: Int) {
        val existing = lines.firstOrNull { it.item.id == item.id }
        if (existing != null) existing.qty += qty
        else lines += Line(item, qty)
    }

    fun inc(itemId: String) { lines.firstOrNull { it.item.id == itemId }?.apply { qty++ } }
    fun dec(itemId: String) {
        lines.firstOrNull { it.item.id == itemId }?.apply {
            qty--; if (qty <= 0) lines.remove(this)
        }
    }
    fun remove(itemId: String) { lines.removeAll { it.item.id == itemId } }
    fun clear() { lines.clear() }

    fun items(): List<Line> = lines.toList()
    fun total(): Double = lines.sumOf { it.item.price * it.qty }
}
