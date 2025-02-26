package com.example.deliveryfoodchef
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.deliveryfoodchef.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ui.viewmodel.AppViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var binding : ActivityMainBinding? = null
    private lateinit var viewmodel : AppViewModel
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        setBottomNavAndStatusBarVisibility()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main)
            .apply { lifecycleOwner = this@MainActivity }
        initialize()
        observeState()
    }

    private fun initialize() {
        viewmodel = ViewModelProvider(this)[AppViewModel::class.java]
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        navHostFragment.also {
            this@MainActivity.navController = it.navController
            binding!!.bottomNavView.setupWithNavController(navController)
        }
        viewmodel.checkUserLoggedIn()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.navigationState.collect {
                    if(it) {
                        withContext(Dispatchers.Main) {
                            navController.navigate(R.id.action_login)
                        }
                        return@collect
                    }
                    Log.d("TAG", "OK ! cho home load data ")
                }
            }
        }
    }

    private fun setBottomNavAndStatusBarVisibility(mode : Boolean = false) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            binding?.apply { root.fitsSystemWindows = mode }
            if(!mode) {
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.statusBars())
                hide(WindowInsetsCompat.Type.navigationBars())
                return@apply
            }
            show(WindowInsetsCompat.Type.statusBars())
            show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}

/*(findViewById<CustomMap>(R.id.map) as CustomMap).setMoney(listOf(
            17.32, 43.98, 67.15, 25.41, 8.76, 41.09, 27.62, 15.83, 40.55, 39.21, 35.90, 13.74,
            58.18, 42.57, 8.33, 36.96, 45.29, 19.67, 23.84, 31.12, 19.48, 17.03, 34.75, 22.86
        ))*/