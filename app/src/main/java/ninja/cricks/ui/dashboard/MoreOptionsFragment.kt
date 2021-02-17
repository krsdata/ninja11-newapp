package ninja.cricks.ui.dashboard

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja.cricks.*
import ninja.cricks.databinding.FragmentMoreoptionsBinding
import ninja.cricks.models.MoreOptionsModel
import ninja.cricks.ui.BaseFragment
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils

class MoreOptionsFragment : BaseFragment() {

    private var mBinding: FragmentMoreoptionsBinding? = null
    lateinit var adapter: MoreOptionsAdaptor
    var allOptionsList = ArrayList<MoreOptionsModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_moreoptions, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.appVersion.text =
            String.format("App Version: %s", MyUtils.getAppVersionName(requireActivity()))
        mBinding!!.recyclerMoreoptions.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        initContent()

        mBinding!!.progressBar.visibility = View.VISIBLE
        BackgroundLoading().execute()
    }

    private fun initContent() {
        allOptionsList.clear()

        if (MainActivity.showScore) {
            val upcomingModel11 = MoreOptionsModel()
            upcomingModel11.drawable = R.drawable.ic_action_score
            upcomingModel11.id = 11
            upcomingModel11.title = MainActivity.menuArrayList[0].getString("title")
            allOptionsList.add(upcomingModel11)
        }

        val upcomingModel10 = MoreOptionsModel()
        upcomingModel10.drawable = R.drawable.ic_action_affiliate
        upcomingModel10.id = 10
        upcomingModel10.title = "LeaderBoard Contest"
        allOptionsList.add(upcomingModel10)

        val upcomingModel = MoreOptionsModel()
        upcomingModel.drawable = R.drawable.ic_action_affiliate
        upcomingModel.id = 0
        upcomingModel.title = "My-Affiliate"
        allOptionsList.add(upcomingModel)

        val upcomingModel1 = MoreOptionsModel()
        upcomingModel1.drawable = R.drawable.ic_action_top_referral
        upcomingModel1.id = 1
        upcomingModel1.title = "Top Referral Users"
        allOptionsList.add(upcomingModel1)

        val upcomingModel2 = MoreOptionsModel()
        upcomingModel2.drawable = R.drawable.ic_action_offer
        upcomingModel2.id = 2
        upcomingModel2.title = "Latest Offers"
        allOptionsList.add(upcomingModel2)

        val upcomingModel3 = MoreOptionsModel()
        upcomingModel3.drawable = R.drawable.ic_action_reffer
        upcomingModel3.id = 3
        upcomingModel3.title = "Refer & Earn"
        allOptionsList.add(upcomingModel3)

        val upcomingModel4 = MoreOptionsModel()
        upcomingModel4.drawable = R.drawable.ic_support
        upcomingModel4.id = 4
        upcomingModel4.title = getString(R.string.label_supportteam)
        allOptionsList.add(upcomingModel4)

        val upcomingModel5 = MoreOptionsModel()
        upcomingModel5.drawable = R.drawable.ic_action_point_system
        upcomingModel5.id = 5
        upcomingModel5.title = "Fantasy Points System"
        allOptionsList.add(upcomingModel5)

        val upcomingModel6 = MoreOptionsModel()
        upcomingModel6.drawable = R.drawable.ic_action_faq
        upcomingModel6.id = 6
        upcomingModel6.title = "FAQs"
        allOptionsList.add(upcomingModel6)

        val upcomingModel7 = MoreOptionsModel()
        upcomingModel7.drawable = R.drawable.ic_action_about
        upcomingModel7.id = 7
        upcomingModel7.title = "About Us"
        allOptionsList.add(upcomingModel7)

        val upcomingModel8 = MoreOptionsModel()
        upcomingModel8.drawable = R.drawable.ic_action_terms
        upcomingModel8.id = 8
        upcomingModel8.title = "Terms and Conditions"
        allOptionsList.add(upcomingModel8)

        val upcomingModel9 = MoreOptionsModel()
        upcomingModel9.drawable = R.drawable.ic_action_logout_icon
        upcomingModel9.id = 9
        val userId = MyPreferences.getUserID(requireActivity())!!
        if (!TextUtils.isEmpty(userId)) {
            upcomingModel9.title = "Logout"
        } else {
            upcomingModel9.title = "Login"
        }
        allOptionsList.add(upcomingModel9)
    }

    inner class MoreOptionsAdaptor(
        val context: Context,
        val tradeinfoModels: ArrayList<MoreOptionsModel>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((MoreOptionsModel) -> Unit)? = null
        private var optionListObject = tradeinfoModels


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_more_options, parent, false)
            return DataViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            val objectVal = optionListObject[viewType]
            val viewHolder: DataViewHolder = parent as DataViewHolder
            viewHolder.optionsTitle?.text = objectVal.title
            viewHolder.optionIcon.setImageResource(objectVal.drawable)
        }

        override fun getItemCount(): Int {
            return optionListObject.size
        }

        inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(optionListObject[adapterPosition])
                }
            }

            val optionsTitle = itemView.findViewById<TextView>(R.id.options_title)
            val optionIcon = itemView.findViewById<ImageView>(R.id.option_icon)
        }
    }

    inner class BackgroundLoading : AsyncTask<Unit, Unit, String>() {

        override fun doInBackground(vararg params: Unit): String {
            Thread.sleep(200)
            return ""
        }

        override fun onPostExecute(result: String) {
            mBinding!!.progressBar.visibility = View.INVISIBLE

            val itemDecoration = DividerItemDecoration(requireActivity(), VERTICAL)
            mBinding!!.recyclerMoreoptions.addItemDecoration(itemDecoration)
            adapter = MoreOptionsAdaptor(requireActivity(), allOptionsList)
            mBinding!!.recyclerMoreoptions.adapter = adapter
            adapter.onItemClick = { objects ->

                when (objects.id) {
                    0 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_MY_AFFILIATE)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_MY_AFFILIATE)
                        intent.putExtra(
                            WebActivity.USER_ID,
                            MyPreferences.getUserID(requireActivity())!!
                        )
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    1 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(
                            WebActivity.KEY_TITLE,
                            BindingUtils.WEB_TITLE_TOP_REFERRAL_USER
                        )
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TOP_REFERRAL_USER)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    2 -> {
                        val intent = Intent(requireActivity(), OfferActivity::class.java)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    3 -> {
                        val intent = Intent(requireActivity(), InviteFriendsActivity::class.java)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    4 -> {
                        val intent = Intent(requireActivity(), SupportActivity::class.java)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    5 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(
                            WebActivity.KEY_TITLE,
                            BindingUtils.WEB_TITLE_FANTASY_POINTS
                        )
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FANTASY_POINTS)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    6 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_FAQ)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FAQ)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    7 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_ABOUT_US)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_ABOUT_US)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    8 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(
                            WebActivity.KEY_TITLE,
                            BindingUtils.WEB_TITLE_TERMS_CONDITION
                        )
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TNC)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    9 -> {
                        logoutApp("Are you sure you want to logout", true)
                    }
                    10 -> {
                        val intent =
                            Intent(requireActivity(), ContestLeaderBoardActivity::class.java)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    11 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_TITLE, MainActivity.menuArrayList[0].getString("title"))
                        intent.putExtra(WebActivity.KEY_URL, MainActivity.menuArrayList[0].getString("url"))
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                }
            }
        }
    }

    /*inner class BackgroundLoading : AsyncTask<Unit, Unit, String>() {

        override fun doInBackground(vararg params: Unit): String {
            Thread.sleep(200)
            return ""
        }

        override fun onPostExecute(result: String) {
            mBinding!!.progressBar.visibility = View.INVISIBLE

            val itemDecoration = DividerItemDecoration(requireActivity(), VERTICAL)
            mBinding!!.recyclerMoreoptions.addItemDecoration(itemDecoration)
            adapter = MoreOptionsAdaptor(requireActivity(), allOptionsList)
            mBinding!!.recyclerMoreoptions.adapter = adapter
            adapter.onItemClick = { objects ->

                when (objects.id) {
                    0 -> {
                        val intent = Intent(requireActivity(), InviteFriendsActivity::class.java)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    1 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(
                            WebActivity.KEY_TITLE,
                            BindingUtils.WEB_TITLE_FANTASY_POINTS
                        )
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FANTASY_POINTS)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    2 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_HOW_TO_PLAY)
                        intent.putExtra(
                            WebActivity.KEY_URL,
                            BindingUtils.WEBVIEW_FANTASY_HOW_TO_PLAY
                        )
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    3 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_ABOUT_US)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_ABOUT_US)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    4 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_LEGALITY)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_LEGALITY)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                    5 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(
                            WebActivity.KEY_TITLE,
                            BindingUtils.WEB_TITLE_TERMS_CONDITION
                        )
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TNC)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }

                    6 -> {
                        val intent = Intent(requireActivity(), SupportActivity::class.java)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }

                    8 -> {
                        logoutApp("Are you sure you want to logout", true)
                    }

                    9 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_FAQ)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FAQ)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }

                    10 -> {
                        val intent = Intent(requireActivity(), OfferActivity::class.java)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }

                    11 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(
                            WebActivity.KEY_TITLE,
                            BindingUtils.WEB_TITLE_TOP_REFERRAL_USER
                        )
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TOP_REFERRAL_USER)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }

                    12 -> {
                        val intent = Intent(requireActivity(), WebActivity::class.java)
                        intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_MY_AFFILIATE)
                        intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_MY_AFFILIATE)
                        intent.putExtra(WebActivity.USER_ID, MyPreferences.getUserID(requireActivity())!!)
                        val options =
                            ActivityOptions.makeSceneTransitionAnimation(activity)
                        startActivity(intent, options.toBundle())
                    }
                }
            }
        }
    }*/
}