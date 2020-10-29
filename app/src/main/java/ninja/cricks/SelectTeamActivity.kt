package ninja.cricks

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja.cricks.adaptors.SelectedTeamAdapter
import ninja.cricks.databinding.ActivitySelectTeamBinding
import ninja.cricks.models.MyTeamModels
import ninja.cricks.models.SelectedTeamModels
import ninja.cricks.models.UpcomingMatchesModel
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RequestModel
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.JoinContestDialogFragment
import ninja.cricks.ui.contest.models.ContestModelLists
import ninja.cricks.ui.home.models.UsersPostDBResponse
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SelectTeamActivity : AppCompatActivity() {

    private var customeProgressDialog: CustomeProgressDialog? = null
    private lateinit var contestModel: ContestModelLists
    private lateinit var matchObject: UpcomingMatchesModel
    private var mBinding: ActivitySelectTeamBinding? = null
    lateinit var adapter: SelectedTeamAdapter
    var selectedTeamList: ArrayList<SelectedTeamModels> = ArrayList<SelectedTeamModels>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_select_team
        )
        customeProgressDialog = CustomeProgressDialog(this)
        matchObject =
            intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY) as UpcomingMatchesModel
        contestModel =
            intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_CONTEST_KEY) as ContestModelLists
        selectedTeamList =
            intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_SELECTED_TEAMS) as ArrayList<SelectedTeamModels>

        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })


        mBinding!!.createTeam.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@SelectTeamActivity, CreateTeamActivity::class.java)
            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
            startActivityForResult(intent, CreateTeamActivity.CREATETEAM_REQUESTCODE)
        })

        mBinding!!.recyclerSelectTeam.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        adapter = SelectedTeamAdapter(this, matchObject, customeProgressDialog!!, selectedTeamList)
        mBinding!!.recyclerSelectTeam.adapter = adapter

        mBinding!!.teamContinue.setOnClickListener(View.OnClickListener {

            joinMatch()
        })

        if (selectedTeamList != null && selectedTeamList.size > 0) {
            var openMatchListPos0 = selectedTeamList.get(0).openTeamList
            if (openMatchListPos0 != null && openMatchListPos0.size == 1) {
                var obj = selectedTeamList.get(0).openTeamList!!
                var otl = obj.get(0)
                otl.isSelected = true
                obj.set(0, otl)
                joinMatch()
            } else {
                if (selectedTeamList.size == 2) {
                    var openMatchListPos1 = selectedTeamList.get(1).openTeamList
                    if (openMatchListPos1 != null && openMatchListPos1.size == 1) {
                        var otl = openMatchListPos1.get(0)
                        otl.isSelected = true
                        openMatchListPos1.set(0, otl)
                        joinMatch()
                    }
                }
            }
        }


    }

    private fun joinMatch() {
        var isTeamFound = false
        var seelctedTeamList = getSelectedOpenList()
        for (x in 0..seelctedTeamList.size - 1) {
            var objects = seelctedTeamList.get(x)
            if (objects.isSelected!!) {
                isTeamFound = true
            }
        }
        if (isTeamFound) {
            val fm = supportFragmentManager
            val pioneersFragment =
                JoinContestDialogFragment(seelctedTeamList, matchObject, contestModel)
            pioneersFragment.show(fm, "PioneersFragment_tag")
        } else {
            MyUtils.showToast(
                this@SelectTeamActivity,
                "Please select your team to join this contest"
            )
        }
    }

    private fun getSelectedOpenList(): ArrayList<MyTeamModels> {
        return selectedTeamList.get(selectedTeamList.size - 1).openTeamList!!
    }

    fun refreshContents() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog!!.show()
        var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        // models.token =MyPreferences.getToken(this)!!
        models.match_id = "" + matchObject.matchId
        models.contest_id = "" + contestModel.id

        WebServiceClient(this).client.create(IApiMethod::class.java).joinNewContestStatus(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog!!.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog!!.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (!res.status) {
                            if (res.code == 401) {
                                MyUtils.showToast(
                                    this@SelectTeamActivity,
                                    res.message
                                )
                            } else {
                                MyUtils.showMessage(
                                    this@SelectTeamActivity,
                                    res.message
                                )
                            }
                        } else {
                            selectedTeamList = res.selectedTeamModel!!
                            adapter = SelectedTeamAdapter(
                                this@SelectTeamActivity, matchObject, customeProgressDialog!!,
                                selectedTeamList
                            )
                            mBinding!!.recyclerSelectTeam.adapter = adapter
                        }
                    }

                }

            })
    }
}
