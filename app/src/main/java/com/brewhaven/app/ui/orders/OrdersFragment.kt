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

class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private lateinit var adapter: OrdersAdapter
    private lateinit var list: RecyclerView
    private lateinit var empty: TextView
    private lateinit var progress: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setBottomNavVisible(true)

        view.findViewById<MaterialToolbar>(R.id.toolbar)?.apply { title = "Orders" }

        list = view.findViewById(R.id.ordersList)
        empty = view.findViewById(R.id.emptyText)
        progress = view.findViewById(R.id.progress)

        list.layoutManager = LinearLayoutManager(requireContext())
        adapter = OrdersAdapter { order ->
            // TODO: navigate to OrderDetailFragment.newInstance(order.id)
        }
        list.adapter = adapter

        // start with spinner
        showLoading(true)

        // live updates from repo
        OrdersRepository.onChange = { orders ->

            if (orders.isEmpty()) {
                empty.visibility = View.VISIBLE
                list.visibility = View.GONE
            } else {
                empty.visibility = View.GONE
                list.visibility = View.VISIBLE
                adapter.submit(orders)
            }
        }

        // render cached immediately if present
        val cached = OrdersRepository.current()
        if (cached.isNotEmpty()) {
            showLoading(false)
            empty.visibility = View.GONE
            list.visibility = View.VISIBLE
            adapter.submit(cached)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        OrdersRepository.onChange = null
    }

    private fun showLoading(show: Boolean) {
        progress.visibility = if (show) View.VISIBLE else View.GONE
    }
}

