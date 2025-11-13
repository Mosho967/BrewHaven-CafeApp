package com.brewhaven.app.ui.store

import android.content.Context
import com.brewhaven.app.R

object ImageResolver {
    fun imageResFor(context: Context, name: String): Int {
        // normalize
        var key = name.lowercase()
            .replace("&", "and")
            .replace("\\(.*?\\)".toRegex(), "")     // remove (Single) etc
            .replace("[^a-z0-9]+".toRegex(), "_")
            .replace("_+".toRegex(), "_")           // collapse
            .trim('_')


        key = key
            .replace("espresso_", "espresso_") // no-op placeholder
            .replace("blt", "blt")

        val resId = context.resources.getIdentifier(key, "drawable", context.packageName)
        return if (resId != 0) resId else R.drawable.ic_image_placeholder
    }
}
