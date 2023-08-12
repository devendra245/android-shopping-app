package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.squareup.picasso.Picasso

class OrdersAdapter(private var orderList: List<Shirt>) :
    RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    fun updateData(newOrderList: List<Shirt>) {
        orderList = newOrderList
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.itemPriceTextView)
        private val itemDescriptionTextView: TextView = itemView.findViewById(R.id.itemDescriptionTextView)
        private val itemImageView: ImageView = itemView.findViewById(R.id.itemImageView)

        fun bind(order: Shirt) {
            itemNameTextView.text = order.name
            itemPriceTextView.text = "Price: ₹${order.price}"
            itemDescriptionTextView.text = order.description

            // Load the item image using Picasso (you can replace with Glide or any other image loading library)
            Picasso.get().load(order.imageUrl).into(itemImageView)

            // Bind any other data to the views here
        }
    }
}
