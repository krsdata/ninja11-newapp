package ninja.cricks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ninja.cricks.CreateTeamActivity.Companion.CREATE_TEAM_ALLROUNDER
import ninja.cricks.CreateTeamActivity.Companion.CREATE_TEAM_BATSMAN
import ninja.cricks.CreateTeamActivity.Companion.CREATE_TEAM_BOWLER
import ninja.cricks.CreateTeamActivity.Companion.CREATE_TEAM_WICKET_KEEPER
import ninja.cricks.customviews.ScreenshotDetectionDelegate
import ninja.cricks.databinding.ActivityTeamPreviewBinding
import ninja.cricks.models.UpcomingMatchesModel
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RequestModel
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.createteam.models.PlayersInfoModel
import ninja.cricks.ui.home.models.UsersPostDBResponse
import ninja.cricks.ui.previewteam.adaptors.GridViewAdapter
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TeamPreviewActivity : AppCompatActivity(),
    ScreenshotDetectionDelegate.ScreenshotDetectionListener {

    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var teamId: Int = 0
    private var teamName: String = ""
    private lateinit var matchObject: UpcomingMatchesModel
    private lateinit var hasmapPlayers: HashMap<String, ArrayList<PlayersInfoModel>>
    private var mBinding: ActivityTeamPreviewBinding? = null
    private val listWicketKeeper = ArrayList<PlayersInfoModel>()
    private val listBatsMan = ArrayList<PlayersInfoModel>()
    private val listAllRounder = ArrayList<PlayersInfoModel>()
    private val listBowler = ArrayList<PlayersInfoModel>()
    private var mContext: Context? = null

    private val screenshotDetectionDelegate = ScreenshotDetectionDelegate(this, this)
    private var contestId: String = ""
    private var userId: String = ""

    companion object {
        const val SERIALIZABLE_TEAM_PREVIEW_KEY: String = "teampreview"
        const val KEY_TEAM_NAME: String = "team_name"
        const val KEY_TEAM_ID: String = "team_id"
        const val KEY_USER_ID: String = "user_id"
        const val KEY_CONTEST_ID: String = "contest_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_team_preview
        )

        mContext = this
        customeProgressDialog = CustomeProgressDialog(mContext)
        if (intent.hasExtra(KEY_TEAM_NAME)) {
            teamName = intent.getStringExtra(KEY_TEAM_NAME)
        }
        if (intent.hasExtra(KEY_TEAM_ID)) {
            teamId = intent.getIntExtra(KEY_TEAM_ID, 0)
        }
        matchObject =
            intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY) as UpcomingMatchesModel
        hasmapPlayers =
            intent.getSerializableExtra(SERIALIZABLE_TEAM_PREVIEW_KEY) as HashMap<String, ArrayList<PlayersInfoModel>>

        if (intent.hasExtra(KEY_CONTEST_ID)) {
            contestId = intent.getStringExtra(KEY_CONTEST_ID)
        }
        if (intent.hasExtra(KEY_USER_ID)) {
            userId = intent.getStringExtra(KEY_USER_ID)
        }

        mBinding!!.imgRefresh.setOnClickListener {
            getPoints(teamId)
        }

        mBinding!!.imgClose.setOnClickListener {
            finish()
        }

        mBinding!!.fantasyPointsWebsview.setOnClickListener {
            val intent = Intent(mContext, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_FANTASY_POINTS)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FANTASY_POINTS)
            startActivity(intent)
        }

        mBinding!!.teamName.text = teamName
        if (matchObject.status == BindingUtils.MATCH_STATUS_UPCOMING) {
            mBinding!!.pointsBar.visibility = View.GONE
            mBinding!!.imgRefresh.visibility = View.GONE
        } else {
            mBinding!!.pointsBar.visibility = View.VISIBLE
            mBinding!!.imgRefresh.visibility = View.VISIBLE
        }

        setupPlayersOnGrounds()
    }

    private fun setupPlayersOnGrounds() {
        mBinding!!.totalPointsValue.text = calculatePoints()
        addWicketKeeper()
        addBatsman()
        addAllRounder()
        addBowler()

        val gridViewAdapterWicket =
            GridViewAdapter(
                this@TeamPreviewActivity,
                listWicketKeeper,
                matchObject
            )
        mBinding!!.gridWicketKeeper.numColumns = listWicketKeeper.size
        mBinding!!.gridWicketKeeper.adapter = gridViewAdapterWicket

        var sizeofColumn = 0
        val gridViewAdapterBatsMan =
            GridViewAdapter(
                this@TeamPreviewActivity,
                listBatsMan,
                matchObject
            )

        if (listBatsMan.size > 4) {
            sizeofColumn = 3
        } else {
            sizeofColumn = listBatsMan.size
        }
        mBinding!!.gridBatsman.numColumns = sizeofColumn
        mBinding!!.gridBatsman.adapter = gridViewAdapterBatsMan

        val gridViewAdapterAllRounder =
            GridViewAdapter(
                this@TeamPreviewActivity,
                listAllRounder,
                matchObject
            )
        if (listAllRounder.size > 4) {
            sizeofColumn = 3
        } else {
            sizeofColumn = listAllRounder.size
        }
        mBinding!!.gridAllRounders.numColumns = sizeofColumn
        mBinding!!.gridAllRounders.adapter = gridViewAdapterAllRounder

        val gridViewAdapterBowler =
            GridViewAdapter(
                this@TeamPreviewActivity,
                listBowler,
                matchObject
            )
        if (listBowler.size > 4) {
            sizeofColumn = 3
        } else {
            sizeofColumn = listBowler.size
        }
        mBinding!!.gridBowlers.numColumns = sizeofColumn
        mBinding!!.gridBowlers.adapter = gridViewAdapterBowler
        setGridViewOnItemClickListener()
    }

    private fun getPoints(teamId: Int) {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()
        val models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.team_id = teamId

        WebServiceClient(this).client.create(IApiMethod::class.java).getPoints(models)
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
                            hasmapPlayers.put(CREATE_TEAM_WICKET_KEEPER, wktKeeperList)
                            hasmapPlayers.put(CREATE_TEAM_BATSMAN, batsManList)
                            hasmapPlayers.put(CREATE_TEAM_ALLROUNDER, allRounderList)
                            hasmapPlayers.put(CREATE_TEAM_BOWLER, allbowlerList)

                            updatePlayersPoints(hasmapPlayers)
                        }
                    }
                }
            })
    }

    private fun updatePlayersPoints(hasmapPlayers: HashMap<String, ArrayList<PlayersInfoModel>>) {
        this.hasmapPlayers.clear()
        this.hasmapPlayers = hasmapPlayers
        setupPlayersOnGrounds()
    }

    private fun calculatePoints(): String {
        var totalPoints: Double = 0.0
        if (hasmapPlayers.containsKey(CREATE_TEAM_WICKET_KEEPER)) {
            val wkKeeper = hasmapPlayers[CREATE_TEAM_WICKET_KEEPER]
            for (x in 0 until wkKeeper!!.size) {
                val obj = wkKeeper[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }

        if (hasmapPlayers.containsKey(CREATE_TEAM_BATSMAN)) {
            val btslist = hasmapPlayers[CREATE_TEAM_BATSMAN]
            for (x in 0 until btslist!!.size) {
                val obj = btslist[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }

        if (hasmapPlayers.containsKey(CREATE_TEAM_ALLROUNDER)) {
            val allList = hasmapPlayers[CREATE_TEAM_ALLROUNDER]
            for (x in 0 until allList!!.size) {
                val obj = allList[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }
        if (hasmapPlayers.containsKey(CREATE_TEAM_BOWLER)) {
            val bowlList = hasmapPlayers[CREATE_TEAM_BOWLER]
            for (x in 0 until bowlList!!.size) {
                val obj = bowlList[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }
        return totalPoints.toString()
    }

    private fun setGridViewOnItemClickListener() {
        mBinding!!.gridWicketKeeper.setOnItemClickListener { parent, view, position, id ->

        }
        mBinding!!.gridBatsman.setOnItemClickListener { parent, view, position, id ->

        }
        mBinding!!.gridAllRounders.setOnItemClickListener { parent, view, position, id ->

        }
        mBinding!!.gridBowlers.setOnItemClickListener { parent, view, position, id ->

        }
    }

    private fun addWicketKeeper() {
        listWicketKeeper.clear()
        if (hasmapPlayers.containsKey(CREATE_TEAM_WICKET_KEEPER)) {
            val listOfPlayers = hasmapPlayers[CREATE_TEAM_WICKET_KEEPER]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                if (playerObject.teamId == matchObject.teamAInfo!!.teamId) {
                    playerObject.setPlayerIcon(R.drawable.ic_player_wk_teama)
                } else {
                    playerObject.setPlayerIcon(R.drawable.ic_player_wk_teamb)
                }
            }
            listWicketKeeper.addAll(listOfPlayers)
        }
    }

    private fun addBatsman() {
        listBatsMan.clear()
        if (hasmapPlayers.containsKey(CREATE_TEAM_BATSMAN)) {
            val listOfPlayers = hasmapPlayers[CREATE_TEAM_BATSMAN]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                if (playerObject.teamId == matchObject.teamAInfo!!.teamId) {
                    playerObject.setPlayerIcon(R.drawable.ic_player_bat_teama)
                } else {
                    playerObject.setPlayerIcon(R.drawable.ic_player_bat_teamb)
                }
            }
            listBatsMan.addAll(listOfPlayers)
        }
    }

    private fun addAllRounder() {
        listAllRounder.clear()
        if (hasmapPlayers.containsKey(CREATE_TEAM_ALLROUNDER)) {
            val listOfPlayers = hasmapPlayers[CREATE_TEAM_ALLROUNDER]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                if (playerObject.teamId == matchObject.teamAInfo!!.teamId) {
                    playerObject.setPlayerIcon(R.drawable.ic_player_all_teama)
                } else {
                    playerObject.setPlayerIcon(R.drawable.ic_player_all_teamb)
                }
            }
            listAllRounder.addAll(listOfPlayers)
        }
    }

    private fun addBowler() {
        listBowler.clear()
        if (hasmapPlayers.containsKey(CREATE_TEAM_BOWLER)) {
            val listOfPlayers = hasmapPlayers[CREATE_TEAM_BOWLER]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                if (playerObject.teamId == matchObject.teamAInfo!!.teamId) {
                    playerObject.setPlayerIcon(R.drawable.ic_player_bowler_teama)
                } else {
                    playerObject.setPlayerIcon(R.drawable.ic_player_bowler_teamb)
                }
            }
            listBowler.addAll(listOfPlayers)
        }
    }

    override fun onStart() {
        super.onStart()
        screenshotDetectionDelegate.startScreenshotDetection()
    }

    override fun onStop() {
        super.onStop()
        screenshotDetectionDelegate.stopScreenshotDetection()
    }

    override fun onScreenCaptured(path: String) {
        if(contestId != null && contestId != "") {
            BindingUtils.sendEventLogs(
                mContext!!, matchObject.matchId.toString(), contestId, userId, teamId,
                (application as NinjaApplication).userInformations, "captured"
            )
        }
    }

    override fun onScreenCapturedWithDeniedPermission() {
        if(contestId != null && contestId != "") {
            BindingUtils.sendEventLogs(
                mContext!!, matchObject.matchId.toString(), contestId, userId, teamId,
                (application as NinjaApplication).userInformations, "captured"
            )
        }
    }
}