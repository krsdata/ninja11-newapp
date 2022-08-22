package fancode.cricks.ui.contest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.edify.atrist.listener.OnContestLoadedListener
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import fancode.cricks.*
import fancode.cricks.databinding.FragmentMyTeamBinding
import fancode.cricks.models.*
import fancode.cricks.network.IApiMethod
import fancode.cricks.network.WebServiceClient
import fancode.cricks.roomDatabase.ResponseDatabase
import fancode.cricks.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyTeamFragment : Fragment() {
    var matchObject: UpcomingMatchesModel? = null
    private var teamName: String? = ""
    private lateinit var mListener: OnContestLoadedListener
    private lateinit var customeProgressDialog: CustomProgressDialog2
    private var mBinding: FragmentMyTeamBinding? = null
    lateinit var adapter: MyTeamAdapter
    var myTeamArrayList = ArrayList<MyTeamModels>()
    private var isVisibleToUser: Boolean = false

    companion object {
        const val SERIALIZABLE_EDIT_TEAM: String = "editteam"
        const val SERIALIZABLE_COPY_TEAM: String = "copyteam"

        fun newInstance(bundle: Bundle): MyTeamFragment {
            val fragment = MyTeamFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        matchObject =
            requireArguments().get(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT) as UpcomingMatchesModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_my_team, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customeProgressDialog = CustomProgressDialog2(activity)
        getMyTeam()
        mBinding!!.recyclerMyTeam.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        adapter = MyTeamAdapter(requireActivity(), myTeamArrayList)
        mBinding!!.recyclerMyTeam.adapter = adapter
        mBinding!!.linearEmptyContest.visibility = View.GONE

        adapter.onItemClick = { objects ->
            teamName = objects.teamName
            getPoints(objects.teamId!!.teamId)

        }
        mBinding!!.btnCreateTeam.setOnClickListener {
            val intent = Intent(activity, CreateTeamActivity::class.java)
            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
            startActivityForResult(
                intent,
                CreateTeamActivity.CREATETEAM_REQUESTCODE
            )
        }
        mBinding!!.myteamRefresh.setOnRefreshListener {
            getMyteamApiCall()
        }
    }

    override fun onResume() {
        super.onResume()
        //if (isVisibleToUser) {
        if ((activity as ContestActivity).responseMyTeamList.isNotEmpty()) {
            myTeamArrayList.clear()
            myTeamArrayList.addAll((activity as ContestActivity).responseMyTeamList)
            adapter.notifyDataSetChanged()
            mListener.onMyTeam(myTeamArrayList)
        } else if (mBinding != null) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
    }

    private fun getPoints(teamId: Int) {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("team_id", teamId)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getPoints(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
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
                                    CreateTeamActivity.SERIALIZABLE_MATCH_KEY,
                                    matchObject
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnContestLoadedListener) {
            mListener = context
        } else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }
    }

    fun getMyTeam() {
        val lastTimeApiCall: Long? = MyPreferences.getLastTimeForApiCall(
            requireContext(),
            (Constant.myTeamFragmentDatabaseId + matchObject!!.matchId)
        )
        if (lastTimeApiCall!! + Constant.delayApiSeconds < System.currentTimeMillis()) {
            getMyteamApiCall()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val value = ResponseDatabase.getInstance(requireContext()).responseDao()
                    .getResponse((Constant.myTeamFragmentDatabaseId + matchObject!!.matchId).toLong())

                if (value != null && value.type == (Constant.myTeamFragmentDatabaseId + matchObject!!.matchId)) {
                    withContext(Dispatchers.Main) { allTeam(value.res) }
                }
            }
        }

    }

    private fun allTeam(res: UsersPostDBResponse) {
        val responseModel = res.responseObject
        if (mBinding != null && mBinding!!.myteamRefresh.isRefreshing) {
            mBinding!!.myteamRefresh.isRefreshing = false
        }
        customeProgressDialog.dismiss()
        if (responseModel != null) {
            if (responseModel.myTeamList != null && responseModel.myTeamList!!.isNotEmpty()) {
                myTeamArrayList.clear()
                (activity as ContestActivity).responseMyTeamList.clear()
                (activity as ContestActivity).responseMyTeamList.addAll(responseModel.myTeamList!!)
                myTeamArrayList.addAll(responseModel.myTeamList!!)
                updateEmptyViews()
                adapter.notifyDataSetChanged()
                mListener.onMyTeam(myTeamArrayList)
            }
        }
    }

    private fun getMyteamApiCall() {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        if (mBinding != null) {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }
        ///mBinding!!.progressMyteam.visibility = View.VISIBLE
        customeProgressDialog.show()
        /*val models = RequestModel()
        models.user_id = MyPreferences.getUserID(requireActivity())!!
        models.token = MyPreferences.getToken(requireActivity())!!
        models.match_id = "" + matchObject!!.matchId*/

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("match_id", matchObject!!.matchId)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getMyTeam(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if (mBinding != null) {
                        mBinding!!.myteamRefresh.isRefreshing = false
                    }
                    MyUtils.showToast(activity!! as AppCompatActivity, t!!.localizedMessage!!)
                    //mBinding!!.progressMyteam.visibility = View.GONE
                    customeProgressDialog.dismiss()
                    updateEmptyViews()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (mBinding != null) {
                        mBinding!!.myteamRefresh.isRefreshing = false
                    }
                    //mBinding!!.progressMyteam.visibility = View.GONE
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.responseObject
                            if (responseModel != null) {

                                if (isAdded)
                                viewLifecycleOwner.lifecycleScope.launch {
                                    withContext(Dispatchers.Main) { allTeam(res) }
                                    withContext(Dispatchers.IO) {
                                        MyPreferences.saveLastTimeForApiCall(
                                            context!!,
                                            Constant.myTeamFragmentDatabaseId + matchObject!!.matchId,
                                            System.currentTimeMillis()
                                        )
                                        ResponseDatabase.getInstance(context!!).responseDao()
                                            .saveResponse(
                                                fancode.cricks.roomDatabase.Response(
                                                    (Constant.myTeamFragmentDatabaseId + matchObject!!.matchId),
                                                    System.currentTimeMillis(),
                                                    res
                                                )
                                            )
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

    fun updateEmptyViews() {
        if (myTeamArrayList.size == 0) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        } else {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }
    }

    inner class MyTeamAdapter(val context: Context, tradeinfoModels: ArrayList<MyTeamModels>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((MyTeamModels) -> Unit)? = null
        private var matchesListObject = tradeinfoModels

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.myteam_rows, parent, false)
            return MyMatchViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            val objectVal = matchesListObject[viewType]
            val viewHolder: MyMatchViewHolder = parent as MyMatchViewHolder
            viewHolder.userTeamName.text = objectVal.teamName
            viewHolder.teamaName.text = objectVal.teamsInfo!![0].teamName
            viewHolder.teambName.text = objectVal.teamsInfo!![1].teamName

            viewHolder.teamaCount.text = "" + objectVal.teamsInfo!![0].count
            viewHolder.teambCount.text = "" + objectVal.teamsInfo!![1].count

            viewHolder.captainPlayerName.text = objectVal.captain!!.playerName
            viewHolder.vcPlayerName.text = objectVal.viceCaptain!!.playerName

            if (objectVal.wicketKeepers != null) {
                viewHolder.countWicketkeeper.text =
                    String.format("%d", objectVal.wicketKeepers!!.size)
            }
            viewHolder.countBatsman.text = String.format("%d", objectVal.batsmen!!.size)
            viewHolder.countAllRounder.text = String.format("%d", objectVal.allRounders!!.size)
            viewHolder.countBowler.text = String.format("%d", objectVal.bowlers!!.size)


            Glide.with(context)
                .load(R.drawable.player_blue)
                .placeholder(R.drawable.player_blue)
                .into(viewHolder.captainImageView)

            Glide.with(context)
                .load(R.drawable.player_blue)
                .placeholder(R.drawable.player_blue)
                .into(viewHolder.vcImageView)


            if (matchObject!!.status == BindingUtils.MATCH_STATUS_UPCOMING) {
                viewHolder.linearTeamCountViews.visibility = View.VISIBLE
                viewHolder.linearPointViews.visibility = View.GONE
                viewHolder.teamEdit.visibility = View.VISIBLE
                viewHolder.teamCopy.visibility = View.VISIBLE
                viewHolder.teamEdit.setOnClickListener(View.OnClickListener {
                    val intent = Intent(activity, CreateTeamActivity::class.java)
                    intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
                    intent.putExtra(SERIALIZABLE_EDIT_TEAM, objectVal)
                    startActivityForResult(
                        intent,
                        CreateTeamActivity.CREATETEAM_REQUESTCODE
                    )
                })

                viewHolder.teamCopy.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        val intent = Intent(activity, CreateTeamActivity::class.java)
                        intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
                        intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
                        intent.putExtra(SERIALIZABLE_COPY_TEAM, objectVal)
                        startActivityForResult(
                            intent,
                            CreateTeamActivity.CREATETEAM_REQUESTCODE
                        )
                    }
                })
            } else {
                viewHolder.linearTeamCountViews.visibility = View.GONE
                viewHolder.linearPointViews.visibility = View.VISIBLE
                viewHolder.teamCopy.visibility = View.GONE
                viewHolder.teamEdit.visibility = View.GONE
                viewHolder.myteamPoints.text = objectVal.teamPoints
            }
        }

        fun copyTeam(teamid: MyTeamId?) {
            customeProgressDialog.show()
            /*val models = RequestModel()
            models.user_id = MyPreferences.getUserID(activity!!)!!
            models.match_id = "" + matchObject!!.matchId
            models.team_id = teamid!!.teamId*/

            val jsonRequest = JsonObject()
            jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
            jsonRequest.addProperty(
                "system_token",
                MyPreferences.getSystemToken(requireActivity())!!
            )
            jsonRequest.addProperty("team_id", teamid!!.teamId)

            WebServiceClient(activity!!).client.create(IApiMethod::class.java).copyTeam(jsonRequest)
                .enqueue(object : Callback<UsersPostDBResponse?> {
                    override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                        if (isVisible) {
                            if (mBinding != null) {
                                mBinding!!.myteamRefresh.isRefreshing = false
                            }
                            MyUtils.showToast(activity!! as AppCompatActivity, t!!.localizedMessage)
                            // mBinding!!.progressMyteam.visibility = View.GONE
                            customeProgressDialog.dismiss()
                            updateEmptyViews()
                        }
                    }

                    override fun onResponse(
                        call: Call<UsersPostDBResponse?>?,
                        response: Response<UsersPostDBResponse?>?
                    ) {
                        if (isVisible) {
                            customeProgressDialog.dismiss()
                            getMyTeam()
                        }
                    }
                })
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

            val userTeamName = itemView.findViewById<TextView>(R.id.user_team_name)
            val teamEdit = itemView.findViewById<ImageView>(R.id.team_edit)
            val teamCopy = itemView.findViewById<ImageView>(R.id.team_copy)
            val teamShare = itemView.findViewById<ImageView>(R.id.team_share)

            val teamaName = itemView.findViewById<TextView>(R.id.teama_name)
            val teamaCount = itemView.findViewById<TextView>(R.id.teama_count)
            val teambName = itemView.findViewById<TextView>(R.id.teamb_name)
            val teambCount = itemView.findViewById<TextView>(R.id.teamb_count)


            val captainImageView = itemView.findViewById<ImageView>(R.id.captain_imageView)
            val captainPlayerName = itemView.findViewById<TextView>(R.id.captain_player_name)

            val vcImageView = itemView.findViewById<ImageView>(R.id.vc_imageView)
            val vcPlayerName = itemView.findViewById<TextView>(R.id.vc_player_name)

            val countWicketkeeper = itemView.findViewById<TextView>(R.id.count_wicketkeeper)
            val countBatsman = itemView.findViewById<TextView>(R.id.count_batsman)
            val countAllRounder = itemView.findViewById<TextView>(R.id.count_allrounder)
            val countBowler = itemView.findViewById<TextView>(R.id.count_bowler)

            val myteamPoints = itemView.findViewById<TextView>(R.id.myteam_points)
            val linearPointViews = itemView.findViewById<LinearLayout>(R.id.linear_point_views)
            val linearTeamCountViews =
                itemView.findViewById<LinearLayout>(R.id.linear_team_count_view)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (requestCode == CreateTeamActivity.CREATETEAM_REQUESTCODE && resultCode == AppCompatActivity.RESULT_OK) {
                getMyteamApiCall()
            }
        }
    }
}