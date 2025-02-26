package ui.view.fragment.home
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.example.deliveryfoodchef.R
import com.example.deliveryfoodchef.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

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
        navController = findNavController(requireActivity(), R.id.nav_host_container)
        binding.testText.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_foodDetailFragment)
        }
        return binding.root
    }
}

/*Cau truc thu muc :

Doi voi man hinh ngoai, dau tien se lay danh sach cac room co chua

// Danh sach phong chat lay ve thi cho vao list de tien show cho ui. Moi khi co cap nha tu su kien nghe thi cap nhat vao list,
sau do emit event len ui de update

chat[
    users{
        attending_room[
            user1<id1> {
                rooms : {
                    id1,
                    id2,
                    ...
                }
            }
        ]
    },
    rooms{
        room[
            room<id>{
                latest_message{
                    sender : {
                        id : Int,
                        name : String
                    }
                    content : String,
                    time : String
                }
                members[
                    member1<id1>{
                        id : Int,
                        name : String,
                        image : String,
                        join_time : String,
                        attending : Boolean
                    }
                ],
                messages[
                    message1<id1> {
                        id : String,
                        sender : {
                            id : Int,
                            name : String
                        }
                        content : String,
                        time : String
                    }
                ],
                waiting_messages[
                    member1<id1>{
                        number : Int
                    }
                ]
            }
        ]
    }
]
 * */