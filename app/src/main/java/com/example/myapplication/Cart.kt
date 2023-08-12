package com.example.myapplication

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Cart : Fragment(), CartAdapter.RemoveFromCartClickListener, CartAdapter.OrderClickListener {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Initialize the Firebase Realtime Database reference for the cart items
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            database = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(currentUser.uid)
                .child("cart")
        } else {
            // Handle case where user is not logged in
        }

        // Get a reference to the RecyclerView in the fragment's layout
        recyclerView = view.findViewById(R.id.recyclerView)

        // Set up the RecyclerView with the CartAdapter and pass `this` as the click listener
        cartAdapter = CartAdapter(emptyList(), this, this)
        recyclerView.adapter = cartAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onStart() {
        super.onStart()

        // Read data from the Firebase Realtime Database
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val cartItemList = mutableListOf<Shirt>()

                for (itemSnapshot in dataSnapshot.children) {
                    // Parse cart item data and create CartItem object
                    val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                    val price = itemSnapshot.child("price").getValue(Double::class.java) ?: 0.0
                    val description = itemSnapshot.child("description").getValue(String::class.java) ?: ""
                    val imageUrl = itemSnapshot.child("imageUrl").getValue(String::class.java) ?: ""

                    val cartItem = Shirt(name, price, description, imageUrl)
                    cartItemList.add(cartItem)
                }

                // Update the RecyclerView with the cart items
                cartAdapter.updateData(cartItemList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        }

        database.addValueEventListener(valueEventListener)
    }

    override fun onRemoveFromCartClick(shirt: Shirt) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val cartRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(currentUser.uid)
                .child("cart")

            // Query the cart items to find the item to remove
            cartRef.orderByChild("name").equalTo(shirt.name).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (itemSnapshot in dataSnapshot.children) {
                        // Find the matching item and remove it from the cart
                        if (itemSnapshot.child("name").getValue(String::class.java) == shirt.name) {
                            itemSnapshot.ref.removeValue()
                            break
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    Toast.makeText(requireContext(), "Failed to remove item from cart.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // User not logged in, handle this case if necessary
            Toast.makeText(requireContext(), "Please log in to remove items from cart.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOrderClick(shirt: Shirt) {
        showOrderForm(shirt)
    }

    private fun showOrderForm(shirt: Shirt) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.order_form_layout)

        // Get references to the views in the order form
        val fullNameEditText = dialog.findViewById<EditText>(R.id.fullNameEditText)
        val mobileNumberEditText = dialog.findViewById<EditText>(R.id.mobileNumberEditText)
        val emailEditText = dialog.findViewById<EditText>(R.id.emailEditText)
        val addressEditText = dialog.findViewById<EditText>(R.id.addressEditText)
        val itemNameTextView = dialog.findViewById<TextView>(R.id.itemNameTextView)
        val itemPriceTextView = dialog.findViewById<TextView>(R.id.itemPriceTextView)
        val itemDescriptionTextView = dialog.findViewById<TextView>(R.id.itemDescriptionTextView)
        val itemImageView = dialog.findViewById<CircleImageView>(R.id.imageViewItem)


        // Populate the order form fields with the shirt details
        itemNameTextView.text = shirt.name
        itemPriceTextView.text = "Price: $${shirt.price}"
        itemDescriptionTextView.text = shirt.description
        Picasso.get()
            .load(shirt.imageUrl)
            .into(itemImageView)

        // Set a click listener for the "Order" button in the order form
        val orderButton = dialog.findViewById<Button>(R.id.orderButton)
        orderButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val mobileNumber = mobileNumberEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()

            if (fullName.isNotEmpty() && mobileNumber.isNotEmpty() && email.isNotEmpty() && address.isNotEmpty()) {
                // Valid form data, create an order in the database
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val orderRef = FirebaseDatabase.getInstance().reference
                        .child("users")
                        .child(currentUser.uid)
                        .child("orders")
                        .push()

                    val orderData = HashMap<String, Any>()
                    orderData["fullName"] = fullName
                    orderData["mobileNumber"] = mobileNumber
                    orderData["email"] = email
                    orderData["address"] = address
                    orderData["itemName"] = shirt.name // Add the shirt name to the order data
                    orderData["itemPrice"] = shirt.price // Add the shirt price to the order data
                    orderData["itemDescription"] = shirt.description // Add the shirt description to the order data
                    orderData["itemImageUrl"] = shirt.imageUrl

                    orderRef.updateChildren(orderData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Order placed successfully!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()

                            // Remove the item from the cart after placing the order
                            onRemoveFromCartClick(shirt)
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to place order.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // User not logged in, handle this case if necessary
                    Toast.makeText(requireContext(), "Please log in to place an order.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}