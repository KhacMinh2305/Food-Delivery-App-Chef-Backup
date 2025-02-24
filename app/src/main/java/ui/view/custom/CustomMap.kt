package ui.view.custom
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
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
    private var fingerDown = false
    private var fingerMove = false

    private fun clear() {
        fingerDown = false
        fingerMove = false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                oldMoveX = event.x.toInt()
                fingerDown = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val newMoveX = event.x.toInt()
                val newMoveY = event.y.toInt()
                mapView.onUserSwipe(newMoveX, newMoveY, newMoveX - oldMoveX)
                oldMoveX = newMoveX
                fingerMove = true
                return true
            }
            MotionEvent.ACTION_UP -> {
                oldMoveX = 0
                if(fingerDown && fingerMove) mapView.onTouchEventEnd(CustomMapWidget.GestureType.MOVE) else mapView.onTouchEventEnd(
                    CustomMapWidget.GestureType.CLICK
                )
                clear()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}

// Van de : Lam sao de phan biet user vuot va user click de show pop so tien ?
// Doi voi vuot : Khi user cham tay xuong man hinh , su kien se phai trai qua 3 qua trinh : down -> move -> up
// Doi voi click : Chi tra qua 2 event la down va up