package com.example.myapplication

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ShoesActivity : AppCompatActivity(), ShirtAdapter.AddToCartClickListener, ShirtAdapter.OrderClickListener {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var shirtAdapter: ShirtAdapter
    private lateinit var gotocart: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shirt)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbarshirt)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Shoes"
        fetchProfileImage()

        // Initialize the Firebase Realtime Database reference for "shirts" category
        database = FirebaseDatabase.getInstance().reference.child("shopping_items").child("shoes")

        // Get a reference to the RecyclerView in your activity's layout
        recyclerView = findViewById(R.id.recyclerView)

        // Set up the RecyclerView with the Adapter and pass `this` as the click listener
        shirtAdapter = ShirtAdapter(emptyList(), this, this)
        recyclerView.adapter = shirtAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Read data from the Firebase Realtime Database
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val shirtList = mutableListOf<Shirt>()

                for (itemSnapshot in dataSnapshot.children) {
                    val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                    val price = itemSnapshot.child("price").getValue(Double::class.java) ?: 0.0
                    val description = itemSnapshot.child("description").getValue(String::class.java) ?: ""
                    val imageUrl = itemSnapshot.child("image_url").getValue(String::class.java) ?: ""

                    val shirt = Shirt(name, price, description, imageUrl)
                    shirtList.add(shirt)
                }

                // Update the RecyclerView with the new shirt data
                shirtAdapter.updateData(shirtList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        }

        database.addValueEventListener(valueEventListener)
    }

    private fun fetchProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference
        val profileImageRef = database.child("users").child(userId ?: "").child("profile").child("profileImage")

        profileImageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.value as? String

                // Fetch and display the user's profile image in the CircleImageView in the toolbar
                val profileImageViewToolbar: CircleImageView = findViewById(R.id.profileimage)
                if (!profileImageUrl.isNullOrBlank()) {
                    Picasso.get().load(profileImageUrl).placeholder(R.drawable.img_9).into(profileImageViewToolbar)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("DatabaseError", "Error fetching profile image URL: ${error.message}")
            }
        })
    }

    override fun onAddToCartClick(shirt: Shirt) {
        addToCart(shirt)
    }

    override fun onOrderClick(shirt: Shirt) {
        showOrderForm(shirt)
    }

    private fun addToCart(shirt: Shirt) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val cartRef = FirebaseDatabase.getInstance().reference
                .child("users")
                .child(currentUser.uid)
                .child("cart")

            // Check if the item already exists in the cart
            cartRef.orderByChild("name").equalTo(shirt.name).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Item already exists in the cart, show a toast message
                        Toast.makeText(this@ShoesActivity, "Item already in cart!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Item does not exist in the cart, add it
                        val cartItemRef = cartRef.push()
                        cartItemRef.setValue(shirt)
                            .addOnSuccessListener {
                                Toast.makeText(this@ShoesActivity, "Item added to cart!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@ShoesActivity, "Failed to add item to cart.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle errors
                    Toast.makeText(this@ShoesActivity, "Failed to add item to cart.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // User not logged in, handle this case if necessary
            Toast.makeText(this, "Please log in to add items to cart.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showOrderForm(shirt: Shirt) {
        val dialog = Dialog(this)
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
                            Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to place order.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // User not logged in, handle this case if necessary
                    Toast.makeText(this, "Please log in to place an order.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
