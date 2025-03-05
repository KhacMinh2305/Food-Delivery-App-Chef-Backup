package binding

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.deliveryfoodchef.R
import com.google.android.material.imageview.ShapeableImageView
import java.text.DecimalFormat
import java.time.LocalTime

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

}