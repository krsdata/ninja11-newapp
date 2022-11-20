package ninja.cricks

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import ninja.cricks.adaptors.SelectedTeamAdapter
import ninja.cricks.databinding.ActivitySelectTeamBinding
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/SelectTeamActivity.kt
import ninja.cricks.models.MyTeamModels
import ninja.cricks.models.SelectedTeamModels
import ninja.cricks.models.UpcomingMatchesModel
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.JoinContestActivity
import ninja.cricks.models.ContestModelLists
import ninja.cricks.models.UsersPostDBResponse
=======
import ninja.cricks.models.*
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.JoinContestActivity
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/SelectTeamActivity.kt
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/SelectTeamActivity.kt

=======
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/SelectTeamActivity.kt
class SelectTeamActivity : AppCompatActivity() {

    private var customeProgressDialog: CustomeProgressDialog? = null
    private lateinit var contestModel: ContestModelLists
    private lateinit var matchObject: UpcomingMatchesModel
    private var mBinding: ActivitySelectTeamBinding? = null
    lateinit var adapter: SelectedTeamAdapter
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/SelectTeamActivity.kt
    var selectedTeamList: ArrayList<SelectedTeamModels> = ArrayList<SelectedTeamModels>()
=======
    var selectedTeamList: ArrayList<SelectedTeamModels> = ArrayList()
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/SelectTeamActivity.kt

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
            val openMatchListPos0 = selectedTeamList.get(0).openTeamList
            if (openMatchListPos0 != null && openMatchListPos0.size == 1) {
                val obj = selectedTeamList.get(0).openTeamList!!
                val otl = obj.get(0)
                otl.isSelected = true
                obj.set(0, otl)
                joinMatch()
            } else {
                if (selectedTeamList.size == 2) {
                    val openMatchListPos1 = selectedTeamList.get(1).openTeamList
                    if (openMatchListPos1 != null && openMatchListPos1.size == 1) {
                        val otl = openMatchListPos1.get(0)
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
        val seelctedTeamList = getSelectedOpenList()
        for (x in 0 until seelctedTeamList.size) {
            val objects = seelctedTeamList[x]
            if (objects.isSelected!!) {
                isTeamFound = true
            }
        }
        if (isTeamFound) {
            // comment by  nilesh for new activity on 30-10-20
            /*val fm = supportFragmentManager
            val pioneersFragment =
                JoinContestDialogFragment(seelctedTeamList, matchObject, contestModel)
            pioneersFragment.show(fm, "PioneersFragment_tag")*/

            val intent =
                Intent(this@SelectTeamActivity, JoinContestActivity::class.java)
            intent.putExtra(
                CreateTeamActivity.SERIALIZABLE_MATCH_KEY,
                matchObject
            )
            intent.putExtra(
                CreateTeamActivity.SERIALIZABLE_CONTEST_KEY,
                contestModel
            )
            intent.putExtra(
                CreateTeamActivity.SERIALIZABLE_SELECTED_TEAMS,
                seelctedTeamList
            )
            startActivityForResult(
                intent,
                CreateTeamActivity.CREATETEAM_REQUESTCODE
            )


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

        /*val models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.token = MyPreferences.getToken(this)!!
        models.match_id = "" + matchObject.matchId
        models.contest_id = "" + contestModel.id*/

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("match_id", matchObject.matchId)
        jsonRequest.addProperty("contest_id", contestModel.id)

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/SelectTeamActivity.kt
        WebServiceClient(this).client.create(IApiMethod::class.java).joinNewContestStatus(jsonRequest)
=======
        WebServiceClient(this).client.create(IApiMethod::class.java)
            .joinNewContestStatus(jsonRequest)
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/SelectTeamActivity.kt
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
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@SelectTeamActivity, res.message)
                                MyUtils.logoutApp(this@SelectTeamActivity)
                            } else if (res.code == 401) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/SelectTeamActivity.kt
            setResult(RESULT_OK)
=======
            if (data != null) {
                val intent = Intent()
                intent.putExtra("keyName", data.getStringExtra("keyName"))
                setResult(RESULT_OK, intent)
                finish()
            } else {
                val intent = Intent()
                setResult(RESULT_CANCELED, intent)
                finish()
            }
        } else if (resultCode == RESULT_CANCELED) {
            val intent = Intent()
            setResult(RESULT_CANCELED, intent)
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/SelectTeamActivity.kt
            finish()
        }
    }
}
