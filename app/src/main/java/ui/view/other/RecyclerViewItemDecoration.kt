package ui.view.other
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ui.adapter.MessageAdapter

class RecyclerViewItemDecoration(private val space : Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val currIndex = parent.getChildAdapterPosition(view)
        when(parent.adapter) {
            is MessageAdapter -> outRect.top = if (currIndex == 0) 0 else space
        }
    }
}