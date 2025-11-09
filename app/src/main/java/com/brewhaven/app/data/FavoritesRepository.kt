package com.brewhaven.app.data

object FavoritesRepository {
    private val ids = linkedSetOf<String>()

    fun isFav(id: String): Boolean = id in ids

    fun toggle(id: String) {
        if (!ids.add(id)) ids.remove(id)
    }

    fun all(): Set<String> = ids
}