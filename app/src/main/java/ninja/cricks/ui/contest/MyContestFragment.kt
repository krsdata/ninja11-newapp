package ninja.cricks.ui.contest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edify.atrist.listener.OnContestEvents
import com.edify.atrist.listener.OnContestLoadedListener
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ninja.cricks.*
import ninja.cricks.databinding.FragmentMyContestBinding
import ninja.cricks.models.MyTeamModels
import ninja.cricks.models.UpcomingMatchesModel
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.models.ContestModelLists
import ninja.cricks.models.PlayersInfoModel
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.roomDatabase.ResponseDatabase
import ninja.cricks.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyContestFragment : Fragment() {
    //private var isMatchStarted: Boolean= false
    var objectMatches: UpcomingMatchesModel? = null
    private lateinit var customProgressDialog: CustomProgressDialog2
    private lateinit var mListener: OnContestLoadedListener
    var mContestListeners: OnContestEvents? = null
    private var mBinding: FragmentMyContestBinding? = null
    lateinit var adapter: MyContestAdapter
    var checkinArrayList = ArrayList<ContestModelLists>()
    private var teamName: String? = ""
    private var isVisibleToUser: Boolean = false

    companion object {
        fun newInstance(bundle: Bundle): MyContestFragment {
            val fragment = MyContestFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        objectMatches =
            arguments?.get(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT) as UpcomingMatchesModel
        customProgressDialog = CustomProgressDialog2(activity)
        parentFragmentManager.setFragmentResultListener(CreateTeamActivity.CREATETEAM_REQUESTCODE.toString(),this,
            { s: String, bundle: Bundle ->
                if (bundle.get(ContestActivity.SERIALIZABLE_KEY_CREATE_TEAM) == "result_ok") {
                    allContestsApiCall()
                }
            })

        getMyJoinedContest()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_my_contest, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding!!.recyclerMyContest.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        mBinding!!.linearEmptyContest.visibility = View.GONE
        adapter = MyContestAdapter(requireActivity(), checkinArrayList)
        mBinding!!.recyclerMyContest.adapter = adapter

        adapter.onItemClick = { objects ->
            val intent = Intent(context, LeadersBoardActivity::class.java)
            intent.putExtra(LeadersBoardActivity.SERIALIZABLE_MATCH_KEY, objectMatches)
            intent.putExtra(LeadersBoardActivity.SERIALIZABLE_CONTEST_KEY, objects)
            requireActivity().startActivityForResult(
                intent,
                LeadersBoardActivity.CREATETEAM_REQUESTCODE
            )
        }
        mBinding!!.linearEmptyContest.visibility = View.GONE

        mBinding!!.btnJoinContest.setOnClickListener {
            (activity as ContestActivity).changeTabsPositions(0)
        }

        mBinding!!.mycontestRefresh.setOnRefreshListener {
            allContestsApiCall()
        }
    }

    override fun onResume() {
        super.onResume()
        if ((activity as ContestActivity).responseMyJoinedContest.isNotEmpty() ) {
            checkinArrayList.clear()
            checkinArrayList.addAll((activity as ContestActivity).responseMyJoinedContest)
            mListener.onMyContest(checkinArrayList)
            adapter.notifyDataSetChanged()
        }
        else if (mBinding != null) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        }
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
            mContestListeners = context
        } else {
            throw RuntimeException(
                "$context must implement OnContestEvents"
            )
        }
    }

    private fun getMyJoinedContest() {

        val lastTimeApiCall: Long? = MyPreferences.getLastTimeForApiCall(requireContext(),
            (Constant.myContestFragmentDatabaseId+objectMatches!!.matchId)
        )
        if (lastTimeApiCall!!+Constant.delayApiSeconds < System.currentTimeMillis()) {
            allContestsApiCall()
        }
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val value = ResponseDatabase.getInstance(requireContext()).responseDao().getResponse(
                    (Constant.myContestFragmentDatabaseId+ objectMatches!!.matchId)
                )

                if (value != null && value.type == (Constant.myContestFragmentDatabaseId + objectMatches!!.matchId)) {
                    withContext(Dispatchers.Main){allContests(value.res)}
                }
                else {
                    withContext(Dispatchers.Main){allContestsApiCall()}
                }
            }
        }

    }

    private fun allContestsApiCall() {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        if (mBinding != null) {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }

        //mBinding!!.progressContest.visibility = View.VISIBLE
        customProgressDialog.show()
        /*val models = RequestModel()
        models.user_id = MyPreferences.getUserID(requireActivity())!!
        models.token = MyPreferences.getToken(requireActivity())!!
        models.match_id = "" + objectMatches!!.matchId*/

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("match_id", objectMatches!!.matchId)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java).getMyContest(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if (mBinding != null) {
                        mBinding!!.mycontestRefresh.isRefreshing = false
                    }
                    //mBinding!!.progressContest.visibility = View.GONE
                    customProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (mBinding != null) {
                        mBinding!!.mycontestRefresh.isRefreshing = false
                    }
                    //mBinding!!.progressContest.visibility = View.GONE
                    customProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.responseObject
                            if (responseModel != null) {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    withContext(Dispatchers.Main){  allContests(res)}
                                    withContext(Dispatchers.IO){
                                        MyPreferences.saveLastTimeForApiCall(context!!,Constant.myContestFragmentDatabaseId + objectMatches!!.matchId, System.currentTimeMillis())
                                        ResponseDatabase.getInstance(context!!).responseDao().saveResponse(ninja.cricks.roomDatabase.Response(
                                            (Constant.myContestFragmentDatabaseId + objectMatches!!.matchId),System.currentTimeMillis(),res))
                                    }
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
                    updateEmptyViews()
                }
            })
    }

    private fun allContests(res: UsersPostDBResponse) {
        if (res.status) {
            val responseModel = res.responseObject
            if (responseModel!!.myJoinedContest != null && responseModel.myJoinedContest!!.size > 0) {
                (activity as ContestActivity).responseMyJoinedContest.clear()
                checkinArrayList.clear()
                (activity as ContestActivity).responseMyJoinedContest.addAll(responseModel.myJoinedContest!!)
                checkinArrayList.addAll((activity as ContestActivity).responseMyJoinedContest)
                mListener.onMyContest(checkinArrayList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun updateEmptyViews() {
        if (checkinArrayList.size == 0) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        } else {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }
    }

    inner class MyContestAdapter(
        val context: Activity,
        tradeinfoModels: ArrayList<ContestModelLists>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((ContestModelLists) -> Unit)? = null
        private var matchesListObject = tradeinfoModels


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.mycontest_rows, parent, false)
            return MyMatchViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, position: Int) {
            val objectVal = matchesListObject[position]
            val viewHolder: MyMatchViewHolder = parent as MyMatchViewHolder
            viewHolder.contestPrizePool.text = String.format("₹%s", objectVal.totalWinningPrize)
            viewHolder.contestEntryPrize.text = String.format("₹%s",objectVal.entryFees)
            if (objectVal.isContestCancelled) {
                viewHolder.contestInfo.text = "Cancelled"
                viewHolder.contestInfo.textSize = 18.0f
                viewHolder.contestInfo.setTextColor(Color.RED)
            } else {
                viewHolder.contestInfo.text = "Entry"
                viewHolder.contestInfo.setTextColor(Color.BLACK)
            }

            if (objectVal.totalSpots == 0) {
                viewHolder.contestProgressBar.max = objectVal.totalSpots + 15
                viewHolder.contestProgressBar.progress = objectVal.filledSpots
                viewHolder.totalSpots?.text = String.format("unlimited spots")
                viewHolder.totalSpotLeft?.text =
                    String.format("%d spot filled", objectVal.filledSpots)
            } else {
                viewHolder.contestProgressBar.max = objectVal.totalSpots
                viewHolder.contestProgressBar.progress = objectVal.filledSpots
                viewHolder.totalSpots?.text = String.format("%d spots", objectVal.totalSpots)

                if (objectVal.totalSpots == objectVal.filledSpots) {
                    viewHolder.totalSpotLeft?.text = "Contest Full"
                    viewHolder.totalSpotLeft?.setTextColor(Color.RED)
                } else {
                    viewHolder.totalSpotLeft?.text =
                        String.format("%d spot left", objectVal.totalSpots - objectVal.filledSpots)
                }
            }


            viewHolder.contestCancellation?.setOnClickListener(View.OnClickListener {

            })

            if (objectMatches!!.status == BindingUtils.MATCH_STATUS_UPCOMING) {
                viewHolder.contestEntryPrize?.setOnClickListener(View.OnClickListener {
                    mContestListeners!!.onContestJoinning(objectVal, position)

                })
            } else {
                viewHolder.contestEntryPrize?.setBackgroundResource(R.drawable.button_selector_grey)
                viewHolder.progressLinear?.visibility = View.GONE
            }
            viewHolder.firstPrize?.text = String.format("%s%s", "₹", objectVal.firstPrice)

            if (objectVal.joinedTeams != null && objectVal.joinedTeams!!.size > 0) {
                viewHolder.recyclerTeamList.layoutManager =
                    LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
                val dividerItemDecoration = DividerItemDecoration(
                    viewHolder.recyclerTeamList.context,
                    RecyclerView.VERTICAL
                )
                viewHolder.recyclerTeamList.addItemDecoration(dividerItemDecoration)

                val adapterJoinTeamAapter =
                    MyContestJoinedTeamAdapter(activity!!, objectVal.joinedTeams!!)
                viewHolder.recyclerTeamList.adapter = adapterJoinTeamAapter

                adapterJoinTeamAapter.onItemClick = { objects ->
                    teamName = objects.teamName
                    getPoints(objects.createdteamId, objectVal.id)

                }
            }
        }

        override fun getItemCount(): Int {
            return matchesListObject.size
        }

        inner class MyMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(matchesListObject[adapterPosition])
                }
            }

            val contestInfo = itemView.findViewById<TextView>(R.id.contest_info)
            val contestProgressBar = itemView.findViewById<ProgressBar>(R.id.contest_progress)
            val contestPrizePool = itemView.findViewById<TextView>(R.id.contest_prize_pool)

            val teamaName = itemView.findViewById<TextView>(R.id.teama_name)
            val progressLinear =
                itemView.findViewById<LinearLayout>(R.id.upcoming_linear_contest_view)
            val contestEntryPrize = itemView.findViewById<TextView>(R.id.contest_entry_prize)
            val totalSpotLeft = itemView.findViewById<TextView>(R.id.total_spot_left)
            val totalSpots = itemView.findViewById<TextView>(R.id.total_spot)

            val contestCancellation = itemView.findViewById<ImageView>(R.id.contest_cancellation)
            val firstPrize = itemView.findViewById<TextView>(R.id.first_prize)
            val recyclerTeamList = itemView.findViewById<RecyclerView>(R.id.recycler_team_list)
        }
    }

    inner class MyContestJoinedTeamAdapter(
        val context: Activity,
        tradeinfoModels: ArrayList<MyTeamModels>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((MyTeamModels) -> Unit)? = null
        private var matchesListObject = tradeinfoModels


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.mycontest_rows_teams, parent, false)
            return MyMatchViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            val objectVal = matchesListObject[viewType]
            val viewHolder: MyMatchViewHolder = parent as MyMatchViewHolder
            viewHolder.txtTeamName.text = objectVal.teamName

            if (!TextUtils.isEmpty(objectVal.teamWonStatus)) {
                val prize = objectVal.teamWonStatus!!.toDouble()
                if (prize > 0) {
                    if (objectMatches!!.status == BindingUtils.MATCH_STATUS_LIVE) {
                        viewHolder.teamWonStatus.text = String.format(
                            "Winning ₹%s",
                            objectVal.teamWonStatus
                        )
                    } else {
                        viewHolder.teamWonStatus.text = String.format(
                            "Won ₹%s",
                            objectVal.teamWonStatus
                        )
                    }
                } else {
                    viewHolder.teamWonStatus.visibility = View.GONE
                }
            } else {
                viewHolder.teamWonStatus.visibility = View.GONE
            }
            viewHolder.teamPoints.text = objectVal.teamPoints
            viewHolder.teamRanks.text = "#" + objectVal.teamRanks
        }

        override fun getItemCount(): Int {
            return matchesListObject.size
        }

        inner class MyMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(matchesListObject[adapterPosition])
                }
            }

            val txtTeamName = itemView.findViewById<TextView>(R.id.team_name)
            val teamWonStatus = itemView.findViewById<TextView>(R.id.team_won_status)
            val teamPoints = itemView.findViewById<TextView>(R.id.team_points)
            val teamRanks = itemView.findViewById<TextView>(R.id.team_ranks)
        }
    }

    fun getPoints(teamId: Int, contestId: Int) {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        customProgressDialog.show()
        /*val models = RequestModel()
        models.user_id = MyPreferences.getUserID(requireActivity())!!
        models.token =MyPreferences.getToken(activity!!)!!
        models.team_id = teamId*/

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("team_id", teamId)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java).getPoints(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            var totalPoints = res.totalPoints
                            val responseModel = res.responseObject
                            if (responseModel != null) {
                                val playerPointsList = responseModel.playerPointsList
                                val hasmapPlayers: HashMap<String, ArrayList<PlayersInfoModel>> =
                                    HashMap<String, ArrayList<PlayersInfoModel>>()

                                val wktKeeperList: ArrayList<PlayersInfoModel> =
                                    ArrayList<PlayersInfoModel>()
                                val batsManList: ArrayList<PlayersInfoModel> =
                                    ArrayList<PlayersInfoModel>()
                                val allRounderList: ArrayList<PlayersInfoModel> =
                                    ArrayList<PlayersInfoModel>()
                                val allbowlerList: ArrayList<PlayersInfoModel> =
                                    ArrayList<PlayersInfoModel>()

                                for (x in 0..playerPointsList!!.size - 1) {
                                    val plyObj = playerPointsList.get(x)
                                    if (plyObj.playerRole.equals("wk")) {
                                        wktKeeperList.add(plyObj)
                                    } else if (plyObj.playerRole.equals("bat")) {
                                        batsManList.add(plyObj)
                                    } else if (plyObj.playerRole.equals("all")) {
                                        allRounderList.add(plyObj)
                                    } else if (plyObj.playerRole.equals("bowl")) {
                                        allbowlerList.add(plyObj)
                                    }
                                }
                                hasmapPlayers.put(
                                    CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER,
                                    wktKeeperList
                                )
                                hasmapPlayers.put(
                                    CreateTeamActivity.CREATE_TEAM_BATSMAN,
                                    batsManList
                                )
                                hasmapPlayers.put(
                                    CreateTeamActivity.CREATE_TEAM_ALLROUNDER,
                                    allRounderList
                                )
                                hasmapPlayers.put(
                                    CreateTeamActivity.CREATE_TEAM_BOWLER,
                                    allbowlerList
                                )

                                val intent = Intent(activity, TeamPreviewActivity::class.java)
                                intent.putExtra(TeamPreviewActivity.KEY_TEAM_ID, teamId)
                                intent.putExtra(TeamPreviewActivity.KEY_TEAM_NAME, teamName)
                                intent.putExtra(
                                    TeamPreviewActivity.KEY_CONTEST_ID,
                                    contestId.toString()
                                )
                                intent.putExtra(
                                    TeamPreviewActivity.KEY_USER_ID,
                                    MyPreferences.getUserID(requireActivity())!!
                                )
                                intent.putExtra(
                                    CreateTeamActivity.SERIALIZABLE_MATCH_KEY,
                                    objectMatches
                                )
                                intent.putExtra(
                                    TeamPreviewActivity.SERIALIZABLE_TEAM_PREVIEW_KEY,
                                    hasmapPlayers
                                )
                                startActivity(intent)
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
}