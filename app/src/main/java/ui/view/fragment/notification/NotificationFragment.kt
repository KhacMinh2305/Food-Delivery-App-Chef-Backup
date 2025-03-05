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
import com.example.deliveryfoodchef.R
import com.example.deliveryfoodchef.databinding.FragmentNotificationBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ui.adapter.ChattingRoomAdapter
import ui.viewmodel.notification.NotificationViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class NotificationFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentNotificationBinding
    private lateinit var notificationViewModel : NotificationViewModel
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        initialize()
        observeStates()
        listenEvent()
        return binding.root
    }

    private fun initialize() {
        notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java].apply {
            binding.viewmodel = this
        }
        navController = findNavController(requireActivity(), R.id.nav_host_container)
        initViews()
    }

    private fun initViews() {
        initMessageRecyclerView()
    }

    private fun initMessageRecyclerView() {
        binding.messageRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = ChattingRoomAdapter {
                Log.d("TAG", "Click on item : $it")
            }
        }
    }

    private fun observeStates() {
        observeChatRoomState()
        observeRoomsChangedState()
    }

    private fun observeRoomsChangedState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.getRoomEmitter().collect {
                    (binding.messageRecyclerView.adapter as ChattingRoomAdapter).refresh()
                    (binding.messageRecyclerView.adapter as ChattingRoomAdapter).notifyDataSetChanged()
                }
            }
        }
    }

    private fun observeChatRoomState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.chattingRoomsState.collect {
                    (binding.messageRecyclerView.adapter as ChattingRoomAdapter).submitData(viewLifecycleOwner.lifecycle, it)
                }
            }
        }
    }

    private fun listenEvent() {
        changeTab()
    }

    private fun changeTab() {
        binding.orderTypeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                notificationViewModel.changeTabPosition(if(tab?.id == R.id.message_tab) 0 else 1)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}