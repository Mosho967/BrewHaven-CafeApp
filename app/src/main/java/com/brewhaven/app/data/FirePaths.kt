package com.brewhaven.app.data

import com.google.firebase.firestore.FirebaseFirestore

object FirePaths {
    private val db get() = FirebaseFirestore.getInstance()

    fun customer(uid: String) =
        db.collection("customers").document(uid)

    fun favorites(uid: String) =
        customer(uid).collection("favorites")

    fun cart(uid: String) =
        customer(uid).collection("cart")

    fun orders(uid: String) =
        customer(uid).collection("orders")
}
