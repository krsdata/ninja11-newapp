package ninja.cricks.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import ninja.cricks.MainActivity
import ninja.cricks.R
import ninja.cricks.ui.mymatches.MyCompletedMatchesFragment
import ninja.cricks.ui.mymatches.MyLiveMatchesFragment
import ninja.cricks.ui.mymatches.MyUpcomingMatchesFragment

class MyMatchesFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_mymatches, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showToolbar()
        val viewpager: ViewPager = view.findViewById(R.id.viewpager)
        val tabs: TabLayout = view.findViewById(R.id.tabs)
        setupViewPager(viewpager)
        // viewpager.addOnPageChangeListener(this)
        tabs.setupWithViewPager(viewpager)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(requireActivity().supportFragmentManager)
        adapter.addFragment(MyUpcomingMatchesFragment(), getString(R.string.mymatch_upcoming))
        adapter.addFragment(MyLiveMatchesFragment(), getString(R.string.mymatch_live))
        adapter.addFragment(MyCompletedMatchesFragment(), getString(R.string.mymatch_completed))
        viewPager.adapter = adapter
    }

    fun onRefresh() {

    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }
}