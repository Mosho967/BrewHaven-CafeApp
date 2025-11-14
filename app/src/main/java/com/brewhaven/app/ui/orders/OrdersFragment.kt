package com.brewhaven.app.ui.orders

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brewhaven.app.MainActivity
import com.brewhaven.app.R
import com.brewhaven.app.data.OrdersRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * OrdersFragment
 *
 * Displays the list of orders placed by the current user.
 * The fragment observes [OrdersRepository.onChange] to stay updated
 * with any new orders or changes, and renders loading / empty / list states.
 *
 * Responsibilities:
 * - Show a list of past orders in a RecyclerView.
 * - Handle back navigation consistently.
 * - Render cached orders instantly if available.
 * - Listen for repository-driven updates and refresh UI accordingly.
 *
 * All order retrieval and logic come from [OrdersRepository];
 * this fragment is strictly responsible for UI.
 */
class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private lateinit var adapter: OrdersAdapter
    private lateinit var list: RecyclerView
    private lateinit var empty: TextView
    private lateinit var progress: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(true)

        // Configure toolbar with proper back behaviour
        view.findViewById<MaterialToolbar>(R.id.toolbar)?.apply {
            title = "Orders"
            setNavigationIcon(R.drawable.ic_arrow_back_24)
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

        list = view.findViewById(R.id.ordersList)
        empty = view.findViewById(R.id.emptyText)
        progress = view.findViewById(R.id.progress)

        list.layoutManager = LinearLayoutManager(requireContext())

        // Adapter for order summaries
        adapter = OrdersAdapter { order ->
            // Placeholder: navigate to order details in future
            // OrderDetailFragment.newInstance(order.id)
        }
        list.adapter = adapter

        showLoading(true)

        /**
         * Listen to live updates from OrdersRepository.
         * When new orders are available, update the UI accordingly.
         */
        OrdersRepository.onChange = onChange@ { orders ->
            if (!isAdded) return@onChange

            showLoading(false)

            if (orders.isEmpty()) {
                empty.visibility = View.VISIBLE
                list.visibility = View.GONE
            } else {
                empty.visibility = View.GONE
                list.visibility = View.VISIBLE
                adapter.submit(orders)
            }
        }

        /**
         * If the repository already has cached orders, show them immediately
         * to avoid a blank screen while the listener fires.
         */
        val cached = OrdersRepository.current()
        if (cached.isNotEmpty()) {
            showLoading(false)
            empty.visibility = View.GONE
            list.visibility = View.VISIBLE
            adapter.submit(cached)
        }
    }

    /**
     * Clears repository callback to prevent leaks or invalid updates.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        OrdersRepository.onChange = null
    }

    /**
     * Toggles progress indicator visibility.
     */
    private fun showLoading(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
    }
}
