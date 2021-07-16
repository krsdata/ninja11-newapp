package ninja.cricks.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.edify.atrist.listener.OnMatchTimerStarted
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import ninja.cricks.BuildConfig
import ninja.cricks.ContestActivity
import ninja.cricks.MainActivity
import ninja.cricks.R
import ninja.cricks.adaptors.BannerSliderAdapter
import ninja.cricks.adaptors.JoinedMatchesAdapter
import ninja.cricks.databinding.FragmentHomeBinding
import ninja.cricks.models.JoinedMatchModel
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.dashboard.FixtureCricketFragment
import ninja.cricks.ui.mymatches.MyCompletedMatchesFragment
import ninja.cricks.ui.mymatches.MyLiveMatchesFragment
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    var mContext: Context? = null
    private lateinit var mBinding: FragmentHomeBinding

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireContext()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home, container, false
        )
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        mBinding.tabs.addTab(mBinding.tabs.newTab().setText(getString(R.string.mymatch_upcoming)))
        mBinding.tabs.addTab(mBinding.tabs.newTab().setText(getString(R.string.mymatch_live)))
        mBinding.tabs.addTab(mBinding.tabs.newTab().setText(getString(R.string.mymatch_completed)))
        mBinding.tabs.tabGravity = TabLayout.GRAVITY_FILL
        val adapter = MyAdapter(childFragmentManager, mBinding.tabs.tabCount)
        mBinding.viewpager.adapter = adapter

        mBinding.viewpager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mBinding.tabs))

        mBinding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                mBinding.viewpager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val tab = mBinding.tabs.getTabAt(0)
        tab!!.select()
    }

    override fun onResume() {
        super.onResume()
        getMessage()
    }

    private fun getMessage() {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            return
        }

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("version_code", BuildConfig.VERSION_CODE)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getMessages(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {

                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {
                    if (isVisible) {
                        val resObje = response!!.body().toString()
                        val jsonObject = JSONObject(resObje)
                        if (jsonObject.optBoolean("status")) {
                            val array = jsonObject.getJSONArray("data")
                            val data = array.getJSONObject(0)
                            if (data.optInt("message_status") == 0) {
                                mBinding!!.messageCard.visibility = View.GONE
                            } else {
                                if (data.getString("message_type") == "HTML") {
                                    mBinding!!.labelMessage.linksClickable = true
                                    mBinding!!.labelMessage.movementMethod =
                                        LinkMovementMethod.getInstance()
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        mBinding!!.labelMessage.text =
                                            Html.fromHtml(
                                                data.getString("message"),
                                                Html.FROM_HTML_MODE_COMPACT
                                            )
                                    } else {
                                        mBinding!!.labelMessage.text = Html.fromHtml(
                                            data.getString("message")
                                        )
                                    }
                                } else {
                                    mBinding!!.labelMessage.text = data.getString("message")
                                }
                                mBinding!!.messageCard.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            })
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