package com.brewhaven.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object OrdersRepository {

    data class OrderSummary(
        val id: String,
        val createdAt: Long,
        val total: Double,
        val itemCount: Int,
        val paymentType: String,
        val status: String
    )

    private val db by lazy { FirebaseFirestore.getInstance() }
    private var uid: String? = null
    private var reg: ListenerRegistration? = null
    private val cache = mutableListOf<OrderSummary>()

    var onChange: ((List<OrderSummary>) -> Unit)? = null

    /** Called by MainActivity when user logs in or out */
    fun bindUser(userId: String?) {
        if (uid == userId) return
        reg?.remove()
        reg = null
        uid = userId
        cache.clear()

        if (uid == null) {
            onChange?.invoke(emptyList())
            return
        }

        reg = orderCol().addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener
            cache.clear()
            for (doc in snap.documents) {
                val id = doc.id
                val createdAt = doc.getLong("createdAt") ?: 0L
                val total = doc.getDouble("total") ?: 0.0
                val count = (doc.getLong("itemCount") ?: 0L).toInt()
                val pay = doc.getString("paymentType") ?: "Unknown"
                val status = doc.getString("status") ?: "Pending"
                cache += OrderSummary(id, createdAt, total, count, pay, status)
            }
            onChange?.invoke(current())
        }
    }

    fun current(): List<OrderSummary> = cache.toList()

    /** Called when placing a new order */
    fun createOrder(
        items: List<CartRepository.Line>,
        paymentType: String,
        onComplete: (String?) -> Unit
    ) {
        val u = uid ?: return onComplete(null)

        val total = items.sumOf { it.price * it.qty }
        val itemCount = items.sumOf { it.qty }

        val order = mapOf(
            "createdAt" to System.currentTimeMillis(),
            "total" to total,
            "itemCount" to itemCount,
            "paymentType" to paymentType,
            "status" to "Pending"
        )

        val col = orderCol()
        val doc = col.document()
        doc.set(order)
            .addOnSuccessListener { onComplete(doc.id) }
            .addOnFailureListener { onComplete(null) }
    }

    private fun orderCol() =
        db.collection("customers").document(requireUid()).collection("orders")

    private fun requireUid(): String =
        uid ?: error("OrdersRepository used without bound user. Call bindUser(uid) first.")
}
