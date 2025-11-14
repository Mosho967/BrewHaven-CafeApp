package com.brewhaven.app.ui.cart

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.brewhaven.app.data.CartRepository
import com.brewhaven.app.data.OrdersRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

/**
 * CartFragment
 *
 * Displays the user's shopping cart, allowing quantity adjustments,
 * item removal, and order checkout. This fragment reads all cart data
 * from [CartRepository], and listens to repository changes via `onChange`
 * to keep the UI always in sync.
 *
 * Checkout triggers an asynchronous order creation via [OrdersRepository].
 * UI state is updated safely with proper guards, and the cart is cleared
 * only after a confirmed successful order.
 *
 * Bottom navigation remains visible on this screen, consistent with
 * the app’s navigation structure.
 */
class CartFragment : Fragment(R.layout.fragment_cart) {

    private lateinit var adapter: CartAdapter
    private lateinit var totalText: TextView
    private lateinit var emptyText: TextView
    private lateinit var list: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavVisible(true)

        // Configure toolbar with back behaviour
        view.findViewById<MaterialToolbar>(R.id.toolbar)?.apply {
            title = "Cart"
            setNavigationIcon(R.drawable.ic_arrow_back_24)

            // Back: if there is a back stack, pop it.
            // If not, return to the Menu tab.
            setNavigationOnClickListener {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    requireActivity()
                        .findViewById<BottomNavigationView>(R.id.bottomNav)
                        .selectedItemId = R.id.nav_menu
                }
            }
        }

        list = view.findViewById(R.id.cartList)
        emptyText = view.findViewById(R.id.emptyText)
        totalText = view.findViewById(R.id.totalText)
        val btnCheckout = view.findViewById<MaterialButton>(R.id.btnCheckout)

        list.layoutManager = LinearLayoutManager(requireContext())

        /**
         * Adapter is configured with callbacks that modify the cart state.
         * After each cart change, render() updates totals + list visibility.
         */
        adapter = CartAdapter(
            onPlus = { line -> CartRepository.inc(line.itemId); render() },
            onMinus = { line -> CartRepository.dec(line.itemId); render() },
            onRemove = { line -> CartRepository.remove(line.itemId); render() },
            nameToDrawable = { name ->
                // Converts item names to drawable filenames (fallback to placeholder)
                val slug = name.lowercase()
                    .replace("&", "and")
                    .replace("[^a-z0-9]+".toRegex(), "_")
                    .trim('_')

                val res = resources.getIdentifier(slug, "drawable", requireContext().packageName)
                if (res != 0) res else R.drawable.ic_image_placeholder
            }
        )
        list.adapter = adapter

        /**
         * Checkout button:
         * - Validates cart not empty
         * - Calls OrdersRepository to create the order asynchronously
         * - On success: clears cart and shows confirmation
         */
        btnCheckout.setOnClickListener {
            val items = CartRepository.items()
            if (items.isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty.", Toast.LENGTH_SHORT).show()
            } else {
                btnCheckout.isEnabled = false

                OrdersRepository.createOrder(items, "Card") { orderId ->
                    btnCheckout.isEnabled = true

                    if (orderId != null) {
                        CartRepository.clear()
                        Toast.makeText(
                            requireContext(),
                            "Order placed successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to place order. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        // Listen to changes in CartRepository (global cart state)
        CartRepository.onChange = { render() }

        render()
    }

    /**
     * Ensures bottom navigation stays visible when returning to this screen.
     */
    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }

    /**
     * Prevents repository callbacks from trying to update destroyed views.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        CartRepository.onChange = null
    }

    /**
     * Renders the full cart UI:
     * - Updates list content
     * - Updates total text
     * - Switches between empty state and populated list
     */
    private fun render() {
        val lines = CartRepository.items()
        adapter.submit(lines)

        val total = CartRepository.total()
        totalText.text = "Total: £" + String.format("%.2f", total)

        val empty = lines.isEmpty()
        emptyText.visibility = if (empty) View.VISIBLE else View.GONE
        list.visibility = if (empty) View.GONE else View.VISIBLE
    }
}
