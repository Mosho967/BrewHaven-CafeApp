package com.brewhaven.app.ui.store

import android.content.Context
import com.brewhaven.app.R

/**
 * ImageResolver
 *
 * Utility responsible for converting menu item names into drawable resource IDs.
 * This allows menu items to automatically map to images when their text labels
 * match drawable filenames (after normalisation).
 *
 * Normalisation steps:
 * - Lowercase
 * - Replace "&" with "and"
 * - Strip text in parentheses (e.g., "(Single)")
 * - Replace non-alphanumeric sequences with underscores
 * - Collapse multiple underscores
 * - Trim leading/trailing underscores
 *
 * Falls back to a placeholder image if the lookup fails.
 */
object ImageResolver {

    /**
     * Returns a drawable resource ID for the given item name after normalisation.
     * @param context Context used to resolve drawable identifiers.
     * @param name Item display name (e.g., "Caramel Latte").
     */
    fun imageResFor(context: Context, name: String): Int {
        // Generate lookup key by normalising item names
        var key = name.lowercase()
            .replace("&", "and")
            .replace("\\(.*?\\)".toRegex(), "")     // remove content inside parentheses
            .replace("[^a-z0-9]+".toRegex(), "_")   // non-alphanumeric -> underscore
            .replace("_+".toRegex(), "_")           // collapse double underscores
            .trim('_')

        // Placeholder transformations (kept if needed for future menu adjustments)
        key = key
            .replace("espresso_", "espresso_")
            .replace("blt", "blt")

        // Resolve drawable by name
        val resId = context.resources.getIdentifier(key, "drawable", context.packageName)

        return if (resId != 0) resId else R.drawable.ic_image_placeholder
    }
}
