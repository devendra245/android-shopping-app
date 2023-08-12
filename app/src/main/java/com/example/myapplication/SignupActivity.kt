package com.example.myapplication


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var signupbt: Button
    private lateinit var nameEt: EditText
    private lateinit var phonenumberEt: EditText

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailEt = findViewById(R.id.email2)
        passwordEt = findViewById(R.id.password2)
        signupbt = findViewById(R.id.button2)
        nameEt = findViewById(R.id.name)
        phonenumberEt = findViewById(R.id.phone)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .child("userprofile")

        signupbt.setOnClickListener {
            val email = emailEt.text.toString()
            val password = passwordEt.text.toString()
            val name = nameEt.text.toString()
            val phonenumber = phonenumberEt.text.toString()
            signUp(email, password, name, phonenumber)
        }
    }

    private fun signUp(email: String, password: String, name: String, phonenumber: String) {
        // Check if any of the required fields are empty
        if (email.isBlank() || password.isBlank() || name.isBlank() || phonenumber.isBlank()) {
            Toast.makeText(this, "Please fill in all the required details.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User creation successful
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid
                        val userData = hashMapOf(
                            "name" to name,
                            "phonenumber" to phonenumber,
                            // Add any additional user data as needed
                        )

                        // Save the user data in the Firebase Realtime Database under the user's UID
                        database.child(userId).setValue(userData)
                            .addOnSuccessListener {
                                val intent = Intent(this@SignupActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                                // Optional: Finish the current Signup activity to prevent going back to it with the back button
                            }
                            .addOnFailureListener { e ->
                                // Error saving user data
                                Toast.makeText(
                                    this,
                                    "Error saving user data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    // User creation failed
                    Toast.makeText(
                        this,
                        "Signup failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
