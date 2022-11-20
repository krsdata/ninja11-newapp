package ninja.cricks.ui.mymatches

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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
=======
import androidx.lifecycle.lifecycleScope
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.edify.atrist.listener.OnMatchTimerStarted
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
import com.google.gson.JsonObject
=======
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ninja.cricks.Constant
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
import ninja.cricks.ContestActivity
import ninja.cricks.MainActivity
import ninja.cricks.R
import ninja.cricks.databinding.FragmentMyUpcomingBinding
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
=======
import ninja.cricks.models.ContestPreferenceModel
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
import ninja.cricks.models.UpcomingMatchesModel
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
import ninja.cricks.utils.BindingUtils
=======
import ninja.cricks.roomDatabase.ResponseDatabase
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.CustomProgressDialog2
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MyUpcomingMatchesFragment : Fragment() {

    private var mBinding: FragmentMyUpcomingBinding? = null
    lateinit var adapter: MyMatchesAdapter
    var checkinArrayList = ArrayList<UpcomingMatchesModel>()
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
=======
    lateinit var customeProgressDialog: CustomProgressDialog2
    companion object{
        public final val MyUpcomingMatchRoomId: Long = 1;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customeProgressDialog = CustomProgressDialog2(activity)
        getMatchHistory()
    }
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_my_upcoming, container, false
        )
        mBinding!!.recyclerMyUpcoming.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        adapter = MyMatchesAdapter(requireActivity(), checkinArrayList)
        mBinding!!.recyclerMyUpcoming.adapter = adapter
        adapter.onItemClick = { objects ->
            val intent = Intent(requireActivity(), ContestActivity::class.java)
            intent.putExtra(ContestActivity.SERIALIZABLE_KEY_UPCOMING_MATCHES, objects)
            startActivity(intent)
        }
        if (checkinArrayList.size > 0) {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        } else {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        }

        mBinding!!.btnEmptyView.setOnClickListener(View.OnClickListener {
            (activity as MainActivity).viewUpcomingMatches()
        })
        return mBinding!!.root
    }

    override fun onResume() {
        super.onResume()
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
        getMatchHistory()
    }

    private fun getMatchHistory() {
=======
        if ((activity as MainActivity).resCheckinArrayList.isNotEmpty()) {
            checkinArrayList.clear()
            checkinArrayList.addAll((activity as MainActivity).resCheckinArrayList)
            adapter.notifyDataSetChanged()
        }
        else if (mBinding != null) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        }
    }

    private fun getMatchHistory() {
        val lastTimeApiCall: Long? = MyPreferences.getLastTimeForApiCall(requireContext(),
            (Constant.myUpcomingMatchesFragmentDatabaseId)
        )
        if (lastTimeApiCall!!+ Constant.delayApiSeconds < System.currentTimeMillis()) {
            getMatchHistoryApiCall()
        }
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val value = ResponseDatabase.getInstance(requireContext()).responseDao().getResponse(
                    (Constant.myUpcomingMatchesFragmentDatabaseId)
                )

                if (value != null && value.type == (Constant.myUpcomingMatchesFragmentDatabaseId)){
                    withContext(Dispatchers.Main){getMatchHistory2(value.res)}
                }
                else {
                    withContext(Dispatchers.Main){getMatchHistoryApiCall()}
                }
            }
        }


    }

    private fun getMatchHistoryApiCall() {
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        //if (checkinArrayList.size == 0) {
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
        mBinding!!.progressBar.visibility = View.VISIBLE
        //}
        mBinding!!.linearEmptyContest.visibility = View.GONE
=======
        //mBinding!!.progressBar.visibility = View.VISIBLE
        customeProgressDialog.show()
        //}
        if (mBinding != null) {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }

>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
        /*val models = RequestModel()
        models.user_id = MyPreferences.getUserID(requireActivity())!!
        models.action_type = "upcoming"*/
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("action_type", "1")


        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getMatchHistory(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
                    if (mBinding!!.progressBar.visibility == View.VISIBLE) {
                        mBinding!!.progressBar.visibility = View.GONE
=======
                    /*if (mBinding!!.progressBar.visibility == View.VISIBLE) {
                        mBinding!!.progressBar.visibility = View.GONE
                    }*/
                    if (customeProgressDialog.isShowing) {
                        customeProgressDialog.dismiss()
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
                    }
                    updateEmptyViews()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (!isVisible){
                        return
                    }
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
                    mBinding!!.progressBar.visibility = View.GONE
=======
                    //mBinding!!.progressBar.visibility = View.GONE
                    customeProgressDialog.dismiss()
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.responseObject
                            if (responseModel != null) {
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
                                if (responseModel.matchdatalist != null && responseModel.matchdatalist!!.size > 0) {
                                    checkinArrayList.clear()
                                    checkinArrayList.addAll(responseModel.matchdatalist!!.get(0).upcomingMatchHistory!!)
                                    adapter.notifyDataSetChanged()
=======
                                viewLifecycleOwner.lifecycleScope.launch {
                                    withContext(Dispatchers.Main){ getMatchHistory2(res) }
                                    withContext(Dispatchers.IO){
                                        MyPreferences.saveLastTimeForApiCall(context!!,Constant.myUpcomingMatchesFragmentDatabaseId, System.currentTimeMillis())
                                        ResponseDatabase.getInstance(context!!).responseDao().saveResponse(ninja.cricks.roomDatabase.Response(
                                            Constant.myUpcomingMatchesFragmentDatabaseId, System.currentTimeMillis(), res))
                                    }
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
                                }
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(requireActivity(), res.message)
                                MyUtils.logoutApp(requireActivity())
                            } else {
                                MyUtils.showMessage(requireActivity(), res.message)
                            }
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
=======

>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
                        }
                    }
                    updateEmptyViews()
                }
            })
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
=======

    }

    private fun getMatchHistory2(res: UsersPostDBResponse) {
        customeProgressDialog.dismiss()
        val responseModel = res.responseObject
        if (responseModel!!.matchdatalist != null && responseModel.matchdatalist!!.size > 0) {
            checkinArrayList.clear()
            (activity as MainActivity).resCheckinArrayList.clear()
            checkinArrayList.addAll(responseModel.matchdatalist!![0].upcomingMatchHistory!!)
            (activity as MainActivity).resCheckinArrayList.addAll(
                responseModel.matchdatalist!!.get(
                    0
                ).upcomingMatchHistory!!
            )
            updateEmptyViews()
            adapter.notifyDataSetChanged()
        }
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
    }

    private fun updateEmptyViews() {
        if (checkinArrayList.size > 0) {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        } else {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        }
    }

    inner class MyMatchesAdapter(
        val context: Context,
        val tradeinfoModels: ArrayList<UpcomingMatchesModel>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((UpcomingMatchesModel) -> Unit)? = null
        private var matchesListObject = tradeinfoModels


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
                .inflate(R.layout.matches_row_upcoming_inner, parent, false)
=======
                .inflate(R.layout.my_matches_row, parent, false)
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/ui/mymatches/MyUpcomingMatchesFragment.kt
            return DataViewHolder(view)

        }

        fun getRandomColor(): Int {
            val rnd = Random()
            return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            val objectVal = matchesListObject[viewType]
            val viewHolder: DataViewHolder = parent as DataViewHolder
            if (objectVal.isLineup) {
                viewHolder.matchTitle?.visibility = View.VISIBLE
            } else {
                viewHolder.matchTitle?.visibility = View.INVISIBLE
            }
            viewHolder.tournamentTitle?.text = objectVal.leagueTitle
            // viewHolder?.matchProgress?.text = ""+objectVal.timestampEnd
            viewHolder.opponent1?.text = objectVal.teamAInfo!!.teamShortName
            viewHolder.opponent2?.text = objectVal.teamBInfo!!.teamShortName

            if (!TextUtils.isEmpty(objectVal.dateStart)) {
                viewHolder.matchtime.visibility = View.VISIBLE
                viewHolder.matchtime.text = objectVal.dateStart
            } else {
                viewHolder.matchtime.visibility = View.GONE
            }

            if (objectVal.freeContest) {
                viewHolder.freeView?.visibility = View.VISIBLE
            } else {
                viewHolder.freeView?.visibility = View.GONE
            }
            viewHolder.teamAColorView?.setBackgroundColor(getRandomColor())
            viewHolder.teamBColorView?.setBackgroundColor(getRandomColor())

            BindingUtils.countDownStartForAdaptors(objectVal.timestampStart,
                object : OnMatchTimerStarted {
                    override fun onTimeFinished() {
                        viewHolder.matchProgress.text = objectVal.statusString
                    }

                    override fun onTicks(time: String) {
                        viewHolder.matchProgress.text = time
                    }

                })
            if (!TextUtils.isEmpty(objectVal.contestName)) {
                viewHolder.upcomingLinearContestView.visibility = View.VISIBLE
                viewHolder.contestName?.text = "" + objectVal.contestName
                viewHolder.contestPrice?.text = "" + objectVal.contestPrize
            } else {
                viewHolder.upcomingLinearContestView.visibility = View.INVISIBLE
            }

            Glide.with(context)
                .load(objectVal.teamAInfo!!.logoUrl)
                .placeholder(R.drawable.placeholder_player_teama)
                .into(viewHolder.teamALogo)


            Glide.with(context)
                .load(objectVal.teamBInfo!!.logoUrl)
                .placeholder(R.drawable.placeholder_player_teama)
                .into(viewHolder.teamBLogo)

        }


        override fun getItemCount(): Int {
            return matchesListObject.size
        }


        inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(matchesListObject[adapterPosition])
                }
            }

            val teamALogo = itemView.findViewById<ImageView>(R.id.teama_logo)
            val teamBLogo = itemView.findViewById<ImageView>(R.id.teamb_logo)
            val matchTitle = itemView.findViewById<TextView>(R.id.upcoming_match_title)
            val tournamentTitle = itemView.findViewById<TextView>(R.id.tournament_title)
            val teamAColorView = itemView.findViewById<View>(R.id.countrycolorview)
            val teamBColorView = itemView.findViewById<View>(R.id.countrybcolorview)
            val opponent1 = itemView.findViewById<TextView>(R.id.upcoming_opponent1)
            val opponent2 = itemView.findViewById<TextView>(R.id.upcoming_opponent2)
            val matchtime = itemView.findViewById<TextView>(R.id.match_time)
            val freeView = itemView.findViewById<TextView>(R.id.free_view)
            val matchProgress = itemView.findViewById<TextView>(R.id.upcoming_match_progress)
            val upcomingLinearContestView =
                itemView.findViewById<LinearLayout>(R.id.upcoming_linear_contest_view)
            val contestName = itemView.findViewById<TextView>(R.id.upcoming_contest_name)
            val contestPrice = itemView.findViewById<TextView>(R.id.upcoming_contest_price)
        }
    }
}