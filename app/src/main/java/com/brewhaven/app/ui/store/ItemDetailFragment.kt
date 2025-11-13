package com.brewhaven.app.ui.store

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.brewhaven.app.data.CartRepository
import com.brewhaven.app.data.FavoritesRepository
import com.google.android.material.appbar.MaterialToolbar

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
        item = requireArguments().getParcelable(ARG_ITEM) ?: error("Item missing")

        val image = view.findViewById<ImageView>(R.id.image)
        val titleText = view.findViewById<TextView>(R.id.title)
        val priceText = view.findViewById<TextView>(R.id.price)
        val kcalText = view.findViewById<TextView>(R.id.kcal)
        val allergensText = view.findViewById<TextView>(R.id.allergens)
        val descText = view.findViewById<TextView>(R.id.description)
        val soldOut = view.findViewById<TextView>(R.id.soldOut)

        val btnMinus = view.findViewById<Button>(R.id.btnMinus)
        val btnPlus = view.findViewById<Button>(R.id.btnPlus)
        val qtyText = view.findViewById<TextView>(R.id.qtyText)
        val btnFav = view.findViewById<Button>(R.id.btnFavorite)
        val btnAdd = view.findViewById<Button>(R.id.btnAddToCart)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        // Fill UI
        titleText.text = item.name
        priceText.text = "£" + String.format("%.2f", item.price)
        descText.text = item.description.orEmpty()
        descText.visibility = if (item.description.isNullOrBlank()) View.GONE else View.VISIBLE

        item.calories?.let {
            kcalText.text = "${it.toInt()} kcal"
            kcalText.visibility = View.VISIBLE
        } ?: run { kcalText.visibility = View.GONE }

        val chips = item.allergens?.filter { it.isNotBlank() }.orEmpty()
        if (chips.isNotEmpty()) {
            allergensText.text = "Allergens: ${chips.joinToString(", ")}"
            allergensText.visibility = View.VISIBLE
        } else {
            allergensText.visibility = View.GONE
        }

        image.setImageResource(nameToDrawable(item.name) ?: R.drawable.ic_image_placeholder)

        val available = item.available
        soldOut.visibility = if (available) View.GONE else View.VISIBLE
        btnAdd.isEnabled = available
        btnPlus.isEnabled = available
        btnMinus.isEnabled = available
        view.alpha = if (available) 1f else 0.9f

        // Qty steppers
        qty = 1
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

        // Favorites: live sync with repo
        var fav = FavoritesRepository.isFav(item.id)
        fun renderFav() { btnFav.text = if (fav) "♥ Favorited" else "♡ Favorite" }
        renderFav()

        FavoritesRepository.onChange = { ids ->
            val newFav = item.id in ids
            if (newFav != fav) {
                fav = newFav
                renderFav()
            }
        }

        btnFav.setOnClickListener { FavoritesRepository.toggle(item.id) }

        // Add to cart
        btnAdd.setOnClickListener {
            CartRepository.add(item, qty)
            Toast.makeText(requireContext(), "Added $qty × ${item.name}", Toast.LENGTH_SHORT).show()
        }

        // Toolbar
        toolbar.title = item.name
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (FavoritesRepository.onChange != null) FavoritesRepository.onChange = null
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }

    override fun onStart() {
        super.onStart()
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }

    private fun nameToDrawable(name: String): Int? {
        val slug = name.lowercase()
            .replace("&", "and")
            .replace("[^a-z0-9]+".toRegex(), "_")
            .trim('_')
        val resId = resources.getIdentifier(slug, "drawable", requireContext().packageName)
        return if (resId != 0) resId else null
    }
}
