package ninja.cricks.ui.mymatches

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import ninja.cricks.Constant
import ninja.cricks.ContestActivity
import ninja.cricks.MainActivity
import ninja.cricks.R
import ninja.cricks.databinding.FragmentMyLiveBinding
import ninja.cricks.models.ContestPreferenceModel
import ninja.cricks.models.JoinedMatchModel
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.roomDatabase.ResponseDatabase
import ninja.cricks.utils.CustomProgressDialog2
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MyLiveMatchesFragment : Fragment() {

    private var mBinding: FragmentMyLiveBinding? = null
    lateinit var adapter: MyMatchesAdapter
    var checkInArrayList = ArrayList<JoinedMatchModel>()
    lateinit var customeProgressDialog: CustomProgressDialog2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customeProgressDialog = CustomProgressDialog2(activity)
        val activity = activity
            getMatchHistory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_my_live, container, false
        )

        mBinding!!.recyclerMyUpcoming.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        adapter = MyMatchesAdapter(requireActivity(), checkInArrayList)
        mBinding!!.recyclerMyUpcoming.adapter = adapter

        adapter.onItemClick = { objects ->
            val intent = Intent(requireActivity(), ContestActivity::class.java)
            intent.putExtra(ContestActivity.SERIALIZABLE_KEY_JOINED_CONTEST, objects)
            startActivity(intent)
        }

        updateEmptyViews()

        mBinding!!.btnEmptyView.setOnClickListener {
            (activity as MainActivity).viewUpcomingMatches()
        }
        return mBinding!!.root
    }

    override fun onResume() {
        super.onResume()
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        Log.e(TAG, "onResume")
        if ((activity as MainActivity).resLiveCheckinArraylist.isNotEmpty()) {
            checkInArrayList.clear()
            checkInArrayList.addAll((activity as MainActivity).resLiveCheckinArraylist)
            adapter.notifyDataSetChanged()
            updateEmptyViews()
        } else if (mBinding != null) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        }
    }

    private fun getMatchHistory() {
        val lastTimeApiCall: Long? = MyPreferences.getLastTimeForApiCall(requireContext(),
            (Constant.myLiveMatchesFragmentDatabaseId)
        )
        if (lastTimeApiCall!!+ Constant.delayApiSeconds < System.currentTimeMillis()) {
            if (activity != null && isAdded)getMatchHistoryApiCall()
        }
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val value = ResponseDatabase.getInstance(requireContext()).responseDao().getResponse(
                    (Constant.myLiveMatchesFragmentDatabaseId)
                )

                if (value != null && value.type == (Constant.myLiveMatchesFragmentDatabaseId)){
                    withContext(Dispatchers.Main){if (activity != null && isAdded)getMatchHistory2(value.res)}
                }
                else {
                    withContext(Dispatchers.Main){if (activity != null && isAdded)getMatchHistoryApiCall()}
                }
            }
        }
    }

    private fun getMatchHistoryApiCall() {
        if (activity != null && !MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        //mBinding!!.progressBar.visibility = View.VISIBLE
       // customeProgressDialog.show()
        if (mBinding != null) {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("action_type", "3")

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getMatchHistory(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
/*
                    if (mBinding!!.progressBar.visibility == View.VISIBLE) {
                        mBinding!!.progressBar.visibility = View.GONE
                    }
*/
                    customeProgressDialog.dismiss()
                    updateEmptyViews()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (!isVisible){
                        return
                    }
                    // mBinding!!.progressBar.visibility = View.GONE
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.responseObject
                            if (responseModel != null) {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    withContext(Dispatchers.Main){ getMatchHistory2(res) }
                                    withContext(Dispatchers.IO){
                                        MyPreferences.saveLastTimeForApiCall(context!!,Constant.myLiveMatchesFragmentDatabaseId, System.currentTimeMillis())
                                        ResponseDatabase.getInstance(context!!).responseDao().saveResponse(ninja.cricks.roomDatabase.Response(
                                            Constant.myLiveMatchesFragmentDatabaseId, System.currentTimeMillis(), res))
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

    private fun getMatchHistory2(res: UsersPostDBResponse) {
        customeProgressDialog.dismiss()
        val responseModel = res.responseObject
        if (responseModel?.matchdatalist != null && responseModel.matchdatalist!!.isNotEmpty()) {
            checkInArrayList.clear()
            if (activity != null && isAdded) {
                (requireActivity() as MainActivity).resLiveCheckinArraylist.clear()
                checkInArrayList.addAll(responseModel.matchdatalist!![0].liveMatchHistory!!)
                (activity as MainActivity).resLiveCheckinArraylist.addAll(responseModel.matchdatalist!![0].liveMatchHistory!!)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun updateEmptyViews() {
        if (checkInArrayList.size > 0) {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        } else {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        }
    }

    inner class MyMatchesAdapter(
        val context: Context,
        tradeInfoModels: ArrayList<JoinedMatchModel>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((JoinedMatchModel) -> Unit)? = null
        private var matchesListObject = tradeInfoModels

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.my_matches_row, parent, false)
            return DataViewHolder(view)
        }

        private fun getRandomColor(): Int {
            val rnd = Random()
            return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            val objectVal = matchesListObject[viewType]
            val viewHolder: DataViewHolder = parent as DataViewHolder
            viewHolder.matchTitle.visibility = View.GONE
            viewHolder.tournamentTitle.visibility = View.VISIBLE
            viewHolder.tournamentTitle.text = objectVal.matchTitle
            viewHolder.opponent1.text = objectVal.teamAInfo!!.teamShortName
            viewHolder.opponent2.text = objectVal.teamBInfo!!.teamShortName
            viewHolder.freeView.visibility = View.GONE
            viewHolder.matchTime.visibility = View.VISIBLE

            viewHolder.teamAColorView.setBackgroundColor(getRandomColor())
            viewHolder.teamBColorView.setBackgroundColor(getRandomColor())

            viewHolder.matchProgress.text = objectVal.statusString
            viewHolder.upcomingLinearContestView.visibility = View.INVISIBLE

            viewHolder.matchTime.text = objectVal.dateStart


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

            val teamALogo: ImageView = itemView.findViewById(R.id.teama_logo)
            val teamBLogo: ImageView = itemView.findViewById(R.id.teamb_logo)
            val matchTitle: TextView = itemView.findViewById(R.id.upcoming_match_title)
            val tournamentTitle: TextView = itemView.findViewById(R.id.tournament_title)
            val teamAColorView: View = itemView.findViewById(R.id.countrycolorview)
            val teamBColorView: View = itemView.findViewById(R.id.countrybcolorview)
            val opponent1: TextView = itemView.findViewById(R.id.upcoming_opponent1)
            val opponent2: TextView = itemView.findViewById(R.id.upcoming_opponent2)
            val freeView: TextView = itemView.findViewById(R.id.free_view)
            val matchProgress: TextView = itemView.findViewById(R.id.upcoming_match_progress)
            val upcomingLinearContestView: LinearLayout =
                itemView.findViewById(R.id.upcoming_linear_contest_view)
            val matchTime: TextView = itemView.findViewById(R.id.match_time)
        }
    }

    companion object {
        var TAG: String = MyLiveMatchesFragment::class.java.simpleName
    }
}