// com.brewhaven.app.data.CartRepository
package com.brewhaven.app.data

import com.brewhaven.app.ui.store.MenuItemModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

object CartRepository {

    data class Line(
        val itemId: String,
        val name: String,
        val price: Double,
        var qty: Int
    )

    private val db by lazy { FirebaseFirestore.getInstance() }
    private var uid: String? = null
    private var reg: ListenerRegistration? = null

    // local in-memory cache
    private val lines = linkedMapOf<String, Line>()

    var onChange: ((List<Line>) -> Unit)? = null

    // ---------- BIND ----------
    fun bindUser(userId: String?) {
        if (uid == userId) return
        reg?.remove(); reg = null
        uid = userId
        lines.clear()

        if (uid == null) {
            onChange?.invoke(emptyList())
            return
        }

        reg = cartCol().addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener
            lines.clear()
            for (d in snap.documents) {
                val id = d.id
                val name = d.getString("name") ?: ""
                val price = d.getDouble("price") ?: 0.0
                val qty = (d.getLong("qty") ?: 0L).toInt()
                if (qty > 0) lines[id] = Line(id, name, price, qty)
            }
            onChange?.invoke(items())
        }
    }

    // ---------- API ----------
    fun items(): List<Line> = lines.values.toList()
    fun total(): Double = lines.values.sumOf { it.price * it.qty }

    fun add(item: MenuItemModel, qty: Int) {
        ensureBound()
        val doc = cartCol().document(item.id)
        doc.set(
            mapOf(
                "name" to item.name,
                "price" to item.price,
                "qty" to FieldValue.increment(qty.toLong()),
                "addedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        )
    }

    fun inc(itemId: String) {
        ensureBound()
        cartCol().document(itemId).update("qty", FieldValue.increment(1))
    }

    fun dec(itemId: String) {
        ensureBound()
        val current = lines[itemId]?.qty ?: 0
        if (current <= 1) remove(itemId)
        else cartCol().document(itemId).update("qty", FieldValue.increment(-1))
    }

    fun remove(itemId: String) {
        ensureBound()
        cartCol().document(itemId).delete()
    }

    fun clear() {
        ensureBound()
        val batch = db.batch()
        lines.keys.forEach { id -> batch.delete(cartCol().document(id)) }
        batch.commit()
    }

    // ---------- INTERNALS ----------
    private fun cartCol() =
        db.collection("customers").document(requireUid()).collection("cart")

    private fun requireUid(): String =
        uid ?: error("CartRepository used without bound user")

    private fun ensureBound() {
        if (uid == null) error("No user bound to CartRepository. Call bindUser(uid).")
    }
}
