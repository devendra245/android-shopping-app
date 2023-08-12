package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ShirtAdapter(
    private var shirtList: List<Shirt>,
    private val addToCartClickListener: AddToCartClickListener,
    private val orderClickListener: OrderClickListener
) : RecyclerView.Adapter<ShirtAdapter.ShirtViewHolder>() {

    interface AddToCartClickListener {
        fun onAddToCartClick(shirt: Shirt)
    }

    interface OrderClickListener {
        fun onOrderClick(shirt: Shirt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShirtViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_shirt, parent, false)
        return ShirtViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ShirtViewHolder, position: Int) {
        val currentItem = shirtList[position]
        holder.textViewShirtName.text = currentItem.name
        holder.textViewShirtPrice.text = "Price: ₹${currentItem.price}"
        holder.textViewShirtDescription.text = currentItem.description

        Picasso.get()
            .load(currentItem.imageUrl)
            .into(holder.imageViewShirt)

        // Set a click listener for the "Add to Cart" button
        holder.buttonAddToCart.setOnClickListener {
            addToCartClickListener.onAddToCartClick(currentItem)
        }

        // Set a click listener for the "Order" button
        holder.orderButton.setOnClickListener {
            orderClickListener.onOrderClick(currentItem)
        }
    }

    override fun getItemCount() = shirtList.size

    fun updateData(newShirtList: List<Shirt>) {
        shirtList = newShirtList
        notifyDataSetChanged()
    }

    inner class ShirtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewShirtName: TextView = itemView.findViewById(R.id.textViewShirtName)
        val textViewShirtPrice: TextView = itemView.findViewById(R.id.textViewShirtPrice)
        val textViewShirtDescription: TextView = itemView.findViewById(R.id.textViewShirtDescription)
        val imageViewShirt: CircleImageView = itemView.findViewById(R.id.imageViewShirt)
        val buttonAddToCart: Button = itemView.findViewById(R.id.cart)
        val orderButton: Button = itemView.findViewById(R.id.ordershirt)
    }

}
