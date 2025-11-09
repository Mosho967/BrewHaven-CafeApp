package com.brewhaven.app.data

import com.brewhaven.app.ui.store.MenuItemModel

data class CartItem(val item: MenuItemModel, var qty: Int)

object CartRepository {
    private val items = linkedMapOf<String, CartItem>() // key = item.id

    fun all(): List<CartItem> = items.values.toList()

    fun add(item: MenuItemModel, qty: Int) {
        val cur = items[item.id]?.qty ?: 0
        items[item.id] = CartItem(item, (cur + qty).coerceIn(1, 20))
    }

    fun setQty(itemId: String, qty: Int) {
        items[itemId]?.qty = qty.coerceIn(1, 20)
    }

    fun remove(itemId: String) {
        items.remove(itemId)
    }

    fun clear() {
        items.clear()
    }

    fun total(): Double =
        items.values.sumOf { it.item.price * it.qty }
}
