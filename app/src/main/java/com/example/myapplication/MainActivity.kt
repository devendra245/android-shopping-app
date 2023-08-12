package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var gotocart: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Picasso.get().setIndicatorsEnabled(true)

        val actionBar = getSupportActionBar()

        // showing the back button in action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        gotocart= findViewById(R.id.gotocart)
        gotocart.setOnClickListener {
            openFragment(Cart())
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_menu_24)

        navView.setNavigationItemSelectedListener(this)
        if (savedInstanceState == null) {
            openFragment(Home()) // Replace "Home()" with the actual fragment instance for the "Home" fragment.
            navView.setCheckedItem(R.id.nav_home) // Highlight the "Home" item in the navigation drawer
        }

        // Fetch and display the user's profile image in the CircleImageView in the toolbar
        fetchProfileImage()
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation item clicks here
        when (item.itemId) {
            R.id.nav_home -> openFragment(Home())
            R.id.nav_profile -> openFragment(ProfileFragment())
            R.id.nav_menu -> openFragment(Cart())
            R.id.nav_orders -> openFragment(Orders())
            R.id.nav_special_offers -> openFragment(Special())
            R.id.nav_discount -> openFragment(Discount())
            R.id.nav_rewards -> openFragment(Rewards())
            R.id.nav_logout -> openFragment(Logout())
            // Add more cases for other menu items if needed
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_content, fragment)
            .commit()
    }

    override fun onSupportNavigateUp(): Boolean {
        drawerLayout.openDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
