package com.example.deliveryfoodchef
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        /*(findViewById<CustomMap>(R.id.map) as CustomMap).setMoney(listOf(
            17.32, 43.98, 67.15, 25.41, 8.76, 41.09, 27.62, 15.83, 40.55, 39.21, 35.90, 13.74,
            58.18, 42.57, 8.33, 36.96, 45.29, 19.67, 23.84, 31.12, 19.48, 17.03, 34.75, 22.86
        ))*/
    }
}