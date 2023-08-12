package com.example.myapplication


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var storageRef: StorageReference
    private lateinit var userId: String

    private lateinit var nameTextview: TextView
    private lateinit var phoneNumberTextview: TextView
    private lateinit var saveButton: Button
    private lateinit var updateButton: Button
    private lateinit var profileImageView: CircleImageView
    private lateinit var editProfileImageButton: ImageButton
    private lateinit var nameEdittext: TextInputLayout
    private lateinit var phonenumberEdittext: TextInputLayout
    private lateinit var editname: TextInputEditText
    private lateinit var editphonenumber: TextInputEditText
    private lateinit var editemail : TextInputEditText
    private lateinit var emailEdittext :  TextInputLayout
    private lateinit var emailTextview  : TextView

    private val PICK_IMAGE_REQUEST = 1
    private val PERMISSION_CODE = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        db = database.reference
        storageRef = FirebaseStorage.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        editemail = view.findViewById(R.id.profileemailET)
        emailEdittext = view.findViewById(R.id.profile_email)
        emailTextview = view.findViewById(R.id.emailEditText)
        editname = view.findViewById(R.id.profilenameET)
        editphonenumber = view.findViewById(R.id.profile_phonenumberET)
        nameTextview = view.findViewById(R.id.nameEditText)
        phoneNumberTextview = view.findViewById(R.id.phoneNumberEditText)
        nameEdittext = view.findViewById(R.id.profile_name)
        phonenumberEdittext = view.findViewById(R.id.profile_phonenumber)
        updateButton = view.findViewById(R.id.updateButton)
        saveButton = view.findViewById(R.id.saveButton)
        profileImageView = view.findViewById(R.id.profile_image)
        editProfileImageButton = view.findViewById(R.id.editProfileImageButton)
        updateButton.visibility = View.VISIBLE

        db.child("users").child(userId).child("profile").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.value as? Map<String, Any>
                if (userData != null) {
                    nameTextview.text = userData["name"].toString()
                    emailTextview.text = userData["email"].toString()
                    phoneNumberTextview.text = userData["phonenumber"].toString()

                    // Fetch and display profile picture
                    val profileImageURL = userData["profileImage"].toString()
                    if (profileImageURL.isNotBlank()) {
                        Picasso.get().load(profileImageURL).into(profileImageView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Error", "Unable to fetch data: ${error.message}")
            }
        })

        editProfileImageButton.setOnClickListener {
            openImageChooser()
        }

        updateButton.setOnClickListener {
            emailTextview.visibility = View.GONE
            nameTextview.visibility = View.GONE
            phoneNumberTextview.visibility = View.GONE
            updateButton.visibility = View.GONE
            nameEdittext.visibility = View.VISIBLE
            emailEdittext.visibility = View.VISIBLE
            phonenumberEdittext.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
            editProfileImageButton.visibility = View.VISIBLE
            editname.text = Editable.Factory.getInstance().newEditable(nameTextview.text.toString())
            editphonenumber.text =
                Editable.Factory.getInstance().newEditable(phoneNumberTextview.text.toString())
            editemail.text = Editable.Factory.getInstance().newEditable(emailTextview.text.toString())
        }

        saveButton.setOnClickListener {
            emailTextview.visibility = View.VISIBLE
            nameTextview.visibility = View.VISIBLE
            phoneNumberTextview.visibility = View.VISIBLE
            updateButton.visibility = View.VISIBLE
            nameEdittext.visibility = View.GONE
            phonenumberEdittext.visibility = View.GONE
            emailEdittext.visibility = View.GONE
            saveButton.visibility = View.GONE
            editProfileImageButton.visibility  =  View.GONE

            val userData = hashMapOf(
                "name" to editname.text.toString(),
                "email" to editemail.text.toString(),
                "phonenumber" to editphonenumber.text.toString()// Assume you are using emailTextview for profileImage URL
            )
            db.child("users").child(userId).child("profile").updateChildren(userData as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d("success", "Data successfully updated")
                }
                .addOnFailureListener { e ->
                    Log.d("failure", "Error saving user data: ${e.message}")
                }
        }
        return view
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            profileImageView.setImageURI(imageUri)
            uploadProfileImage(imageUri)
        }
    }

    private fun uploadProfileImage(imageUri: Uri?) {
        val imageRef = storageRef.child("profile_images").child("$userId.jpg")
        imageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val userData = hashMapOf(
                        "profileImage" to uri.toString()
                    )
                    db.child("users").child(userId).child("profile").updateChildren(userData as Map<String, Any>)
                        .addOnSuccessListener {
                            Log.d("success", "Profile image uploaded and URL saved")
                        }
                        .addOnFailureListener { e ->
                            Log.d("failure", "Error saving profile image URL: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.d("failure", "Error uploading profile image: ${e.message}")
            }
    }
}
