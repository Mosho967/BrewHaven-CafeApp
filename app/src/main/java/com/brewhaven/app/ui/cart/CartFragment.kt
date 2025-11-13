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

class CartFragment : Fragment(R.layout.fragment_cart) {

    private lateinit var adapter: CartAdapter
    private lateinit var totalText: TextView
    private lateinit var emptyText: TextView
    private lateinit var list: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavVisible(true)

        view.findViewById<MaterialToolbar>(R.id.toolbar)?.apply {
            title = "Cart"
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

        adapter = CartAdapter(
            onPlus = { line -> CartRepository.inc(line.itemId); render() },
            onMinus = { line -> CartRepository.dec(line.itemId); render() },
            onRemove = { line -> CartRepository.remove(line.itemId); render() },
            nameToDrawable = { name ->
                val slug = name.lowercase()
                    .replace("&", "and")
                    .replace("[^a-z0-9]+".toRegex(), "_")
                    .trim('_')
                val res = resources.getIdentifier(slug, "drawable", requireContext().packageName)
                if (res != 0) res else R.drawable.ic_image_placeholder
            }
        )
        list.adapter = adapter

        btnCheckout.setOnClickListener {
            if (CartRepository.items().isEmpty()) {
                Toast.makeText(requireContext(), "Cart is empty.", Toast.LENGTH_SHORT).show()
            } else {
                btnCheckout.isEnabled = false
                val items = CartRepository.items()

                OrdersRepository.createOrder(items, "Card") { orderId ->
                    btnCheckout.isEnabled = true
                    if (orderId != null) {
                        CartRepository.clear()
                        Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to place order. Try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }



        // Optional live redraw if you wired CartRepository.onChange somewhere
        CartRepository.onChange = { render() }

        render()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setBottomNavVisible(true)
    }

    private fun render() {
        val lines = CartRepository.items()              // List<CartRepository.Line>
        adapter.submit(lines)

        val total = CartRepository.total()
        totalText.text = "Total: £" + String.format("%.2f", total)

        val empty = lines.isEmpty()
        emptyText.visibility = if (empty) View.VISIBLE else View.GONE
        list.visibility = if (empty) View.GONE else View.VISIBLE
    }
}
