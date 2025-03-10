package ui.view.fragment.home
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.example.deliveryfoodchef.R
import com.example.deliveryfoodchef.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
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
    private lateinit var homeViewModel : HomeViewModel
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
        binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        initialize()
        observeStates()
        return binding.root
    }

    private fun initialize() {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java].apply {
            initialize(1)
            binding.viewmodel = this
        }
        navController = findNavController(requireActivity(), R.id.nav_host_container)
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
        
    }
}
