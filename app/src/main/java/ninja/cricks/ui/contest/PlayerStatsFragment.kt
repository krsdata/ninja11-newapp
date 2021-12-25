package ninja.cricks.ui.contest

import android.content.Context
import android.content.Intent
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ninja.cricks.Constant
import ninja.cricks.ContestActivity
import ninja.cricks.PlayerStatsInfoActivity
import ninja.cricks.R
import ninja.cricks.databinding.FragmentPlayerStatsBinding
import ninja.cricks.models.PlayerStatsInfoModel
import ninja.cricks.models.UpcomingMatchesModel
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.roomDatabase.ResponseDatabase
import ninja.cricks.utils.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class PlayerStatsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = PlayerStatsFragment::class.java.simpleName
        fun newInstance(bundle: Bundle): PlayerStatsFragment {
            val fragment = PlayerStatsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    var mContext: Context? = null
    var objectMatches: UpcomingMatchesModel? = null
    private lateinit var binding: FragmentPlayerStatsBinding
    private lateinit var customProgressDialog: CustomProgressDialog2
    var playerStatsList = ArrayList<PlayerStatsInfoModel>()
    var adapter: PlayerStatsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
        objectMatches =
            arguments?.get(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT) as UpcomingMatchesModel
        customProgressDialog = CustomProgressDialog2(activity)
        getPlayerStats(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_player_stats, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.refreshLayout.setOnRefreshListener(this)

        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        val divider = DividerItemDecoration(mContext!!, layoutManager.orientation)
        binding.recyclerView.addItemDecoration(divider)
        adapter = PlayerStatsAdapter(playerStatsList)
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        if ((activity as ContestActivity).resPlayerStatsList.isNotEmpty() && adapter != null) {
            playerStatsList.clear()
            playerStatsList.addAll((activity as ContestActivity).resPlayerStatsList)
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onRefresh() {
        getPlayerStats(false)
    }

    private fun getPlayerStats(b: Boolean) {
        val lastTimeApiCall: Long? = MyPreferences.getLastTimeForApiCall(requireContext(),
            (Constant.playerStatsFragmentDatabaseId+objectMatches!!.matchId)
        )
        if (lastTimeApiCall!!+ Constant.delayApiSeconds < System.currentTimeMillis()) {
            getPlayerStatsApiCall(b)
        }
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val value = ResponseDatabase.getInstance(requireContext()).responseDao().getResponseJsonObject(
                    (Constant.playerStatsFragmentDatabaseId+ objectMatches!!.matchId)
                )

                if (value != null && value.type == (Constant.playerStatsFragmentDatabaseId + objectMatches!!.matchId)){
                    withContext(Dispatchers.Main){playerstats(value.res)}
                }
            }
        }
    }

    private fun getPlayerStatsApiCall(b: Boolean) {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        if (b) {
            customProgressDialog.show()
        }
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("match_id", objectMatches!!.matchId)

        WebServiceClient(mContext!!).client.create(IApiMethod::class.java)
            .getPlayerStat(jsonRequest)
            .enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    customProgressDialog.dismiss()
                    binding.refreshLayout.isRefreshing = false
                    if (response.body() != null) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.Main){ playerstats(response.body()) }
                            withContext(Dispatchers.IO){
                                MyPreferences.saveLastTimeForApiCall(context!!,Constant.playerStatsFragmentDatabaseId + objectMatches!!.matchId, System.currentTimeMillis())
                                ResponseDatabase.getInstance(context!!).responseDao().saveResponseJsonObject(ninja.cricks.roomDatabase.ResponseJsonObject(
                                    (Constant.playerStatsFragmentDatabaseId + objectMatches!!.matchId),System.currentTimeMillis(),
                                    response.body()!!
                                ))
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    customProgressDialog.dismiss()
                    binding.refreshLayout.isRefreshing = false
                }
            })

    }

    private fun playerstats(responseBody: JsonObject?) {
            playerStatsList.clear()
            Log.e(TAG, "res ======> ${responseBody}")
            val jsonObject = JSONObject(responseBody.toString())
            if (jsonObject.getBoolean("status")) {
                val jsonArray = jsonObject.getJSONArray("data")


                for (i in 0 until jsonArray.length()) {
                    val childJsonObject = jsonArray.getJSONObject(i)
                    val childArrayList = ArrayList<JSONObject>()
                    val statsArray: JSONArray = childJsonObject.getJSONArray("match_points")

                    for (j in 0 until statsArray.length()) {
                        val key: String = statsArray.optJSONObject(j).optString("key")
                        if (key != "" && key.isNotEmpty()) {
                            childArrayList.add(statsArray.optJSONObject(j))
                        }
                    }

                    val playerStatsInfoModel = PlayerStatsInfoModel(
                        childJsonObject.getString("pid"),
                        childJsonObject.getString("name"),
                        childJsonObject.getString("role"),
                        childJsonObject.getString("rating"),
                        childJsonObject.getString("point"),
                        childJsonObject.getString("team_name"),
                        childJsonObject.getString("selection"),
                        childJsonObject.getString("c_selection"),
                        childJsonObject.getString("vc_selection"),
                        childJsonObject.getString("nationality"),
                        childArrayList
                    )
                    playerStatsList.add(playerStatsInfoModel)
                }
//                            for (i in 0 until jsonArray.length()) {
//                                playerStatsList.add(jsonArray.getJSONObject(i))
//                            }

                playerStatsList.sortWith { lhs, rhs ->
                    rhs.point.toDouble()
                        .compareTo(lhs.point.toDouble())
                }
                (activity as ContestActivity).resPlayerStatsList.clear()
                (activity as ContestActivity).resPlayerStatsList.addAll(playerStatsList)
                adapter!!.notifyDataSetChanged()
            } else {
                MyUtils.showMessage(mContext!!, "Please try after sometime")
            }
    }

    inner class PlayerStatsAdapter(private val arrayList: ArrayList<PlayerStatsInfoModel>) :
        RecyclerView.Adapter<PlayerStatsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val playerName: TextView = view.findViewById(R.id.player_name)
            val playerRole: TextView = view.findViewById(R.id.player_role)
            val playerRatting: TextView = view.findViewById(R.id.player_ratting)
            val playerPoint: TextView = view.findViewById(R.id.player_point)
            val playerTeam: TextView = view.findViewById(R.id.player_team)
            val playerSelection: TextView = view.findViewById(R.id.player_selection)

            val playerImage: ImageView = view.findViewById(R.id.player_image)
            val mainLayout: LinearLayout = view.findViewById(R.id.main_layout)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(mContext!!).inflate(
                R.layout.player_stat_list_item,
                parent,
                false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                val jsonObject = arrayList[position]
                Log.e(TAG, "jsonObject =======> $jsonObject")
                holder.playerName.text = jsonObject.name
                holder.playerRole.text = jsonObject.role
                holder.playerRatting.text = String.format("Ratting: %s", jsonObject.rating)
                holder.playerPoint.text = jsonObject.point
                holder.playerTeam.text = jsonObject.teamName
                holder.playerSelection.text = String.format("Selected by: %s%s", jsonObject.selection, "%")

                holder.mainLayout.setOnClickListener {
                    val jsonPlayerStats = Gson().toJson(arrayList)

                    val intent = Intent(mContext, PlayerStatsInfoActivity::class.java)
                    intent.putExtra(BindingUtils.playerStatsList, jsonPlayerStats)
                    intent.putExtra(BindingUtils.position, position)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }
    }
}