package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import java.util.*

class Home : Fragment() {
    private lateinit var shoesButton: ImageButton
    private lateinit var shirtButton: ImageButton
    private lateinit var jeanButton: ImageButton
    private lateinit var TopButton: ImageButton
    private lateinit var GjeanButton: ImageButton
    private lateinit var GshoesButton: ImageButton
    private lateinit var WAtchButton: ImageButton
    private lateinit var GwatchButton: ImageButton
    private lateinit var SpectaclesButton: ImageButton
    private lateinit var viewPager: ViewPager
    private val imageList = arrayOf(R.drawable.img_1, R.drawable.img_7, R.drawable.img, R.drawable.img_3,R.drawable.img_8,R.drawable.img_4,R.drawable.img_5,R.drawable.img_6)
    private var currentPage = 0
    private val delayTime: Long = 2000 // 2 seconds
    private var handler: Handler = Handler()

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (currentPage == imageList.size) {
                currentPage = 0
            }
            viewPager.setCurrentItem(currentPage++, true)
            handler.postDelayed(this, delayTime)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        viewPager = view.findViewById(R.id.viewPager)
        shoesButton = view.findViewById(R.id.shoes)
        shirtButton = view.findViewById(R.id.shirt)
        jeanButton=view.findViewById(R.id.jean)
        TopButton=view.findViewById(R.id.gtop)
        GjeanButton=view.findViewById(R.id.gjean)
        GshoesButton=view.findViewById(R.id.gshoes)
        WAtchButton=view.findViewById(R.id.watch)
        GwatchButton=view.findViewById(R.id.swatch)
        SpectaclesButton=view.findViewById(R.id.spectacles)

        val adapter = ImagePagerAdapter(imageList, requireContext())
        viewPager.adapter = adapter
        shoesButton.setOnClickListener {
            // Navigate to ShoesFragment when "shoes" image button is clicked
            val intent = Intent(requireContext(), ShoesActivity::class.java)
            startActivity(intent)
        }

        shirtButton.setOnClickListener {
            val intent = Intent(requireContext(), ShirtActivity::class.java)
            startActivity(intent)
        }
        jeanButton.setOnClickListener {
            val intent = Intent(requireContext(), JeanActivity::class.java)
            startActivity(intent)
        }
        TopButton.setOnClickListener {
            val intent = Intent(requireContext(), TopActivity::class.java)
            startActivity(intent)
        }
        GjeanButton.setOnClickListener {
            val intent = Intent(requireContext(), GjeansActivity::class.java)
            startActivity(intent)
        }
        GshoesButton.setOnClickListener {
            val intent = Intent(requireContext(), GshoesActivity::class.java)
            startActivity(intent)
        }
        WAtchButton.setOnClickListener {
            val intent = Intent(requireContext(), WatchActivity::class.java)
            startActivity(intent)
        }
        GwatchButton.setOnClickListener {
            val intent = Intent(requireContext(), GwatchActivity::class.java)
            startActivity(intent)
        }
        SpectaclesButton.setOnClickListener {
            val intent = Intent(requireContext(), SpectaclesActivity::class.java)
            startActivity(intent)
        }




        // Start automatic image slideshow
        handler.postDelayed(runnable, delayTime)

        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Stop the slideshow when the fragment is destroyed or paused
        handler.removeCallbacks(runnable)
    }
}
