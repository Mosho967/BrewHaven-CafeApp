package com.brewhaven.app.data
object FavoritesRepository {
    private val favs = mutableSetOf<String>()

    fun toggle(id: String) {
        if (favs.contains(id)) favs.remove(id) else favs.add(id)
    }

    fun remove(id: String) = favs.remove(id)

    fun isFav(id: String) = favs.contains(id)

    fun allIds(): Set<String> = favs
}
