package ninja.cricks.ui.contest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.edify.atrist.listener.OnContestEvents
import com.edify.atrist.listener.OnContestLoadedListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ninja.cricks.*
import ninja.cricks.databinding.FragmentAllContestBinding
import ninja.cricks.models.*
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.roomDatabase.ResponseDatabase
import ninja.cricks.ui.contest.adaptors.ContestAdapter
import ninja.cricks.ui.contest.adaptors.ContestListAdapter
import ninja.cricks.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ContestFragment : Fragment() {

    private var customProgressDialog2: CustomProgressDialog2? = null
    private var objectMatches: UpcomingMatchesModel? = null
    var matchObject: UpcomingMatchesModel? = null
    var mListenerContestEvents: OnContestEvents? = null
    private lateinit var mListener: OnContestLoadedListener
    private var mBinding: FragmentAllContestBinding? = null
    lateinit var adapter: ContestAdapter
    private lateinit var spotSizeFilterAdaptor: ContestListAdapter
    var filterSpotsListData = ArrayList<ContestModelLists>()
    var filterArrayList = ArrayList<ContestCategoryModel>()
    var isEntryAscending = false
    private var isVisibleToUser: Boolean = false
    private var filterAdapter: FilterAdapter? = null
    private var pos = 0

    companion object {
        private val TAG: String = ContestFragment::class.java.simpleName
        fun newInstance(bundle: Bundle): ContestFragment {
            val fragment = ContestFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customProgressDialog2 = CustomProgressDialog2(context)
        objectMatches =
            requireArguments().get(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT) as UpcomingMatchesModel
        matchObject = objectMatches
        getAllContest(true)
        parentFragmentManager.setFragmentResultListener(CreateTeamActivity.CREATETEAM_REQUESTCODE.toString(),this,
            { s: String, bundle: Bundle ->
                if (bundle.get(ContestActivity.SERIALIZABLE_KEY_CREATE_TEAM) == "result_ok") {
                    allContestsApiCall(true)
                }
            })
        parentFragmentManager.setFragmentResultListener("filter", this, {
                s: String, bundle: Bundle ->
            (activity as ContestActivity).filterContestList()
            if ((activity as ContestActivity).filteredAllContestListData.isNotEmpty()) {
                allContest((activity as ContestActivity).filteredAllContestListData)
                mBinding!!.linearEmptyContest.visibility = View.GONE
            }
            else if (mBinding != null) {
                mBinding!!.linearEmptyContest.visibility = View.VISIBLE
            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_all_contest, container, false
        )
        mBinding!!.contestViewRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        adapter = ContestAdapter(
            requireActivity(),
            (activity as ContestActivity).filteredAllContestListData,
            matchObject,
            mListenerContestEvents!!
        )
        mBinding!!.contestViewRecycler.adapter = adapter
        mBinding!!.recyclerBySpotsize.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        spotSizeFilterAdaptor = ContestListAdapter(
            requireActivity(),
            filterSpotsListData,
            matchObject!!,
            mListenerContestEvents,
            0
        )
        mBinding!!.recyclerBySpotsize.adapter = spotSizeFilterAdaptor
        mBinding!!.contestViewRecycler.adapter = adapter
        mBinding!!.filterRecyclerView.layoutManager =
            LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        filterAdapter = FilterAdapter(requireActivity(), filterArrayList)
        mBinding!!.filterRecyclerView.adapter = filterAdapter
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.linearEmptyContest.visibility = View.GONE
        registerSpotSizeSelection()
        mBinding!!.linearEmptyContest.visibility = View.GONE
        mBinding!!.imgFilter.setOnClickListener{
          var filterFragment = FilterFragment()
            filterFragment.show(parentFragmentManager,"filter")
        }

        mBinding!!.btnCreateTeam.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, CreateTeamActivity::class.java)
            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
            requireActivity().startActivityForResult(
                intent,
                CreateTeamActivity.CREATETEAM_REQUESTCODE
            )
        })

        mBinding!!.btnEmptyView.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_PRIVACY_POLICY)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_PRIVACY)
            requireActivity().startActivity(intent)
        })

        mBinding!!.contestFilterRefresh.setColorSchemeResources(R.color.colorPrimary)
        mBinding!!.contestRefresh.setColorSchemeResources(R.color.colorPrimary)

        mBinding!!.contestRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            allContestsApiCall(true)
        })

        mBinding!!.contestFilterRefresh.setOnRefreshListener {
            getFilteredContest()
        }
    }

    private fun registerSpotSizeSelection() {

        mBinding!!.sortBy2spots.setOnClickListener {

            mBinding!!.sortBy2spots.setBackgroundResource(R.drawable.circle_app_color)
            mBinding!!.sortBy2spots.setTextColor(resources.getColor(R.color.white))

            mBinding!!.sortBy3spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy3spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy4spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy4spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.filterByAll.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.filterByAll.setTextColor(resources.getColor(R.color.black))

            mBinding!!.linearEntryPrizeSort.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.prizeArrow.visibility = View.GONE
            mBinding!!.rupees.setTextColor(resources.getColor(R.color.black))

            showRecyclerListBySpotSize(2, (activity as ContestActivity).filteredAllContestListData)
        }

        mBinding!!.sortBy3spots.setOnClickListener {

            mBinding!!.sortBy2spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy2spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy3spots.setBackgroundResource(R.drawable.circle_app_color)
            mBinding!!.sortBy3spots.setTextColor(resources.getColor(R.color.white))

            mBinding!!.sortBy4spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy4spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.filterByAll.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.filterByAll.setTextColor(resources.getColor(R.color.black))

            mBinding!!.linearEntryPrizeSort.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.prizeArrow.visibility = View.GONE
            mBinding!!.rupees.setTextColor(resources.getColor(R.color.black))

            showRecyclerListBySpotSize(3,(activity as ContestActivity).filteredAllContestListData)
        }

        mBinding!!.sortBy4spots.setOnClickListener {

            mBinding!!.sortBy2spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy2spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy3spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy3spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy4spots.setBackgroundResource(R.drawable.circle_app_color)
            mBinding!!.sortBy4spots.setTextColor(resources.getColor(R.color.white))

            mBinding!!.filterByAll.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.filterByAll.setTextColor(resources.getColor(R.color.black))

            mBinding!!.linearEntryPrizeSort.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.prizeArrow.visibility = View.GONE
            mBinding!!.rupees.setTextColor(resources.getColor(R.color.black))

            showRecyclerListBySpotSize(4, (activity as ContestActivity).filteredAllContestListData)
        }

        mBinding!!.linearEntryPrizeSort.setOnClickListener {

            mBinding!!.sortBy2spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy2spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy3spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy3spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy4spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy4spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.filterByAll.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.filterByAll.setTextColor(resources.getColor(R.color.black))

            mBinding!!.linearEntryPrizeSort.setBackgroundResource(R.drawable.circle_app_color)
            mBinding!!.prizeArrow.visibility = View.VISIBLE

            mBinding!!.rupees.setTextColor(resources.getColor(R.color.white))

            filterByEntryPrize((activity as ContestActivity).filteredAllContestListData)
        }

        mBinding!!.filterByAll.setOnClickListener {
            selectAllContest()
        }
    }

    private fun selectAllContest() {
        showAllContestRecycler()

        mBinding!!.linearEntryPrizeSort.setBackgroundResource(R.drawable.circle_grey)
        mBinding!!.rupees.setTextColor(resources.getColor(R.color.black))
        mBinding!!.prizeArrow.visibility = View.GONE

        mBinding!!.sortBy2spots.setBackgroundResource(R.drawable.circle_grey)
        mBinding!!.sortBy2spots.setTextColor(resources.getColor(R.color.black))

        mBinding!!.sortBy3spots.setBackgroundResource(R.drawable.circle_grey)
        mBinding!!.sortBy3spots.setTextColor(resources.getColor(R.color.black))

        mBinding!!.sortBy4spots.setBackgroundResource(R.drawable.circle_grey)
        mBinding!!.sortBy4spots.setTextColor(resources.getColor(R.color.black))

        mBinding!!.filterByAll.setBackgroundResource(R.drawable.circle_app_color)
        mBinding!!.filterByAll.setTextColor(resources.getColor(R.color.white))
    }

    private fun showAllContestRecycler() {
        var isAllContestListEmpty = true
        for (i in (activity as ContestActivity).filteredAllContestListData) {
            if (i.allContestsRunning!!.isNotEmpty()) {
                isAllContestListEmpty = false
            }
        }
        if (isAllContestListEmpty) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
            mBinding!!.btnCreateTeam.visibility = View.GONE
            mBinding!!.contestViewRecycler.visibility = View.GONE
            mBinding!!.contestRefresh.visibility = View.GONE
            mBinding!!.contestFilterRefresh.visibility = View.VISIBLE
            mBinding!!.recyclerBySpotsize.visibility = View.VISIBLE
        }
        else {
            mBinding!!.btnCreateTeam.visibility = View.VISIBLE
            mBinding!!.linearEmptyContest.visibility = View.GONE
            mBinding!!.contestViewRecycler.visibility = View.VISIBLE
            mBinding!!.contestRefresh.visibility = View.VISIBLE
            mBinding!!.contestFilterRefresh.visibility = View.GONE
            mBinding!!.recyclerBySpotsize.visibility = View.GONE
        }
    }

    private fun showFilteredContestRecycler() {
        mBinding!!.btnCreateTeam.visibility = View.VISIBLE
        mBinding!!.contestViewRecycler.visibility = View.GONE
        mBinding!!.contestRefresh.visibility = View.GONE
        mBinding!!.contestFilterRefresh.visibility = View.VISIBLE
        mBinding!!.recyclerBySpotsize.visibility = View.VISIBLE
    }

    private fun filterByEntryPrize(allContestListData: ArrayList<ContestsParentModels>) {

        isEntryAscending = !isEntryAscending
        if (isEntryAscending) {
            mBinding!!.prizeArrow.setImageResource(R.drawable.ic_baseline_arrow_upward_24)
        } else {
            mBinding!!.prizeArrow.setImageResource(R.drawable.ic_baseline_arrow_downward_24)
        }
        mBinding!!.contestViewRecycler.visibility = View.GONE
        mBinding!!.recyclerBySpotsize.visibility = View.VISIBLE
        filterSpotsListData.clear()
        val filterValues = ArrayList<ContestModelLists>()
        for (i in 0..allContestListData.size - 1) {
            val values = allContestListData.get(i).allContestsRunning
            if (values != null && values.size > 0) {

                if (isEntryAscending) {
                    val sortedEntryPrizes = values.sortedBy { it -> it.entryFees }
                    sortedEntryPrizes.forEach { s -> filterValues.add(s) }
                } else {
                    val sortedEntryPrizes = values.sortedByDescending { it -> it.entryFees }
                    sortedEntryPrizes.forEach { s -> filterValues.add(s) }
                }
            }
        }

        if (isEntryAscending) {
            val objectPrize = filterValues.sortedBy { it -> it.entryFees }
            objectPrize.forEach { s -> filterSpotsListData.add(s) }
        } else {
            val objectPrize = filterValues.sortedByDescending { it -> it.entryFees }
            objectPrize.forEach { s -> filterSpotsListData.add(s) }
        }
        spotSizeFilterAdaptor.notifyDataSetChanged()
        mBinding!!.recyclerBySpotsize.scheduleLayoutAnimation()
    }

    private fun showRecyclerListBySpotSize(spotSize: Int, allContestListData: ArrayList<ContestsParentModels>) {
        mBinding!!.contestViewRecycler.visibility = View.GONE
        mBinding!!.recyclerBySpotsize.visibility = View.VISIBLE
        filterSpotsListData.clear()
        for (i in 0..allContestListData.size - 1) {
            val values = allContestListData.get(i).allContestsRunning
            if (values != null && values.size > 0) {
                for (j in 0..values.size - 1) {
                    val spotObject = values.get(j)
                    if (4 == spotSize && spotObject.totalSpots >= 4) {
                        filterSpotsListData.add(spotObject)
                    } else {
                        if (spotObject.totalSpots == spotSize) {
                            filterSpotsListData.add(spotObject)
                        }
                    }
                }
            }
        }

        spotSizeFilterAdaptor.notifyDataSetChanged()
        mBinding!!.recyclerBySpotsize.smoothScrollToPosition(spotSizeFilterAdaptor.itemCount - 1)
    }

    override fun onResume() {
        super.onResume()
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            customProgressDialog2?.dismiss()
            return
        }
        if ((activity as ContestActivity).filteredAllContestListData.isNotEmpty()) {
            allContest((activity as ContestActivity).filteredAllContestListData)
        }
//        else if (mBinding != null) {
//            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
//        }
        //pos = 0
        Log.e(TAG, "pos =======> $pos")
//        for (i in filterArrayList.indices) {
//            filterArrayList[i].isStatus = pos == i
//        }
//
//        filterAdapter.updateRecord(filterArrayList)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnContestLoadedListener) {
            mListener = context
        } else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }

        if (context is OnContestEvents) {
            mListenerContestEvents = context
        } else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }
    }

    private fun getAllContest(isLoading: Boolean) {
            val lastTimeApiCall: Long? = MyPreferences.getLastTimeForApiCall(requireContext(),
                (Constant.contestFragmentDatabaseId+matchObject!!.matchId)
            )
        if (lastTimeApiCall!!+Constant.delayApiSeconds < System.currentTimeMillis()) {
            allContestsApiCall(isLoading)
        }
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val value = ResponseDatabase.getInstance(requireContext()).responseDao().getResponse((Constant.contestFragmentDatabaseId+ matchObject!!.matchId).toLong())

                if (value != null && value.type == (Constant.contestFragmentDatabaseId + matchObject!!.matchId)){
                    withContext(Dispatchers.Main){allContests(value.res)}
                }
                else {
                    withContext(Dispatchers.Main){allContestsApiCall(isLoading)}
                }
            }
        }

    }

    private fun allContestsApiCall(isLoading: Boolean) {
        if (mBinding != null) {
            mBinding!!.contestRefresh.isRefreshing = false
        }
        //mBinding!!.filterBar.visibility = View.GONE
        if (isLoading)
        //  mBinding!!.progressBar.visibility = View.VISIBLE
            customProgressDialog2!!.show()


        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("match_id", matchObject!!.matchId)

        val gson = Gson()
        val jsonString: String = gson.toJson(
            HardwareInfoManager(requireActivity()).collectData(
                MyPreferences.getDeviceToken(requireActivity())!!
            )
        )
        val deviceDetails: JsonObject = JsonParser().parse(jsonString).asJsonObject
        jsonRequest.add("deviceDetails", deviceDetails)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getContestByMatch(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if (isVisible) {
                        MyUtils.showToast(activity!! as AppCompatActivity, "Something went wrong!!")
                        if (mBinding != null)  mBinding!!.contestRefresh.isRefreshing = false
                        //     mBinding!!.progressBar.visibility = View.GONE
                        customProgressDialog2!!.dismiss()
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (!isVisible) {
                        return
                    }
                    if (mBinding != null) {
                        mBinding!!.contestRefresh.isRefreshing = false
                        //mBinding!!.progressBar.visibility = View.GONE
                    }
                    customProgressDialog2!!.dismiss()
                    val res = response!!.body()
                    if (res != null && res.appMaintainance) {
                        val intent = Intent(activity, MaintainanceActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        activity!!.finish()
                    } else {
                        if (res != null) {
                            if (res.status) {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    withContext(Dispatchers.Main){allContests(res)}
                                    withContext(Dispatchers.IO){
                                        MyPreferences.saveLastTimeForApiCall(context!!,Constant.contestFragmentDatabaseId + matchObject!!.matchId, System.currentTimeMillis())
                                        ResponseDatabase.getInstance(context!!).responseDao().saveResponse(ninja.cricks.roomDatabase.Response(
                                            (Constant.contestFragmentDatabaseId + matchObject!!.matchId),System.currentTimeMillis(),res))
                                    }
                                }

                            } else {
                                if (res.code == 1001) {
                                    MyUtils.showMessage(requireActivity(), res.message)
                                    MyUtils.logoutApp(requireActivity())
                                } else {
                                    MyUtils.showMessage(requireActivity(), res.message)
                                }
                            }
                        }
                    }
                    updateEmptyViews()
                }
            })
    }

    private fun allContests(res: UsersPostDBResponse) {
        customProgressDialog2?.dismiss()
        if (mBinding != null) {
            mBinding!!.contestRefresh.isRefreshing = false
        }
        BindingUtils.currentTimeStamp = res.systemTime
        val responseModel = res.responseObject

        if (responseModel!!.matchContestlist != null && responseModel.matchContestlist!!.isNotEmpty()) {
            (activity as ContestActivity).allContestListData.clear()
            (activity as ContestActivity).allContestListData.addAll(
                responseModel.matchContestlist!!
            )
            (activity as ContestActivity).filterContestList()
            allContest((activity as ContestActivity).filteredAllContestListData)
            mListener.onMyTeam(responseModel.myjoinedTeams!!)
            mListener.onMyContest(responseModel.joinedContestDetails!!)
        }
        else {
            MyUtils.showToast(
                requireActivity() as AppCompatActivity,
                "No Contest available for this match"
            )
        }
    }

    private fun allContest(resAllContestList: ArrayList<ContestsParentModels>) {
        customProgressDialog2!!.dismiss()
        filterArrayList.clear()
        val model = ContestCategoryModel("All", true)
        filterArrayList.add(model)

        for (i in resAllContestList.indices) {
            val categoryModel = ContestCategoryModel(
                resAllContestList[i].contestTitle,
                false
            )
            filterArrayList.add(categoryModel)
            if (!(activity as ContestActivity).filterTitleArray.contains(FilterChipModel(resAllContestList[i].contestTitle, false)) && !(activity as ContestActivity).filterTitleArray.contains(FilterChipModel(resAllContestList[i].contestTitle, true))) {
                (activity as ContestActivity).filterTitleArray.add(FilterChipModel(resAllContestList[i].contestTitle, false))
            }

        }

        Log.e(TAG, "pos =======> $pos")
        /*for (i in filterArrayList.indices) {
            filterArrayList[i].isStatus = pos == i
        }

        filterAdapter.updateRecord(filterArrayList)*/

        adapter.setMatchesList(resAllContestList)
        updateContestData(pos,resAllContestList)
    }

    fun updateEmptyViews() {
        if ((activity as ContestActivity).filteredAllContestListData.size == 0) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        } else {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }
    }

    inner class FilterAdapter(
        val mContext: Activity,
        var arrayList: ArrayList<ContestCategoryModel>
    ) :
        RecyclerView.Adapter<FilterAdapter.ViewHolderJoinedContest>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FilterAdapter.ViewHolderJoinedContest {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.contest_filter_new, parent, false)
            return ViewHolderJoinedContest(view)
        }

        override fun onBindViewHolder(
            holder: FilterAdapter.ViewHolderJoinedContest,
            position: Int
        ) {
            try {
                val categoryModel: ContestCategoryModel = arrayList[position]
                holder.contestTitle.text = categoryModel.name
                if (categoryModel.isStatus) {
//                    holder.filterLayout.background = mContext.resources.getDrawable(R.drawable.new_filter_color_back)
         //           holder.contestTitle.setTextColor(mContext.resources.getColor(R.color.white))
                    holder.contestTitle.setTextColor(mContext.resources.getColor(R.color.green))
                } else {
                    holder.contestTitle.setTextColor(mContext.resources.getColor(R.color.crop__selector_focused))
//                    holder.filterLayout.background = mContext.resources.getDrawable(R.drawable.new_filter_back)
                }
                holder.filterLayout.setOnClickListener(ClickView(position))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        inner class ClickView(position: Int) : View.OnClickListener {
            var pos = position

            override fun onClick(v: View?) {
                updateContestData(pos,(activity as ContestActivity).filteredAllContestListData)
            }
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        fun updateRecord(filterArrayList: ArrayList<ContestCategoryModel>) {
            this.arrayList = filterArrayList
            notifyDataSetChanged()
        }

        inner class ViewHolderJoinedContest(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val contestTitle: TextView = itemView.findViewById(R.id.contest_title)
            val filterLayout: LinearLayout = itemView.findViewById(R.id.filter_layout)
        }
    }

    private fun updateContestData(pos: Int, allContestListData: ArrayList<ContestsParentModels>) {
        for (i in filterArrayList.indices) {
            filterArrayList[i].isStatus = pos == i
        }
        filterAdapter!!.updateRecord(filterArrayList)

        this.pos = pos

        if (pos == 0) {
            showAllContestRecycler()
        } else {

            val actualPosition = pos - 1

            showFilteredContestRecycler()
            filterSpotsListData.clear()

            for (i in allContestListData.indices) {
                if (actualPosition == i) {
                    val values = allContestListData[i].allContestsRunning
                    if (values != null) {
                        filterSpotsListData.addAll(values)
                    }
                }
            }
            spotSizeFilterAdaptor.notifyDataSetChanged()
            if (filterSpotsListData.isEmpty()) {
                mBinding!!.linearEmptyContest.visibility = View.VISIBLE
                mBinding!!.btnCreateTeam.visibility = View.GONE

            }
            else{
                mBinding!!.linearEmptyContest.visibility = View.GONE
                mBinding!!.btnCreateTeam.visibility = View.VISIBLE
            }
        }
    }

    private fun getFilteredContest() {

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("match_id", matchObject!!.matchId)

        val gson = Gson()
        val jsonString: String = gson.toJson(
            HardwareInfoManager(requireActivity()).collectData(
                MyPreferences.getDeviceToken(requireActivity())!!
            )
        )
        val deviceDetails: JsonObject = JsonParser().parse(jsonString).asJsonObject
        jsonRequest.add("deviceDetails", deviceDetails)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getContestByMatch(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if (isVisible) {
                        MyUtils.showToast(activity!! as AppCompatActivity, "Something went wrong!!")
                        mBinding!!.contestFilterRefresh.isRefreshing = false
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (!isVisible) {
                        return
                    }
                    mBinding!!.contestFilterRefresh.isRefreshing = false
                    val res = response!!.body()
                    if (res != null && res.appMaintainance) {
                        val intent = Intent(activity, MaintainanceActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        activity!!.finish()
                    } else
                        if (res != null) {
                            if (res.status) {
                                BindingUtils.currentTimeStamp = res.systemTime
                                val responseModel = res.responseObject
                                if (responseModel!!.matchContestlist != null && responseModel.matchContestlist!!.isNotEmpty()) {
                                    (activity as ContestActivity).allContestListData.clear()
                                    (activity as ContestActivity).allContestListData.addAll(responseModel.matchContestlist!!)
                                    filterArrayList.clear()

                                    val model = ContestCategoryModel("All", false)
                                    filterArrayList.add(model)

                                    val actualPosition = pos - 1

                                    for (i in responseModel.matchContestlist!!.indices) {
                                        if (actualPosition == i) {
                                            val categoryModel = ContestCategoryModel(
                                                responseModel.matchContestlist!![i].contestTitle,
                                                true
                                            )
                                            filterArrayList.add(categoryModel)
                                        } else {
                                            val categoryModel = ContestCategoryModel(
                                                responseModel.matchContestlist!![i].contestTitle,
                                                false
                                            )
                                            filterArrayList.add(categoryModel)
                                        }
                                    }

                                    filterAdapter?.updateRecord(filterArrayList)


                                    showFilteredContestRecycler()
                                    filterSpotsListData.clear()

                                    for (i in (activity as ContestActivity).filteredAllContestListData.indices) {
                                        if (actualPosition == i) {
                                            val values = (activity as ContestActivity).filteredAllContestListData[i].allContestsRunning
                                            if (values != null) {
                                                filterSpotsListData.addAll(values)
                                            }
                                        }
                                    }
                                    spotSizeFilterAdaptor.notifyDataSetChanged()

                                    mListener.onMyTeam(responseModel.myjoinedTeams!!)
                                    mListener.onMyContest(responseModel.joinedContestDetails!!)
                                } else {
                                    MyUtils.showToast(
                                        activity!! as AppCompatActivity,
                                        "No Contest available for this match $res"
                                    )
                                }
                            } else {
                                if (res.code == 1001) {
                                    MyUtils.showMessage(requireActivity(), res.message)
                                    MyUtils.logoutApp(requireActivity())
                                } else {
                                    MyUtils.showMessage(requireActivity(), res.message)
                                }
                            }
                        }
                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CreateTeamActivity.CREATETEAM_REQUESTCODE && resultCode == AppCompatActivity.RESULT_OK) {
            allContestsApiCall(true)
        }
    }
}