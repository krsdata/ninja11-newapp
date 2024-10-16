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
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import ninja.cricks.R
import ninja.cricks.ui.mymatches.MyCompletedMatchesFragment
import ninja.cricks.ui.mymatches.MyLiveMatchesFragment
import ninja.cricks.ui.mymatches.MyUpcomingMatchesFragment

class MyMatchesFragment : Fragment() {
    var tabLayout: TabLayout? = null
    var viewpager: ViewPager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_mymatches, container, false)
        viewpager = root.findViewById(R.id.viewpager)
        tabLayout = root.findViewById(R.id.tabs)
        setupViewPager()
        return root
    }

    private fun setupViewPager() {

        tabLayout?.addTab(tabLayout!!.newTab().setText(getString(R.string.mymatch_upcoming)))
        tabLayout?.addTab(tabLayout!!.newTab().setText(getString(R.string.mymatch_live)))
        tabLayout?.addTab(tabLayout!!.newTab().setText(getString(R.string.mymatch_completed)))
        tabLayout?.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = MyAdapter(childFragmentManager, tabLayout!!.tabCount)
        viewpager?.adapter = adapter

        viewpager?.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))

        tabLayout!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewpager?.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val tab = tabLayout!!.getTabAt(0)
        tab?.select()
    }

    class MyAdapter internal constructor(fm: FragmentManager?, var totalTabs: Int) :
        FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> MyUpcomingMatchesFragment()
                1 -> MyLiveMatchesFragment()
                else -> MyCompletedMatchesFragment()
            }
        }

        override fun getCount(): Int {
            return totalTabs
        }
    }
}