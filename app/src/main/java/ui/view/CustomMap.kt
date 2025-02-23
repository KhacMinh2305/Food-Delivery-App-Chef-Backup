package ui.view
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.deliveryfoodchef.R

class CustomMap(private val context : Context, private val attrs : AttributeSet) : ConstraintLayout(context, attrs) {

    private val mapView by lazy {
        findViewById<CustomMapWidget>(R.id.custom_map)
    }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.map_layout, this, true)
    }

    fun setMoney(money : List<Double>) {
        mapView.setMoney(money)
    }

    private var oldMoveX = 0
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                oldMoveX = event.x.toInt()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val newMoveX = event.x.toInt()
                val newMoveY = event.y.toInt()
                mapView.onUserSwipe(newMoveX, newMoveY, newMoveX - oldMoveX)
                oldMoveX = newMoveX
                return true
            }
            MotionEvent.ACTION_UP -> {
                oldMoveX = 0
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}