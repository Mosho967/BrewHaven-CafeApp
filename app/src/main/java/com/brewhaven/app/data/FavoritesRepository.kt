package com.brewhaven.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object FavoritesRepository {

    private val db by lazy { FirebaseFirestore.getInstance() }

    // In-memory cache for currently bound user
    private val favs = mutableSetOf<String>()
    private var uid: String? = null
    private var reg: ListenerRegistration? = null

    /** Optional hook for UI to refresh when favourites change */
    var onChange: ((Set<String>) -> Unit)? = null

    /** Bind to a user (or null to unbind on sign-out). Starts/stops listener and resets cache. */
    fun bindUser(userId: String?) {
        if (uid == userId) return

        reg?.remove()
        reg = null
        favs.clear()
        uid = userId

        if (uid == null) {
            onChange?.invoke(favs.toSet())
            return
        }

        reg = favCol().addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener
            favs.clear()
            for (d in snap.documents) favs += d.id
            onChange?.invoke(favs.toSet())
        }
    }

    // ---------- Public API ----------
    fun toggle(itemId: String) {
        if (isFav(itemId)) remove(itemId) else add(itemId)
    }

    fun add(itemId: String) {
        ensureBound()
        favs += itemId
        onChange?.invoke(favs.toSet())
        favCol().document(itemId).set(mapOf("added" to System.currentTimeMillis()))
    }

    fun remove(itemId: String) {
        ensureBound()
        favs.remove(itemId)
        onChange?.invoke(favs.toSet())
        favCol().document(itemId).delete()
    }

    fun isFav(itemId: String): Boolean = itemId in favs
    fun allIds(): Set<String> = favs.toSet()

    // ---------- Internals ----------
    private fun favCol() =
        db.collection("customers").document(requireUid()).collection("favorites")

    private fun requireUid(): String =
        uid ?: error("FavoritesRepository used while no user is bound. Call bindUser(uid) first.")

    private fun ensureBound() {
        if (uid == null) error("No user bound. Call FavoritesRepository.bindUser(uid) after login.")
    }
}
