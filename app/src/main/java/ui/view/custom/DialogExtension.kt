package ui.view.custom
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.example.deliveryfoodchef.R
import com.example.deliveryfoodchef.databinding.LoadingDialogLayoutBinding
import com.example.deliveryfoodchef.databinding.NotificationDialogBinding

fun Context.showNotificationDialog(message : String) {
    val dialog = Dialog(this)
    val binding = NotificationDialogBinding.inflate(LayoutInflater.from(this), null, false).apply {
        subtitleTextview.text = message
        okTextview.setOnClickListener {
            dialog.cancel()
        }
    }
    dialog.apply {
        setContentView(binding.root)
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            setLayout(
                context.resources.displayMetrics.widthPixels * 7 / 10,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }
    dialog.show()
}

fun Context.showLoadingDialog() : Dialog {
    val dialog = Dialog(this)
    val binding = LoadingDialogLayoutBinding.inflate(LayoutInflater.from(this), null, false).apply {
        Glide.with(this@showLoadingDialog).load(R.drawable.plus_button_foreground).into(imageview)
    }
    dialog.apply {
        setContentView(binding.root)
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        setCancelable(true)
    }
    dialog.show()
    return dialog
}