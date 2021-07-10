package ninja.cricks.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import ninja.cricks.R
import ninja.cricks.ui.dashboard.FixtureCricketFragment
import ninja.cricks.ui.mymatches.MyCompletedMatchesFragment
import ninja.cricks.ui.mymatches.MyLiveMatchesFragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    var tabLayout: TabLayout? = null
    var viewpager: ViewPager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewpager = view.findViewById(R.id.viewpager)
        tabLayout = view.findViewById(R.id.tabs)
        setupViewPager()
    }

    private fun setupViewPager() {
        tabLayout?.addTab(tabLayout!!.newTab().setText(getString(R.string.mymatch_upcoming)))
        tabLayout?.addTab(tabLayout!!.newTab().setText(getString(R.string.mymatch_live)))
        tabLayout?.addTab(tabLayout!!.newTab().setText(getString(R.string.mymatch_completed)))
        tabLayout?.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = MyAdapter(childFragmentManager, tabLayout!!.tabCount)
        viewpager?.adapter = adapter

        viewpager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewpager?.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val tab = tabLayout!!.getTabAt(0)
        tab?.select()
    }

    inner class MyAdapter(fm: FragmentManager?, var totalTabs: Int) :
        FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> FixtureCricketFragment()
                1 -> MyLiveMatchesFragment()
                else -> MyCompletedMatchesFragment()
            }
        }

        override fun getCount(): Int {
            return totalTabs
        }
    }
}