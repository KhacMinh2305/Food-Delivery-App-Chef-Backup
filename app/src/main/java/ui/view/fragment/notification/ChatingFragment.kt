package ui.view.fragment.notification
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.deliveryfoodchef.R
import com.example.deliveryfoodchef.databinding.FragmentChatingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ui.adapter.MessageAdapter
import ui.view.other.RecyclerViewItemDecoration
import ui.viewmodel.AppViewModel
import ui.viewmodel.notification.ChattingViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class ChatingFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private var param1: String? = null
    private var param2: String? = null
    private var roomId : String = ""

    private lateinit var binding : FragmentChatingBinding
    private lateinit var appViewModel : AppViewModel
    private lateinit var chattingViewModel : ChattingViewModel
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            roomId = it.getString("room_id", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatingBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        initialize()
        observeStates()
        listenEvents()
        return binding.root
    }

    private val adapterUpdatingObserve = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            /*the case that position = 0 && itemCount = 1 only happen when the first page (display order) only has 1 item
            Corresponding, the itemCount = 1 and position != 0 only happen when the latest message is inserted*/
            /*A race condition problem happen because Callback of Adapter calculate differences between old list and new list
            asynchronous. It will take time to re-display, but adapter not provide method to inject callback update. This is
            the reason a short delay goes here*/
            if(positionStart != 0 && itemCount == 1) {
                binding.messageRecyclerview.postDelayed({
                    val scrollRange = binding.messageRecyclerview.computeVerticalScrollRange()
                    binding.messageRecyclerview.scrollBy(0, scrollRange)
                }, 200)
            }
        }
    }

    private fun initialize() {
        appViewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java].apply {
            changeBottomNavBarVisibility(false)
        }
        chattingViewModel = ViewModelProvider(this)[ChattingViewModel::class.java].apply {
            initialize(roomId)
        }
        navController = findNavController(requireActivity(), R.id.nav_host_container)
        initViews()
    }

    private fun initViews() {
        binding.messageRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false).apply {
                stackFromEnd = true
            }
            addItemDecoration(RecyclerViewItemDecoration(60))
            adapter = MessageAdapter().apply {
                registerAdapterDataObserver(adapterUpdatingObserve)
            }
        }
    }

    private fun observeStates() {
        observeMessages()
    }

    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                chattingViewModel.messageFlow.collect { data ->
                    (binding.messageRecyclerview.adapter as MessageAdapter).submitData(viewLifecycleOwner.lifecycle, data)
                }
            }
        }
    }

    private fun listenEvents() {
        goBack()
        sendMessage()
    }

    private fun goBack() {
        binding.backButton.setOnClickListener {
            navController.navigateUp()
        }
    }

    private fun sendMessage() {
        binding.sendButton.setOnClickListener {
            val message = binding.chatEdittext.text.toString().trim()
            chattingViewModel.sendMessage(roomId, message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (binding.messageRecyclerview.adapter as MessageAdapter).unregisterAdapterDataObserver(adapterUpdatingObserve)
    }
}