package ipca.example.drawerexample.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.singularity.ipcaplus.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var tabsTitles = arrayOf<String>()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collectionPagerAdapter = CollectionPagerAdapter(childFragmentManager)


        tabsTitles = arrayOf(
            "Chats",
            "Oficias"
        )

        binding.viewPager.adapter = collectionPagerAdapter
        binding.tabs.setupWithViewPager(binding.viewPager)

        fragments.add(BlankFragment())
        fragments.add(Blank2Fragment())

        collectionPagerAdapter.notifyDataSetChanged()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private var fragments: MutableList<Fragment> = ArrayList()
    private lateinit var collectionPagerAdapter: CollectionPagerAdapter

    inner class CollectionPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getCount(): Int  = fragments.count()

        override fun getPageTitle(position: Int): CharSequence {
            return tabsTitles[position]
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

    }
}