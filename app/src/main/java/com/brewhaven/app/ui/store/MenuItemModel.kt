package com.brewhaven.app.ui.store

data class MenuItemModel(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val available: Boolean,
    val description: String?,
    val calories: Double?,          // Firestore may store numbers as Double
    val allergens: List<String>?
)