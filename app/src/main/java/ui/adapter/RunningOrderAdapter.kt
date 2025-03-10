package ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.deliveryfoodchef.databinding.OrderItemBinding
import ui.model.RunningOrder

class RunningOrderAdapter(
    private val clickEvent: (String) -> Unit,
    private val doneEvent: (String) -> Unit,
    private val cancelEvent: (String) -> Unit
) : RecyclerView.Adapter<RunningOrderAdapter.RunningOrderViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<RunningOrder>() {
        override fun areItemsTheSame(oldItem: RunningOrder, newItem: RunningOrder): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: RunningOrder, newItem: RunningOrder): Boolean {
            var isSame = true
            isSame = isSame && oldItem.orderId == newItem.orderId
            isSame = isSame && oldItem.image == newItem.image
            isSame = isSame && oldItem.tag == newItem.tag
            isSame = isSame && oldItem.totalItem == newItem.totalItem
            isSame = isSame && oldItem.price == newItem.price
            return isSame
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list: List<RunningOrder>) {
        differ.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunningOrderViewHolder {
        val binding = OrderItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RunningOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunningOrderViewHolder, position: Int) {
        val runningOrder = differ.currentList[position]
        runningOrder?.let {
            holder.bind(it)
            holder.listenEvent(it.orderId)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class RunningOrderViewHolder(private val binding: OrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(runningOrder: RunningOrder) {
            with(binding) {
                item = runningOrder
            }
        }

        fun listenEvent(orderId : String) {
            binding.root.setOnClickListener {
                clickEvent.invoke(orderId)
            }
            binding.btnDone.setOnClickListener {
                doneEvent.invoke(orderId)
            }
            binding.btnCancel.setOnClickListener {
                cancelEvent.invoke(orderId)
            }
        }
    }
}