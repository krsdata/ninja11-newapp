package ninja.cricks.ui.contest

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.JsonObject
import ninja.cricks.ContestActivity
import ninja.cricks.R
import ninja.cricks.databinding.FragmentPlayerStatsBinding
import ninja.cricks.models.UpcomingMatchesModel
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private lateinit var customProgressDialog: CustomeProgressDialog
    var playerStatsList = ArrayList<JSONObject>()
    var adapter: PlayerStatsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
        objectMatches =
            arguments?.get(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT) as UpcomingMatchesModel
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
        customProgressDialog = CustomeProgressDialog(activity)
        binding.refreshLayout.setOnRefreshListener(this)

        val layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager

    }

    override fun onResume() {
        super.onResume()
        getPlayerStats(true)
    }

    override fun onRefresh() {
        getPlayerStats(false)
    }

    private fun getPlayerStats(b: Boolean) {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        customProgressDialog.show()
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
                        playerStatsList.clear()
                        Log.e(TAG, "res ======> ${response.body()}")
                        val jsonObject = JSONObject(response.body().toString())
                        if (jsonObject.getBoolean("status")) {
                            val jsonArray = jsonObject.getJSONArray("data")

                            for (i in 0 until jsonArray.length()) {
                                playerStatsList.add(jsonArray.getJSONObject(i))
                            }
                            adapter = PlayerStatsAdapter(playerStatsList)
                            binding.recyclerView.adapter = adapter
                            adapter!!.notifyDataSetChanged()
                        } else {
                            MyUtils.showMessage(mContext!!, "Please try after sometime")
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    customProgressDialog.dismiss()
                    binding.refreshLayout.isRefreshing = false
                }
            })
    }

    inner class PlayerStatsAdapter(private val arrayList: ArrayList<JSONObject>) :
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
                holder.playerName.text = jsonObject.getString("name")
                holder.playerRole.text = jsonObject.getString("role")
                holder.playerRatting.text = jsonObject.getString("rating")
                holder.playerPoint.text = jsonObject.getString("point")
                holder.playerTeam.text = jsonObject.getString("team_name")
                holder.playerSelection.text = jsonObject.getString("selection")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }
    }
}