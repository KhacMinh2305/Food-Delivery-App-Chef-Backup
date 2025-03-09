package binding
import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.deliveryfoodchef.R
import com.google.android.material.imageview.ShapeableImageView
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object AppBinding {
    @SuppressLint("SetTextI18n")
    @BindingAdapter("decimalValue")
    @JvmStatic
    fun bindTextViewWithUnitValue(view : TextView, value : Float?) {
        value?.let {
            val decimalFormatter = DecimalFormat("#.#")
            val collapseValue = decimalFormatter.format(it)
            view.text = "$$collapseValue"
        }
    }

    @BindingAdapter("drawableSource")
    @JvmStatic
    fun bindWelcomeGuideImageView(view : ImageView, imageSource : Int) {
        view.setImageResource(imageSource)
    }

    @SuppressLint("SetTextI18n")
    @BindingAdapter("leftTime")
    @JvmStatic
    fun bindTimeToResendTextView(view : TextView, time : Int?) {
        view.text = "in. ${time}sec"
    }

    @BindingAdapter("imageSource")
    @JvmStatic
    fun bindHomeImageView(view : ImageView, imageSource : Int) {
        view.setImageResource(imageSource)
    }

    @BindingAdapter("imageUrl")
    @JvmStatic
    fun bindShapeableImageView(view : ShapeableImageView, imageSource : String?) {
        Glide.with(view.context).load(imageSource)
            .error(R.drawable.ic_launcher_background)
            .placeholder(R.drawable.ic_launcher_background).into(view)
    }

    @BindingAdapter("timeValue")
    @JvmStatic
    fun bindMessageTime(view : TextView, time : String) {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(time.toLong()),
            ZoneId.systemDefault()
        )
        val currentTime = "${dateTime.hour}:${if(dateTime.minute < 10) "0${dateTime.minute}" else dateTime.minute}"
        view.text = currentTime
    }

    @BindingAdapter("onlineState")
    @JvmStatic
    fun bindActiveState(view : View, state : Boolean) {
        view.setBackgroundResource(if(state) R.drawable.user_state_drawable else R.drawable.user_offline_state_drawable)
    }

    @BindingAdapter("onlineState")
    @JvmStatic
    fun bindMessageSeenState(view : ImageView, state : Boolean) {
        view.setImageResource(if(state) R.drawable.ic_received else R.drawable.ic_unreceived)
    }
}