package ninja.cricks

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.edify.atrist.listener.OnContestEvents
import com.edify.atrist.listener.OnContestLoadedListener
import com.edify.atrist.listener.OnMatchTimerStarted
import com.google.gson.JsonObject
import ninja.cricks.databinding.ActivityContestBinding
import ninja.cricks.models.*
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.BaseActivity
import ninja.cricks.ui.contest.ContestFragment
import ninja.cricks.ui.contest.MyContestFragment
import ninja.cricks.ui.contest.MyTeamFragment
import ninja.cricks.ui.contest.PlayerStatsFragment
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ContestActivity : BaseActivity(), OnContestLoadedListener, OnContestEvents {

    //private var isMatchLive: Boolean = false
    var matchObject: UpcomingMatchesModel? = null
    var isTimeUp: Boolean = false
    var joinedTeamList: java.util.ArrayList<MyTeamModels>? = null
    var contestObjects: ArrayList<ContestModelLists>? = null
    private var mBinding: ActivityContestBinding? = null
    public var getAllContestResponseModel: UsersPostDBResponse? = null
    var responseMyJoinedContest: ArrayList<ContestModelLists> = ArrayList()
    var responseMyTeamList: ArrayList<MyTeamModels> = ArrayList()
    var resPlayerStatsList = ArrayList<PlayerStatsInfoModel>()


    companion object {
        val SERIALIZABLE_KEY_UPCOMING_MATCHES: String = "contest"
        val SERIALIZABLE_KEY_JOINED_CONTEST: String = "joinedcontest"
        val SERIALIZABLE_KEY_MATCH_OBJECT: String = "matchobject"
        val SERIALIZABLE_KEY_CONTEST_OBJECT: String = "contestmodel"
        val SERIALIZABLE_KEY_CREATE_TEAM: String = "create_team"
        val SERIALIZABLE_KEY_CREATE_TEAM1: String = "create_team1"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_contest)

        if (intent.hasExtra(SERIALIZABLE_KEY_UPCOMING_MATCHES)) {
            matchObject =
                intent.getSerializableExtra(SERIALIZABLE_KEY_UPCOMING_MATCHES) as UpcomingMatchesModel
        }
        if (intent.hasExtra(SERIALIZABLE_KEY_JOINED_CONTEST)) {
            val joinedMatchObject =
                intent.getSerializableExtra(SERIALIZABLE_KEY_JOINED_CONTEST) as JoinedMatchModel
            matchObject = UpcomingMatchesModel()
            matchObject!!.teamAInfo = joinedMatchObject.teamAInfo
            matchObject!!.teamBInfo = joinedMatchObject.teamBInfo
            matchObject!!.matchId = joinedMatchObject.matchId
            matchObject!!.timestampStart = joinedMatchObject.timestampStart
            matchObject!!.timestampEnd = joinedMatchObject.timestampEnd
            matchObject!!.matchTitle = joinedMatchObject.matchTitle
            matchObject!!.status = joinedMatchObject.status
            matchObject!!.statusString = joinedMatchObject.statusString
        }
        if (matchObject != null) {
            initViewUpcomingMatches()
        }

        mBinding!!.imageBack.setOnClickListener {
            finish()
        }

        mBinding!!.imgWallet.setOnClickListener {
            val intent = Intent(this@ContestActivity, MyBalanceActivity::class.java)
            startActivity(intent)
        }
        setupViewPager(mBinding!!.viewpagerContest)
        mBinding!!.tabs.setupWithViewPager(mBinding!!.viewpagerContest)
    }

    private fun initViewUpcomingMatches() {
        mBinding!!.teamsa.text = matchObject!!.teamAInfo!!.teamShortName
        mBinding!!.teamsb.text = matchObject!!.teamBInfo!!.teamShortName
    }

    override fun onResume() {
        super.onResume()
        if (matchObject!!.status == BindingUtils.MATCH_STATUS_UPCOMING) {
            pauseCountDown()
            startCountDown()
        } else {
            updateTimerHeader()
        }
    }

    private fun startCountDown() {
        BindingUtils.logD("TimerLogs", "initViewUpcomingMatches() called in ContestActivity")
        //matchObject!!.timestampStart = 1591371412 + 300
        BindingUtils.countDownStart(matchObject!!.timestampStart, object : OnMatchTimerStarted {
            override fun onTimeFinished() {
                if (!isTimeUp) {
                    isTimeUp = true
                    updateTimerHeader()
                    if (matchObject!!.status.equals(BindingUtils.MATCH_STATUS_UPCOMING)) {
                        showMatchTimeUpDialog()
                    }
                }
            }

            override fun onTicks(time: String) {
                mBinding!!.matchTimer.text = time
                mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.colorPrimary))
                mBinding!!.watchTimerImg.visibility = View.GONE
                BindingUtils.logD("TimerLogs", "ContestScreen: $time")
            }
        })
    }

    private fun updateTimerHeader() {
        mBinding!!.matchTimer.text = matchObject!!.statusString.toUpperCase(Locale.getDefault())
        mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.colorPrimary))
        mBinding!!.watchTimerImg.visibility = View.GONE
    }

    private fun pauseCountDown() {
        BindingUtils.stopTimer()
    }

    override fun onPause() {
        super.onPause()
        pauseCountDown()
    }

    fun changeTabsPositions(position: Int) {
        mBinding!!.viewpagerContest.currentItem = position
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (data != null) {
                MyUtils.showToast(this@ContestActivity, data.getStringExtra("keyName")!!)
            }
            if (requestCode == CreateTeamActivity.CREATETEAM_REQUESTCODE && resultCode == RESULT_OK) {
                val bundle = Bundle()
                bundle.putString(SERIALIZABLE_KEY_CREATE_TEAM, "result_ok")
                supportFragmentManager.setFragmentResult(
                    CreateTeamActivity.CREATETEAM_REQUESTCODE.toString(),
                    bundle
                )
                val bundle1 = Bundle()
                bundle1.putString(SERIALIZABLE_KEY_CREATE_TEAM1, "result_ok")
                supportFragmentManager.setFragmentResult(
                    CreateTeamActivity.CREATETEAM_REQUESTCODE1.toString(),
                    bundle1
                )

            }
        }
        mBinding!!.viewpagerContest.currentItem = 0
    }

    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {

    }

    override fun onBackPressed() {
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(R.menu.main_menu, menu)
//        this.collapseMenu = menu
        return true
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val bundle = Bundle()
        bundle.putSerializable(SERIALIZABLE_KEY_MATCH_OBJECT, matchObject)
        val adapter = ViewPagerAdapter(supportFragmentManager)
        if (matchObject!!.status == BindingUtils.MATCH_STATUS_UPCOMING) {
            adapter.addFragment(ContestFragment.newInstance(bundle), getString(
                    R.string.contest_type_contests
                ))
        }
        adapter.addFragment(MyContestFragment.newInstance(bundle), getString(
                R.string.contest_type_mycontest
            ))
        adapter.addFragment(MyTeamFragment.newInstance(bundle), getString(
                R.string.contest_type_myteam
            ))

        if (matchObject!!.status != BindingUtils.MATCH_STATUS_UPCOMING) {
            adapter.addFragment(PlayerStatsFragment.newInstance(bundle), getString(R.string.contest_type_playerstats))
        }

        viewPager.adapter = adapter

        if (matchObject!!.status == BindingUtils.MATCH_STATUS_COMPLETED) {
            mBinding!!.includeMatchRow.liveMatchesRow.visibility = View.VISIBLE
        } else {
            mBinding!!.includeMatchRow.liveMatchesRow.visibility = View.GONE
        }
        initScoreCard()
    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }

    override fun onMyContest(contestObjects: ArrayList<ContestModelLists>) {
        this.contestObjects = contestObjects
        if (matchObject!!.status == BindingUtils.MATCH_STATUS_UPCOMING) {
            mBinding!!.tabs.getTabAt(1)!!.text =
                String.format("My Contest(%d)", contestObjects.size)
        } else {
            mBinding!!.tabs.getTabAt(0)!!.text =
                String.format("My Contest(%d)", contestObjects.size)
        }
    }

    override fun onMyTeam(objects: ArrayList<MyTeamModels>) {
        this.joinedTeamList = objects
        if (matchObject!!.status == BindingUtils.MATCH_STATUS_UPCOMING) {
            mBinding!!.tabs.getTabAt(2)!!.text =
                String.format("MyTeam(%d)", this.joinedTeamList!!.size)
        } else {
            mBinding!!.tabs.getTabAt(1)!!.text =
                String.format("MyTeam(%d)", this.joinedTeamList!!.size)
        }
    }

    override fun onContestJoinning(objects: ContestModelLists, position: Int) {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("match_id", matchObject!!.matchId)
        jsonRequest.addProperty("contest_id", objects.id)

        WebServiceClient(this).client.create(IApiMethod::class.java)
            .joinNewContestStatus(jsonRequest)
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
                        if (!res.status) {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@ContestActivity, res.message)
                                MyUtils.logoutApp(this@ContestActivity)
                            } else if (res.code == 401) {
                                MyUtils.showToast(
                                    this@ContestActivity,
                                    res.message
                                )
                            } else {
                                MyUtils.showMessage(
                                    this@ContestActivity,
                                    res.message
                                )
                            }
                        } else {
                            if (res.actionForTeam == 1) {
                                val intent =
                                    Intent(this@ContestActivity, CreateTeamActivity::class.java)
                                intent.putExtra(
                                    CreateTeamActivity.SERIALIZABLE_MATCH_KEY,
                                    matchObject
                                )
                                startActivityForResult(
                                    intent,
                                    CreateTeamActivity.CREATETEAM_REQUESTCODE
                                )
                            } else if (res.actionForTeam == 2) {
                                val intent =
                                    Intent(this@ContestActivity, SelectTeamActivity::class.java)
                                intent.putExtra(
                                    CreateTeamActivity.SERIALIZABLE_MATCH_KEY,
                                    matchObject
                                )
                                intent.putExtra(
                                    CreateTeamActivity.SERIALIZABLE_CONTEST_KEY,
                                    objects
                                )
                                intent.putExtra(
                                    CreateTeamActivity.SERIALIZABLE_SELECTED_TEAMS,
                                    res.selectedTeamModel
                                )
                                startActivityForResult(
                                    intent,
                                    CreateTeamActivity.CREATETEAM_REQUESTCODE
                                )
                            } else {
                                Toast.makeText(this@ContestActivity, res.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                }
            })
    }

    override fun onShareContest(objects: ContestModelLists) {

    }

    private fun initScoreCard() {

        /*mBinding!!.teamsa.text = matchObject!!.teamAInfo!!.teamShortName
        mBinding!!.teamsb.text = matchObject!!.teamBInfo!!.teamShortName*/
        Glide.with(this)
            .load(matchObject!!.teamAInfo!!.logoUrl)
            .placeholder(R.drawable.placeholder_player_teama)
            .into(mBinding!!.includeMatchRow.imgTeamaLogo)

        Glide.with(this)
            .load(matchObject!!.teamBInfo!!.logoUrl)
            .placeholder(R.drawable.placeholder_player_teama)
            .into(mBinding!!.includeMatchRow.imgTeambLogo)

        mBinding!!.matchTimer.text = matchObject!!.statusString.toUpperCase(Locale.ENGLISH)
        mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.colorPrimary))
        mBinding!!.watchTimerImg.visibility = View.GONE

        mBinding!!.includeMatchRow.teamAName.text = matchObject!!.teamAInfo!!.teamShortName
        mBinding!!.includeMatchRow.teamBName.text = matchObject!!.teamBInfo!!.teamShortName

        mBinding!!.includeMatchRow.teamAScore.text = "0-0"
        mBinding!!.includeMatchRow.teamAOver.text = "(0)"

        mBinding!!.includeMatchRow.teamBScore.text = "0-0"
        mBinding!!.includeMatchRow.teamBOver.text = "0-0"

        updateScores()
    }

    private fun updateScores() {

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("contest_id", "")
        jsonRequest.addProperty("match_id", matchObject!!.matchId)

        WebServiceClient(this).client.create(IApiMethod::class.java).getScore(jsonRequest)
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
                        if (res.scoresModel != null) {
                            if (res.sessionExpired) {
                                logoutApp("Session Expired Please login again!!", false)
                            } else {
                                mBinding!!.includeMatchRow.statusNote.text =
                                    res.scoresModel!!.statusNote
                                if (res.scoresModel!!.teama!!.scores != null) {
                                    mBinding!!.includeMatchRow.teamAScore.text =
                                        res.scoresModel!!.teama!!.scores
                                } else {
                                    mBinding!!.includeMatchRow.teamAScore.text = ""
                                }

                                if (res.scoresModel!!.teama!!.overs != null) {
                                    mBinding!!.includeMatchRow.teamAOver.text =
                                        String.format("(%s)", res.scoresModel!!.teama!!.overs)
                                } else {
                                    mBinding!!.includeMatchRow.teamAOver.text =
                                        String.format("(%s)", "")
                                }

                                mBinding!!.includeMatchRow.teamBScore.text =
                                    res.scoresModel!!.teamb!!.scores
                                mBinding!!.includeMatchRow.teamBOver.text =
                                    String.format("(%s)", res.scoresModel!!.teamb!!.overs)
                            }
                        }
                    }
                }
            })
    }
}