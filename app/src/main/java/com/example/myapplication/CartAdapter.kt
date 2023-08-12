package com.example.myapplication

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class CartAdapter(
    private var cartItemList: List<Shirt>,
    private val removeFromCartClickListener: RemoveFromCartClickListener,
    private val orderClickListener: OrderClickListener
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val currentItem = cartItemList[position]
        holder.textViewCartItemName.text = currentItem.name
        holder.textViewCartItemPrice.text = "Price: $${currentItem.price}"
        holder.textViewCartItemDescription.text = currentItem.description

        Picasso.get()
            .load(currentItem.imageUrl)
            .into(holder.imageViewCartItem)

        holder.buttonRemoveFromCart.setOnClickListener {
            removeFromCartClickListener.onRemoveFromCartClick(currentItem)
        }

        holder.buttonOrder.setOnClickListener {
            orderClickListener.onOrderClick(currentItem)
        }
    }

    override fun getItemCount() = cartItemList.size

    fun updateData(newCartItemList: List<Shirt>) {
        cartItemList = newCartItemList
        notifyDataSetChanged()
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewCartItemName: TextView = itemView.findViewById(R.id.textViewCartName)
        val textViewCartItemPrice: TextView = itemView.findViewById(R.id.textViewCartPrice)
        val textViewCartItemDescription: TextView = itemView.findViewById(R.id.textViewCartDescription)
        val imageViewCartItem: ImageView = itemView.findViewById(R.id.imageViewCart)
        val buttonRemoveFromCart: Button = itemView.findViewById(R.id.removecart)
        val buttonOrder: Button = itemView.findViewById(R.id.ordershirt)
    }

    interface RemoveFromCartClickListener {
        fun onRemoveFromCartClick(shirt: Shirt)
    }

    interface OrderClickListener {
        fun onOrderClick(shirt: Shirt)
    }
}
