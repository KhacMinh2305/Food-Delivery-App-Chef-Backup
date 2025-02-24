package ui.view.custom

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.example.deliveryfoodchef.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class CustomNavBar(private val context : Context, private val attrs : AttributeSet) : BottomNavigationView(context, attrs) {

    init {
        itemActiveIndicatorColor = ColorStateList.valueOf(resources.getColor(R.color.transparent, null))
    }
}