package ninja.cricks.ui.home

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ninja.cricks.BuildConfig
import ninja.cricks.Constant
import ninja.cricks.MainActivity
import ninja.cricks.R
import ninja.cricks.databinding.FragmentHomeBinding
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.roomDatabase.ResponseDatabase
import ninja.cricks.ui.dashboard.FixtureCricketFragment
import ninja.cricks.ui.mymatches.MyCompletedMatchesFragment
import ninja.cricks.ui.mymatches.MyLiveMatchesFragment
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    var mContext: Context? = null
    private lateinit var mBinding: FragmentHomeBinding

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
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
        getMessage()
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


    private fun getMessage() {
        val lastTimeApiCall: Long? = MyPreferences.getLastTimeForApiCall(requireContext(),
            (Constant.getMessagesDatabaseId)
        )
        if (lastTimeApiCall!!+ Constant.delayApiSeconds < System.currentTimeMillis()) {
            // if (activity != null && isAdded) {
            getMessageApiCall()
            //   }
        }
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val value = ResponseDatabase.getInstance(requireContext()).responseDao().getResponseJsonObject(
                    (Constant.getMessagesDatabaseId)
                )

                if (value != null && value.type == (Constant.getMessagesDatabaseId)){
                    withContext(Dispatchers.Main){getMessage2(value.res)}
                }
                else {
                    withContext(Dispatchers.Main){
                        getMessageApiCall()
                    }
                }
            }
        }
    }

    private fun getMessageApiCall() {
        if (!MyUtils.isConnectedWithInternet(requireActivity())) {
            return
        }

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireContext()))
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireContext()))
        jsonRequest.addProperty("version_code", BuildConfig.VERSION_CODE)

        WebServiceClient(requireContext()).client.create(IApiMethod::class.java)
            .getMessages(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {
                    val resObje = response!!.body().toString()
                    val jsonObject = JSONObject(resObje)
                    if (jsonObject.optBoolean("status")) {
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main){ getMessage2(response.body()!!) }
                            withContext(Dispatchers.IO){
                                MyPreferences.saveLastTimeForApiCall(requireContext(),Constant.getMessagesDatabaseId, System.currentTimeMillis())
                                ResponseDatabase.getInstance(requireContext()).responseDao().saveResponseJsonObject(ninja.cricks.roomDatabase.ResponseJsonObject(
                                    (Constant.getMessagesDatabaseId),System.currentTimeMillis(),
                                    response.body()!!
                                ))
                            }
                        }
                    }
                }
            })
    }

    private fun getMessage2(resObje: JsonObject) {
        val jsonObject = JSONObject(resObje.toString())
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
