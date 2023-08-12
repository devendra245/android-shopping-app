package com.example.myapplication



import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.videoView)
        val videoPath = "android.resource://" + packageName + "/" + R.raw.splash // Replace 'your_video_file' with the actual video file name
        videoView.setVideoURI(Uri.parse(videoPath))
        videoView.setOnCompletionListener {
            checkLoginStatus()
        }
        videoView.start()
    }

    private fun checkLoginStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already logged in, proceed to the main activity
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        } else {
            // User is not logged in, proceed to the login activity
            startActivity(Intent(this@SplashActivity, Login::class.java))
        }
        finish()
    }
}

