package ninja.cricks.ui.dashboard

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import ninja.cricks.MaintainanceActivity
import ninja.cricks.NinjaApplication
import ninja.cricks.R
import ninja.cricks.adaptors.MatchesAdapter
import ninja.cricks.databinding.FragmentAllGamesBinding
import ninja.cricks.listener.RecyclerViewLoadMoreScroll
import ninja.cricks.models.MatchesModels
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.BaseFragment
import ninja.cricks.utils.BindingUtils
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FixtureCricketFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        val TAG: String = FixtureCricketFragment::class.java.simpleName
        fun newInstance() = FixtureCricketFragment()
        var pageNo = 1
    }

    private var mBinding: FragmentAllGamesBinding? = null
    lateinit var adapter: MatchesAdapter
    var allmatchesArrayList = ArrayList<MatchesModels>()
    var scrollListener: RecyclerViewLoadMoreScroll? = null
    lateinit var sdialog: Dialog
    var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_all_games, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.allGameViewRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        mBinding!!.linearEmptyContest.visibility = View.GONE

        mBinding!!.swipeRefresh.setColorScheme(
            R.color.colorPrimary
        )

        mBinding!!.swipeRefresh.setOnRefreshListener(this)

        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        scrollListener = RecyclerViewLoadMoreScroll(linearLayoutManager)

        mBinding!!.allGameViewRecycler.layoutManager = linearLayoutManager

        val upcomingmatchlist =
            (requireActivity().applicationContext as NinjaApplication).getUpcomingMatches
        if (upcomingmatchlist.size > 0) {
            allmatchesArrayList.clear()
            allmatchesArrayList.addAll(upcomingmatchlist)
        }
        adapter = MatchesAdapter(requireActivity(), allmatchesArrayList)
        mBinding!!.allGameViewRecycler.adapter = adapter

        if (NinjaApplication.lastApiCallForMatch + 120000 < System.currentTimeMillis()) {
            getAllMatches()
        }
    }

    private fun isValidRequest(): Boolean {
        val offset = 10
        val cal = (pageNo * offset) + 1
        if (adapter.itemCount <= cal) {
            return true
        } else {
            return true
        }
    }

    fun updateEmptyViews() {
        if (allmatchesArrayList.size == 0) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
            mBinding!!.btnEmptyView.setOnClickListener(View.OnClickListener {
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse(BindingUtils.WEBVIEW_TNC)
                startActivity(openURL)
            })

        } else {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }
    }

    private fun getAllMatches() {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            mBinding!!.swipeRefresh.isRefreshing = false

            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "NO Internet Connection found!!",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Retry") {
                // Call action functions here
                getAllMatches()
            }.setActionTextColor(resources.getColor(R.color.red)).show()
            return
        }
        mBinding!!.swipeRefresh.isRefreshing = true

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getAllMatches(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    mBinding!!.swipeRefresh.isRefreshing = false
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (!isVisible) {
                        return
                    }
                    val resObje = response!!.body()
                    mBinding!!.swipeRefresh.isRefreshing = false

                    if (resObje != null && resObje.appMaintainance) {
                        val intent = Intent(activity, MaintainanceActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    } else
                        if (resObje != null) {
                            if (resObje.status) {
                                if (resObje.sessionExpired) {
                                    logoutApp("Session Expired Please login again!!", false)
                                } else {
                                    BindingUtils.currentTimeStamp = resObje.systemTime
                                    NinjaApplication.lastApiCallForMatch = System.currentTimeMillis()
                                    val responseObject = resObje.responseObject
                                    val listofData =
                                        responseObject!!.matchdatalist as ArrayList<MatchesModels>?
                                    (requireActivity().applicationContext as NinjaApplication).saveUpcomingMatches(
                                        listofData
                                    )
                                    if (listofData!!.size > 0) {
                                        addAllList(listofData)
                                        adapter.setMatchesList(allmatchesArrayList)
                                    }
                                    val offerImage = resObje.offerImage
                                    if (offerImage != "" && offerImage.contains("http")) {
                                        showAlert(offerImage)
                                    }
                                }
                            } else {
                                if (resObje.code == 1001) {
                                    MyUtils.showMessage(requireActivity(), resObje.message)
                                    MyUtils.logoutApp(requireActivity())
                                } else {
                                    MyUtils.showMessage(requireActivity(), resObje.message)
                                }
                            }
                        }
                    updateEmptyViews()
                }
            })
    }

    private fun addAllList(userPostData: java.util.ArrayList<MatchesModels>) {
        if (isValidRequest()) {
            allmatchesArrayList.clear()
            allmatchesArrayList.addAll(userPostData)
        }
    }

    override fun onRefresh() {
        getAllMatches()
    }

    private fun showAlert(offerImage: String) {
        sdialog = Dialog(mContext!!, R.style.MyDialog)
        sdialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        sdialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        sdialog.setContentView(R.layout.dialog_offer_image)
        sdialog.setCancelable(false)
        sdialog.show()
        val close: ImageView = sdialog.findViewById(R.id.dialog_close)
        val offerImageView: ImageView = sdialog.findViewById(R.id.dialog_offer_image)
        val progressBar: ProgressBar = sdialog.findViewById(R.id.progress_bar)

        close.setOnClickListener {
            sdialog.dismiss()
        }

        Glide.with(mContext!!).load(offerImage).listener(object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any,
                target: Target<Drawable?>,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any,
                target: Target<Drawable?>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                progressBar.visibility = View.GONE
                return false
            }
        }).into(offerImageView)

        sdialog.setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss()
            }
            true
        })
    }
}