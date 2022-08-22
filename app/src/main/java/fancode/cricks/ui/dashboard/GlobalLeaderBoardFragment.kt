package fancode.cricks.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import fancode.cricks.R
import fancode.cricks.ViewUserDetailActivity
import fancode.cricks.databinding.FragmentGlobalLeaderBoardBinding
import fancode.cricks.models.ContestLeaderBoardModel
import fancode.cricks.models.DataModel
import fancode.cricks.models.LeaderBoardModel
import fancode.cricks.models.RankModel
import fancode.cricks.network.IApiMethod
import fancode.cricks.network.WebServiceClient
import fancode.cricks.utils.MyPreferences
import fancode.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GlobalLeaderBoardFragment : Fragment() {

    private var mContext: Context? = null
    private var mBinding: FragmentGlobalLeaderBoardBinding? = null
    private var filterList = ArrayList<ContestLeaderBoardModel>()
    private var dataList = ArrayList<DataModel>()
    private var childDataList = ArrayList<DataModel>()
    private var filterAdapter: FilterAdapter? = null
    private var dataAdapter: LeaderBoardDataAdapter? = null
    private var prizeBreakUpAdapter: PriceBreakupAdapter? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var infoMenu: MenuItem? = null
    private var position: Int = 0

    companion object{
        val TAG: String = GlobalLeaderBoardFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_global_leader_board, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /*mBinding!!.toolbar.title = "LeaderBoard Contest"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white, null))
        } else {
            mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        }*/

        mBinding!!.contestFilterRefresh.setColorSchemeColors(mContext!!.resources.getColor(R.color.colorPrimary))
        mBinding!!.contestFilterRefresh.setOnRefreshListener {
            getAllLeaderBoard(false)
        }

        mBinding!!.recyclerView.layoutManager =
            LinearLayoutManager(mContext!!, RecyclerView.VERTICAL, false)

        mBinding!!.filterRecyclerView.layoutManager =
            LinearLayoutManager(mContext!!, RecyclerView.HORIZONTAL, false)
    }

    override fun onResume() {
        super.onResume()
        getAllLeaderBoard(true)
    }

    private fun getAllLeaderBoard(showLoader: Boolean) {
        if (showLoader) {
            mBinding!!.progressBar.visibility = View.VISIBLE
        }
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(mContext!!)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(mContext!!)!!)

        WebServiceClient(mContext!!).client.create(IApiMethod::class.java)
            .globalLeaderBoard(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                    MyUtils.showToast(requireActivity(), "Something went wrong!!")
                    mBinding!!.contestFilterRefresh.isRefreshing = false
                    mBinding!!.progressBar.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {
                    mBinding!!.contestFilterRefresh.isRefreshing = false
                    mBinding!!.progressBar.visibility = View.GONE
                    val res = JSONObject(response!!.body().toString())
                    if (res.getBoolean("status")) {
                        val dataArray = res.getJSONArray("data")

                        filterList.clear()
                        dataList.clear()
                        childDataList.clear()

                        for (i in 0 until dataArray.length()) {
                            val dataObject = dataArray.getJSONObject(i)
                            val rankArray = dataObject.getJSONArray("rank")
                            val userArray = dataObject.getJSONArray("leaderBoard")

                            val rankList = ArrayList<RankModel>()
                            val userList = ArrayList<LeaderBoardModel>()

                            if (i == 0) {
                                filterList.add(
                                    ContestLeaderBoardModel(
                                        i,
                                        dataObject.getString("match_name"),
                                        true
                                    )
                                )
                            } else {
                                filterList.add(
                                    ContestLeaderBoardModel(
                                        i,
                                        dataObject.getString("match_name"),
                                        false
                                    )
                                )
                            }

                            for (j in 0 until rankArray.length()) {
                                val rankObject = rankArray.getJSONObject(j)
                                rankList.add(
                                    RankModel(
                                        rankObject.getString("key"),
                                        rankObject.getString("value")
                                    )
                                )
                            }

                            for (j in 0 until userArray.length()) {
                                val userObject = userArray.getJSONObject(j)
                                userList.add(
                                    LeaderBoardModel(
                                        userObject.getString("max_point"),
                                        userObject.getString("points"),
                                        userObject.getString("team_count"),
                                        userObject.getString("team_name"),
                                        userObject.getString("user_id"),
                                        userObject.getString("user_name"),
                                        userObject.getString("ranks"),
                                        userObject.getString("series_id")
                                    )
                                )
                            }

                            dataList.add(
                                DataModel(
                                    userList,
                                    dataObject.getString("match_name"),
                                    rankList
                                )
                            )
                        }

                        childDataList.add(dataList[0])

                        if (dataAdapter == null) {
                            dataAdapter =
                                LeaderBoardDataAdapter(mContext!!, childDataList[0].userList)
                            mBinding!!.recyclerView.adapter = dataAdapter
                        } else {
                            dataAdapter!!.updateDataRecord(childDataList[0].userList)
                        }

                        if (filterAdapter == null) {
                            filterAdapter =
                                FilterAdapter(mContext!!, filterList)
                            mBinding!!.filterRecyclerView.adapter = filterAdapter
                        } else {
                            filterAdapter!!.updateRecord(filterList)
                        }

                        infoMenu!!.isVisible = true
                    } else {
                        if (res.getInt("code") == 1001) {
                            MyUtils.showMessage(
                                mContext!!,
                                res.getString("message")
                            )
                            MyUtils.logoutApp(requireActivity())
                        } else {
                            MyUtils.showMessage(
                                mContext!!,
                                res.getString("message")
                            )
                        }
                    }
                }
            })
    }

    private fun updateLeaderBoardData(pos: Int) {
        position = pos
        for (i in filterList.indices) {
            filterList[i].isSelect = pos == i
        }

        filterAdapter!!.updateRecord(filterList)
        childDataList.clear()

        for (i in 0 until dataList.size) {
            if (pos == i) {
                childDataList.add(dataList[i])
            }
        }
        mBinding!!.recyclerView.smoothScrollToPosition(0)
        dataAdapter!!.updateDataRecord(childDataList[0].userList)
    }

    private fun showRankBreakup(contestTitle: String, prizeArrayList: ArrayList<RankModel>) {
        val view = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        val listView: RecyclerView = view.findViewById(R.id.listView)
        val backImage: ImageView = view.findViewById(R.id.back_image)
        val title: TextView = view.findViewById(R.id.toolbar_title)

        title.text = String.format("%s", contestTitle)

        listView.layoutManager =
            LinearLayoutManager(mContext!!, RecyclerView.VERTICAL, false)

        val dividerItemDecoration = DividerItemDecoration(
            listView.context,
            RecyclerView.VERTICAL
        )
        listView.addItemDecoration(dividerItemDecoration)

        if (prizeBreakUpAdapter != null) {
            prizeBreakUpAdapter =
                PriceBreakupAdapter(mContext!!, prizeArrayList)
            listView.adapter = prizeBreakUpAdapter
        } else {
            prizeBreakUpAdapter =
                PriceBreakupAdapter(mContext!!, prizeArrayList)
            listView.adapter = prizeBreakUpAdapter
        }

        bottomSheetDialog = BottomSheetDialog(mContext!!)
        bottomSheetDialog!!.setContentView(view)
        bottomSheetDialog!!.setCancelable(true)
        bottomSheetDialog!!.setCanceledOnTouchOutside(true)
        bottomSheetDialog!!.show()

        backImage.setOnClickListener {
            if (bottomSheetDialog!!.isShowing) {
                bottomSheetDialog!!.dismiss()
            }
        }
    }

    inner class LeaderBoardDataAdapter(
        val mContext: Context,
        var userDataList: ArrayList<LeaderBoardModel>
    ) :
        RecyclerView.Adapter<LeaderBoardDataAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.contest_leader_board_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                val dataModel = userDataList[position]
                holder.playerRanks.text = dataModel.ranks
                holder.userPoints.text = dataModel.max_point

                if (MyPreferences.getUserID(mContext) == dataModel.user_id) {
                    holder.userName.text = "You"
                } else {
                    if (dataModel.team_name == "") {
                        holder.userName.text = dataModel.user_name
                    } else {
                        holder.userName.text = dataModel.team_name
                    }
                }

                holder.leadersRows.setOnClickListener(UserClick(position))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return userDataList.size
        }

        fun updateDataRecord(dataList: ArrayList<LeaderBoardModel>) {
            userDataList = dataList
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
            val userName: TextView = itemView.findViewById(R.id.team_name)
            val userPoints: TextView = itemView.findViewById(R.id.points)
            val playerRanks: TextView = itemView.findViewById(R.id.player_rank)
            val leadersRows: LinearLayout = itemView.findViewById(R.id.leaders_rows)
        }

        inner class UserClick(val pos: Int) : View.OnClickListener {
            override fun onClick(v: View?) {
                val userId = userDataList[pos].user_id
                val matchName = userDataList[pos].series_id
                val intent = Intent(mContext, ViewUserDetailActivity::class.java)
                intent.putExtra("userId", userId)
                intent.putExtra("matchName", matchName)
                startActivity(intent)

            }
        }
    }

    inner class FilterAdapter(
        val mContext: Context,
        var arrayList: ArrayList<ContestLeaderBoardModel>
    ) :
        RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.contest_filter_new, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                val categoryModel: ContestLeaderBoardModel = arrayList[position]
                holder.contestTitle.text = categoryModel.match_name
                if (categoryModel.isSelect) {
                    holder.filterLayout.background =
                        ResourcesCompat.getDrawable(
                            mContext.resources,
                            R.drawable.filter_round_back_amber,
                            null
                        )
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        holder.contestTitle.setTextColor(
                            mContext.resources.getColor(
                                R.color.white,
                                null
                            )
                        )
                    } else {
                        holder.contestTitle.setTextColor(mContext.resources.getColor(R.color.white))
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        holder.contestTitle.setTextColor(
                            mContext.resources.getColor(
                                R.color.crop__selector_focused,
                                null
                            )
                        )
                    } else {
                        holder.contestTitle.setTextColor(mContext.resources.getColor(R.color.crop__selector_focused))
                    }
                    holder.filterLayout.background =
                        ResourcesCompat.getDrawable(
                            mContext.resources,
                            R.drawable.filter_round_back_white,
                            null
                        )
                }
                holder.filterLayout.setOnClickListener(ClickView(position))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        inner class ClickView(position: Int) : View.OnClickListener {
            var pos = position

            override fun onClick(v: View?) {
                updateLeaderBoardData(pos)
            }
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        fun updateRecord(filterArrayList: ArrayList<ContestLeaderBoardModel>) {
            arrayList = filterArrayList
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val contestTitle: TextView = itemView.findViewById(R.id.contest_title)
            val filterLayout: LinearLayout = itemView.findViewById(R.id.filter_layout)
        }
    }

    internal class PriceBreakupAdapter(
        var mContext: Context,
        var prizeArrayList: ArrayList<RankModel>
    ) :
        RecyclerView.Adapter<PriceBreakupAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var rankTxt: TextView = view.findViewById(R.id.ranktxt)
            var prizeTxt: TextView = view.findViewById(R.id.prizetxt)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_prize_breakup, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                val model: RankModel = prizeArrayList[position]
                holder.prizeTxt.text = String.format(
                    "₹%s",
                    model.value
                )
                holder.rankTxt.text = String.format("Rank %s", model.key)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return prizeArrayList.size
        }
    }
}