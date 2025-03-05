package ui.view.fragment.independent
import android.os.Bundle
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
import com.example.deliveryfoodchef.R
import com.example.deliveryfoodchef.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ui.view.custom.showNotificationDialog
import ui.viewmodel.AppViewModel
import ui.viewmodel.authentication.AuthenticationViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class LoginFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentLoginBinding
    private lateinit var appViewModel : AppViewModel
    private lateinit var authViewModel : AuthenticationViewModel
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
        binding = FragmentLoginBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        initialize()
        observeStates()
        listenEvent()
        return binding.root
    }

    private fun initialize() {
        appViewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        navController = findNavController(requireActivity(), R.id.nav_host_container)
    }

    private fun observeStates() {
        observeNavigationState()
        observeMessageState()
    }

    private fun observeMessageState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                authViewModel.messageState.collect {
                    context?.showNotificationDialog(it)
                }
            }
        }
    }

    private fun observeNavigationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                authViewModel.navigationState.collect {
                    appViewModel.startObserveMessage()
                    navController.navigateUp()
                }
            }
        }
    }

    private fun listenEvent() {
        binding.loginButton.setOnClickListener {
            authViewModel.login(binding.accountTitleEdittext.text.toString().trim(), binding.passwordTitleEdittext.text.toString().trim())
        }
    }
}