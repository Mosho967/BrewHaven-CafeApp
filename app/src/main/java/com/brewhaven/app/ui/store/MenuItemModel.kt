package com.brewhaven.app.ui.store

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuItemModel(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val available: Boolean,
    val description: String? = null,
    val calories: Double? = null,
    val allergens: List<String>? = null
) : Parcelable {
    companion object {
        fun from(d: DocumentSnapshot): MenuItemModel {
            return MenuItemModel(
                id = d.id,
                name = d.getString("name") ?: "Unnamed",
                price = d.getDouble("price") ?: 0.0,
                category = d.getString("category") ?: "",
                available = d.getBoolean("available") ?: false,
                description = d.getString("description"),
                calories = d.getDouble("calories"),
                allergens = (d.get("allergens") as? List<*>)?.mapNotNull { it as? String }
            )
        }
    }
}
