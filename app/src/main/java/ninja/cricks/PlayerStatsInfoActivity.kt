package ninja.cricks

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tmall.ultraviewpager.transformer.UltraScaleTransformer
import ninja.cricks.databinding.ActivityPlayerStatsInfoBinding
import ninja.cricks.models.PlayerStatsInfoModel
import ninja.cricks.utils.BindingUtils
import org.json.JSONObject
import java.lang.String

class PlayerStatsInfoActivity : AppCompatActivity() {

    companion object {
        private val TAG = PlayerStatsInfoActivity::class.java.simpleName
    }

    var mContext: Context? = null
    var toolbar: Toolbar? = null
    var infoAdapter: PlayerStatsInfoAdapter? = null
    var arrayList = ArrayList<PlayerStatsInfoModel>()
    var mBinding: ActivityPlayerStatsInfoBinding? = null
    var pos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_player_stats_info)
        mContext = this

        mBinding!!.toolbar.title = "Player Info"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)

        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        try {
            arrayList.clear()
            val playerStats = intent.getStringExtra(BindingUtils.playerStatsList)

            val type = object : TypeToken<List<PlayerStatsInfoModel>>() {}.type
            val dataList = Gson().fromJson<ArrayList<PlayerStatsInfoModel>>(playerStats, type)

            arrayList.addAll(dataList)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        pos = intent.getIntExtra(BindingUtils.position, 0)
        infoAdapter = PlayerStatsInfoAdapter(mContext!!, arrayList)
        mBinding!!.viewPager.adapter = infoAdapter
        mBinding!!.viewPager.setMultiScreen(0.80f)
        //view_pager.setItemRatio(1.0f);
        //view_pager.setRatio(2.0f);
        //view_pager.setMaxHeight(800);
        mBinding!!.viewPager.setAutoMeasureHeight(true)
        mBinding!!.viewPager.setPageTransformer(false, UltraScaleTransformer())
        if (pos < arrayList.size) {
            mBinding!!.viewPager.currentItem = pos
        } else {
            mBinding!!.viewPager.currentItem = 0
        }
        infoAdapter!!.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    inner class PlayerStatsInfoAdapter(
        var mContext: Context,
        var infoModelArrayList: ArrayList<PlayerStatsInfoModel>
    ) :
        PagerAdapter() {
        var inflater: LayoutInflater = LayoutInflater.from(mContext)

        override fun getCount(): Int {
            return infoModelArrayList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var itemView: View? = null
            val viewHolder: ViewHolder
            if (itemView == null) {
                itemView = inflater.inflate(R.layout.plater_stats_info_item, container, false)
                viewHolder = ViewHolder(itemView)
                itemView.tag = viewHolder
            } else {
                viewHolder = itemView.tag as ViewHolder
            }
            try {
                val model: PlayerStatsInfoModel = infoModelArrayList[position]
                val adapter = PagerListAdapter(
                    mContext,
                    R.layout.player_stats_info_list_item,
                    model.matchPoints
                )
                viewHolder.listView.adapter = adapter
                viewHolder.playerName.text = model.name
                viewHolder.playerPoint.text = String.format("Points - %s", model.point)
                if (model.selection != null && !model.selection.equals("") && model.selection.isNotEmpty()) {
                    viewHolder.playerSelection.text =
                        String.format("Selected By - %s%%", model.selection)
                } else {
                    viewHolder.playerSelection.text = ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            container.addView(itemView)
            return itemView!!
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as RelativeLayout)
        }

        inner class ViewHolder(view: View?) {
            var listView: ListView = view!!.findViewById(R.id.listView)
            var playerImage: ImageView = view!!.findViewById(R.id.player_image)
            var playerName: TextView = view!!.findViewById(R.id.player_name)
            var playerPoint: TextView = view!!.findViewById(R.id.player_point)
            var playerSelection: TextView = view!!.findViewById(R.id.player_selection)
        }
    }

    internal class PagerListAdapter(
        mContext: Context,
        private val res: Int,
        private val objectArrayList: List<JSONObject>
    ) :
        ArrayAdapter<JSONObject>(mContext, res, objectArrayList) {
        private val inflater: LayoutInflater = LayoutInflater.from(mContext)
        private var holder: ViewHolder? = null
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (convertView == null) {
                convertView = inflater.inflate(res, parent, false)
                holder = ViewHolder(convertView)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }
            try {
                val jsonObject = objectArrayList[position]
                if (jsonObject.has("key")) {
                    holder!!.playerEvent.text = jsonObject.getString("key")

                    if (jsonObject.has("value")) {
                        holder!!.playerPoints.text = jsonObject.getString("value")
                    } else {
                        holder!!.playerPoints.text = ""
                    }
                } else {
                    holder!!.playerEvent.text = ""
                    holder!!.playerActual.text = ""
                    holder!!.playerPoints.text = ""
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return convertView!!
        }

        inner class ViewHolder(view: View?) {
            var playerEvent: TextView = view!!.findViewById(R.id.player_event)
            var playerActual: TextView = view!!.findViewById(R.id.player_actual)
            var playerPoints: TextView = view!!.findViewById(R.id.player_points)
        }
    }
}