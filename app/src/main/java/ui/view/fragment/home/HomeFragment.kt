package ui.view.fragment.home
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.deliveryfoodchef.R
import com.example.deliveryfoodchef.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import dagger.hilt.android.AndroidEntryPoint
import ui.adapter.RunningOrderAdapter
import ui.view.other.RecyclerViewItemDecoration
import ui.viewmodel.AppViewModel
import ui.viewmodel.home.HomeViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentHomeBinding
    private lateinit var appViewModel : AppViewModel
    private lateinit var homeViewModel : HomeViewModel
    private lateinit var navController : NavController
    private lateinit var runningOrderSheetBehavior : BottomSheetBehavior<LinearLayout>

    private val sheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if(newState == BottomSheetBehavior.STATE_EXPANDED) appViewModel.changeBottomNavBarVisibility(false)
            else appViewModel.changeBottomNavBarVisibility(true)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

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
        binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        initialize()
        observeStates()
        listenEvents()
        return binding.root
    }

    private fun initialize() {
        appViewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java].apply {
            initialize(1)
            binding.viewmodel = this
        }
        navController = findNavController(requireActivity(), R.id.nav_host_container)
        initViews()
    }

    private fun initViews() {
        runningOrderSheetBehavior = BottomSheetBehavior.from(binding.orderSheet.orderBottomSheet).apply {
            addBottomSheetCallback(sheetCallback)
        }
        with(binding.orderSheet.orderRecyclerView) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = RunningOrderAdapter({ id ->
                // TODO : navigate to other screen
            }, { id ->
                homeViewModel.onOrderDone(id)
            }, { id ->
                homeViewModel.onOrderCanceled(id)
            })
        }
    }

    private fun observeStates() {
        observeIncomeGraphState()
        observeRunningOrders()
    }

    private fun observeIncomeGraphState() {
        homeViewModel.graphData.observe(viewLifecycleOwner) {
            binding.mapView.setMoney(it)
        }
    }

    private fun observeRunningOrders() {
        homeViewModel.runningOrdersData.observe(viewLifecycleOwner) {
            (binding.orderSheet.orderRecyclerView.adapter as RunningOrderAdapter).submitList(it)
        }
    }

    private fun listenEvents() {
        openRunningOrderSheet()
    }

    private fun openRunningOrderSheet() {
        binding.runningOrderContainer.setOnClickListener {
            runningOrderSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runningOrderSheetBehavior.removeBottomSheetCallback(sheetCallback)
    }
}
