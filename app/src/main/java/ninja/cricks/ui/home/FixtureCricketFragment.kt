package ninja.cricks.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.deliverdas.customers.utils.HardwareInfoManager
import com.google.android.material.snackbar.Snackbar
import ninja.cricks.MainActivity
import ninja.cricks.MaintainanceActivity
import ninja.cricks.SportsFightApplication
import ninja.cricks.UpdateApplicationActivity
import ninja.cricks.adaptors.MatchesAdapter
import ninja.cricks.listener.RecyclerViewLoadMoreScroll
import ninja.cricks.models.MatchesModels
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RequestModel
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.BaseFragment
import ninja.cricks.ui.home.models.UsersPostDBResponse
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ninja.cricks.R
import ninja.cricks.databinding.FragmentAllGamesBinding


class FixtureCricketFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {
    //var listener : OnPageRefreshedListener? =null
    companion object {
        fun newInstance() = FixtureCricketFragment()
        var pageNo  = 1
    }

 //   private lateinit var mainViewModel: MatchesViewModel
    private var mBinding: FragmentAllGamesBinding? = null
    lateinit var adapter: MatchesAdapter
    var allmatchesArrayList = ArrayList<MatchesModels>()
    var scrollListener : RecyclerViewLoadMoreScroll?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_all_games, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).showToolbar()
       // mainViewModel = ViewModelProviders.of(this).get(MatchesViewModel::class.java)
        //mainViewModel = ViewModelProviders.of(this).get(MatchesViewModel::class.java)
        mBinding!!.allGameViewRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        mBinding!!.linearEmptyContest.visibility=View.GONE

        mBinding!!.swipeRefresh.setColorScheme(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light)

        mBinding!!.swipeRefresh.setOnRefreshListener(this)

        // initDummyContent();
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        scrollListener = RecyclerViewLoadMoreScroll(linearLayoutManager)

        mBinding!!.allGameViewRecycler.layoutManager = linearLayoutManager

        var upcomingmatchlist =
            (requireActivity().applicationContext as SportsFightApplication).getUpcomingMatches
        if(upcomingmatchlist!=null && upcomingmatchlist.size>0){
            allmatchesArrayList.clear()
            allmatchesArrayList.addAll(upcomingmatchlist)
        }
        adapter = MatchesAdapter(requireActivity(), allmatchesArrayList)
        mBinding!!.allGameViewRecycler.adapter = adapter
        getAllMatches()

    }


    private fun isValidRequest(): Boolean {

        var offset = 10
        var cal = (pageNo*offset)+1
        if(adapter.itemCount<=cal){
            return  true
        }else {
            return  true
        }
    }

    fun updateEmptyViews(){
        if(allmatchesArrayList.size==0){
            mBinding!!.linearEmptyContest.visibility=View.VISIBLE
            mBinding!!.btnEmptyView.setOnClickListener(View.OnClickListener {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse(BindingUtils.WEBVIEW_TNC)
                startActivity(openURL)
            })

        }else {
            mBinding!!.linearEmptyContest.visibility=View.GONE
        }
    }


    fun getAllMatches() {
//        if(!isVisible()){
//            return
//        }
        if(!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)){
            mBinding!!.swipeRefresh.isRefreshing =false
//            val mSnackbar =
//                Snackbar.make(mBinding!!.linearEmptyContest, , Snackbar.LENGTH_LONG)
//                    .setAction("RETRY") {
//                        getAllMatches()
//                    }
//            mSnackbar.show()



            Snackbar.make(
                activity!!.findViewById(android.R.id.content),
                "NO Internet Connection found!!",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Retry") {
                // Call action functions here
                getAllMatches()
            }.setActionTextColor(resources.getColor(R.color.red)).show()
            return
        }
        mBinding!!.swipeRefresh.isRefreshing = true
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(activity!!)!!
        models.token =MyPreferences.getToken(activity!!)!!
        models.deviceDetails = HardwareInfoManager(context).collectData()

        WebServiceClient(activity!!).client.create(IApiMethod::class.java).getAllMatches(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                   // customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if(isVisible) {
                        mBinding!!.swipeRefresh.isRefreshing = false
                        var resObje = response!!.body()

                        if(resObje != null && resObje!!.appMaintainance){
                            var intent = Intent(activity, MaintainanceActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            activity!!.finish()
                        }else
                        if (resObje != null && resObje.status) {
                            if(resObje.sessionExpired){
                                 logoutApp("Session Expired Please login again!!",false)
                            }else {
                                BindingUtils.currentTimeStamp =  resObje.systemTime
                                var responseObject = resObje.responseObject
                                var listofData = responseObject!!.matchdatalist as ArrayList<MatchesModels>?
                                (activity!!.applicationContext as SportsFightApplication).saveUpcomingMatches(listofData)
                                if(listofData!!.size>0) {
                                    addAllList(listofData)
                                    adapter.setMatchesList(allmatchesArrayList)
                                }
                            }

                        }
                        updateEmptyViews()

                    }
                }

            })

    }


    private fun addAllList(userPostData: java.util.ArrayList<MatchesModels>) {
        if(isValidRequest()) {
            allmatchesArrayList.clear()
            allmatchesArrayList.addAll(userPostData)
        }
    }

//    override fun onAttach(activity: Activity) {
//        super.onAttach(activity)
//        if (activity is OnPageRefreshedListener) {
//            Log.d("Annv - Fragment", "activity " + activity.localClassName)
//           listener = activity as OnPageRefreshedListener
//        }
//    }

    override fun onRefresh() {
        getAllMatches()
    }
}
