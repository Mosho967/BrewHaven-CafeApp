package com.brewhaven.app.ui.store

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.brewhaven.app.R

class ItemDetailFragment : Fragment(R.layout.fragment_item_detail) {

    companion object {
        private const val ARG_ITEM = "arg_item"
        fun newInstance(item: MenuItemModel) = ItemDetailFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_ITEM, item) }
        }
    }

    private lateinit var item: MenuItemModel
    private var qty = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        item = requireArguments().getParcelable(ARG_ITEM)
            ?: error("Item missing")

        val image = view.findViewById<ImageView>(R.id.image)
        val title = view.findViewById<TextView>(R.id.title)
        val price = view.findViewById<TextView>(R.id.price)
        val kcal = view.findViewById<TextView>(R.id.kcal)
        val allergens = view.findViewById<TextView>(R.id.allergens)
        val desc = view.findViewById<TextView>(R.id.description)
        val soldOut = view.findViewById<TextView>(R.id.soldOut)

        val btnMinus = view.findViewById<Button>(R.id.btnMinus)
        val btnPlus = view.findViewById<Button>(R.id.btnPlus)
        val qtyText = view.findViewById<TextView>(R.id.qtyText)
        val btnFav = view.findViewById<Button>(R.id.btnFavorite)
        val btnAdd = view.findViewById<Button>(R.id.btnAddToCart)

        // Fill UI
        title.text = item.name
        price.text = "£" + String.format("%.2f", item.price)
        desc.text = item.description.orEmpty()
        desc.visibility = if (item.description.isNullOrBlank()) View.GONE else View.VISIBLE

        // calories
        item.calories?.let {
            kcal.text = "${it.toInt()} kcal"
            kcal.visibility = View.VISIBLE
        } ?: run {
            kcal.visibility = View.GONE
        }


        val chips = item.allergens?.filter { it.isNotBlank() }.orEmpty()
        if (chips.isNotEmpty()) {
            allergens.text = "Allergens: ${chips.joinToString(", ")}"
            allergens.visibility = View.VISIBLE
        } else {
            allergens.visibility = View.GONE
        }

        // Image: try to map name to drawable (filenames like "still_water_500ml.png")
        image.setImageResource(nameToDrawable(item.name) ?: R.drawable.ic_image_placeholder)

        // Sold out treatment
        val available = item.available
        soldOut.visibility = if (available) View.GONE else View.VISIBLE
        btnAdd.isEnabled = available
        btnPlus.isEnabled = available
        btnMinus.isEnabled = available
        view.alpha = if (available) 1f else 0.9f

        // Qty steppers
        qtyText.text = qty.toString()
        btnMinus.setOnClickListener {
            if (qty > 1) {
                qty -= 1
                qtyText.text = qty.toString()
            }
        }
        btnPlus.setOnClickListener {
            if (qty < 20) {
                qty += 1
                qtyText.text = qty.toString()
            }
        }

        // Favorite: visual toggle only for now (real persistence in step 3)
        var fav = false
        btnFav.setOnClickListener {
            fav = !fav
            btnFav.text = if (fav) "♥ Favorited" else "♡ Favorite"
            // later: FavoritesStore.toggle(item.id)
        }

        // Add to cart: stub for now, we wire to CartRepository in step 2
        btnAdd.setOnClickListener {
            Toast.makeText(requireContext(), "Added ${qty} × ${item.name}", Toast.LENGTH_SHORT).show()
            // later: CartRepository.add(item, qty); maybe navigate to cart
        }
    }

    /**
     * Map a human name to a drawable you already placed in res/drawable.
     * Example: "Still Water 500ml" -> R.drawable.still_water_500ml
     */
    private fun nameToDrawable(name: String): Int? {
        val slug = name.lowercase()
            .replace("&", "and")
            .replace("[^a-z0-9]+".toRegex(), "_")
            .trim('_')
        val resId = resources.getIdentifier(slug, "drawable", requireContext().packageName)
        return if (resId != 0) resId else null
    }
}
