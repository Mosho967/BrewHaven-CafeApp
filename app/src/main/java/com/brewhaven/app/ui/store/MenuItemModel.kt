package com.brewhaven.app.ui.store

import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.parcelize.Parcelize

/**
 * MenuItemModel
 *
 * Represents an individual menu item loaded from Firestore.
 * This model is Parcelable so it can be passed safely between fragments.
 *
 * Fields include:
 * - id: Firestore document ID
 * - name: Display name of the item
 * - price: Unit price
 * - category: Category this item belongs to (e.g., "Teas", "Sandwiches")
 * - available: Whether the item is currently sold out or available
 * - description: Optional text description
 * - calories: Optional numeric calorie value
 * - allergens: Optional list of allergen tags
 *
 * The [from] helper provides a safe and consistent way to build this model
 * from a Firestore DocumentSnapshot.
 */
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
        /**
         * Builds a [MenuItemModel] from a Firestore DocumentSnapshot.
         * Ensures null-safe defaults for all required fields.
         */
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
