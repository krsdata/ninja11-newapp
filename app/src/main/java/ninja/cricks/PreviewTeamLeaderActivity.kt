package ninja.cricks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import ninja.cricks.databinding.ActivityTeamPreviewBinding
import ninja.cricks.models.PlayersInfoModel
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PreviewTeamLeaderActivity : AppCompatActivity() {

    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var teamId: Int = 0
    private var teamName: String = ""
    private lateinit var hasmapPlayers: HashMap<String, ArrayList<PlayersInfoModel>>
    private var mBinding: ActivityTeamPreviewBinding? = null
    private val listWicketKeeper = ArrayList<PlayersInfoModel>()
    private val listBatsMan = ArrayList<PlayersInfoModel>()
    private val listAllRounder = ArrayList<PlayersInfoModel>()
    private val listBowler = ArrayList<PlayersInfoModel>()
    private var mContext: Context? = null

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
            teamName = intent.getStringExtra(KEY_TEAM_NAME)!!
        }
        if (intent.hasExtra(KEY_TEAM_ID)) {
            teamId = intent.getIntExtra(KEY_TEAM_ID, 0)
        }
        hasmapPlayers =
            intent.getSerializableExtra(SERIALIZABLE_TEAM_PREVIEW_KEY) as HashMap<String, ArrayList<PlayersInfoModel>>

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
        mBinding!!.pointsBar.visibility = View.VISIBLE
        mBinding!!.imgRefresh.visibility = View.VISIBLE

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
                mContext!!,
                listWicketKeeper,
            )
        mBinding!!.gridWicketKeeper.numColumns = listWicketKeeper.size
        mBinding!!.gridWicketKeeper.adapter = gridViewAdapterWicket

        var sizeofColumn = 0
        val gridViewAdapterBatsMan =
            GridViewAdapter(
                mContext!!,
                listBatsMan,
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
                mContext!!,
                listAllRounder,
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
                mContext!!,
                listBowler,
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

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("team_id", teamId)

        WebServiceClient(this).client.create(IApiMethod::class.java).getPoints(jsonRequest)
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

                                for (x in 0 until playerPointsList!!.size) {
                                    val plyObj = playerPointsList[x]
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
                                hasmapPlayers[CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER] =
                                    wktKeeperList
                                hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BATSMAN] = batsManList
                                hasmapPlayers[CreateTeamActivity.CREATE_TEAM_ALLROUNDER] =
                                    allRounderList
                                hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BOWLER] = allbowlerList
                                updatePlayersPoints(hasmapPlayers)
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(mContext!!, res.message)
                                MyUtils.logoutApp(this@PreviewTeamLeaderActivity)
                            } else {
                                MyUtils.showMessage(mContext!!, res.message)
                            }
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
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER)) {
            val wkKeeper = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER]
            for (x in 0 until wkKeeper!!.size) {
                val obj = wkKeeper[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }

        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_BATSMAN)) {
            val btslist = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BATSMAN]
            for (x in 0 until btslist!!.size) {
                val obj = btslist[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }

        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_ALLROUNDER)) {
            val allList = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_ALLROUNDER]
            for (x in 0 until allList!!.size) {
                val obj = allList[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_BOWLER)) {
            val bowlList = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BOWLER]
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
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER)) {
            val listOfPlayers = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                playerObject.setPlayerIcon(R.drawable.player_blue)
            }
            listWicketKeeper.addAll(listOfPlayers)
        }
    }

    private fun addBatsman() {
        listBatsMan.clear()
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_BATSMAN)) {
            val listOfPlayers = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BATSMAN]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                playerObject.setPlayerIcon(R.drawable.player_blue)
            }
            listBatsMan.addAll(listOfPlayers)
        }
    }

    private fun addAllRounder() {
        listAllRounder.clear()
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_ALLROUNDER)) {
            val listOfPlayers = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_ALLROUNDER]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                playerObject.setPlayerIcon(R.drawable.player_blue)
            }
            listAllRounder.addAll(listOfPlayers)
        }
    }

    private fun addBowler() {
        listBowler.clear()
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_BOWLER)) {
            val listOfPlayers = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BOWLER]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                playerObject.setPlayerIcon(R.drawable.player_blue)
            }
            listBowler.addAll(listOfPlayers)
        }
    }

    inner class GridViewAdapter(
        val context: Context,
        val listImageURLs: List<PlayersInfoModel>,
    ) :
        BaseAdapter() {

        override fun getItem(position: Int): Any {
            return listImageURLs[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return listImageURLs.size
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            val viewHolder: ViewHolder
            if (convertView == null) {
                convertView =
                    LayoutInflater.from(context)
                        .inflate(R.layout.preview_player_info, parent, false)
                viewHolder =
                    ViewHolder()
                viewHolder.imageView = convertView!!.findViewById(R.id.imageView)
                viewHolder.playerName = convertView.findViewById(R.id.player_name)
                viewHolder.playerFantasyPoints = convertView.findViewById(R.id.player_points)
                viewHolder.playerRole = convertView.findViewById(R.id.player_role)
                viewHolder.playing11 = convertView.findViewById(R.id.playing11)
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
            }
            val objects = listImageURLs.get(position)
            viewHolder.playerName.text = objects.shortName
            viewHolder.playerFantasyPoints.text = objects.playerPoints + " Pt"

            Glide.with(context)
                .load(objects.playerImage)
                .placeholder(R.drawable.player_blue)
                .into(viewHolder.imageView)

            viewHolder.playerName.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.ract_black_background,
                    null
                )
            viewHolder.playerName.setTextColor(context.resources.getColor(R.color.white))

            if (objects.isPlaying11) {
                viewHolder.playing11.visibility = View.GONE
            } else {
                viewHolder.playing11.visibility = View.VISIBLE
            }
            if (objects.isCaptain) {
                viewHolder.playerRole.visibility = View.VISIBLE
                viewHolder.playerRole.text = "C"
            } else if (objects.isViceCaptain) {
                viewHolder.playerRole.visibility = View.VISIBLE
                viewHolder.playerRole.text = "VC"
            } else {
                viewHolder.playerRole.visibility = View.GONE
            }
            return convertView
        }

        inner class ViewHolder {
            lateinit var imageView: ImageView
            lateinit var playerName: TextView
            lateinit var playerFantasyPoints: TextView
            lateinit var playerRole: TextView
            lateinit var playing11: TextView
        }
    }
}