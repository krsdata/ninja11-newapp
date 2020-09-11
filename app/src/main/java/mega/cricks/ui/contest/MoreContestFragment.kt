package mega.cricks.ui.contest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edify.atrist.listener.OnContestEvents
import com.edify.atrist.listener.OnContestLoadedListener
import mega.cricks.ContestActivity
import mega.cricks.LeadersBoardActivity
import mega.cricks.R
import mega.cricks.models.UpcomingMatchesModel
import mega.cricks.ui.contest.adaptors.ContestListAdapter
import mega.cricks.ui.contest.models.ContestModelLists
import mega.cricks.utils.CustomeProgressDialog
import mega.cricks.databinding.FragmentMoreContestBinding


class MoreContestFragment: Fragment() {
    var mListenerContestEvents: OnContestLoadedListener?=null
    var mListener: OnContestLoadedListener?=null

    private lateinit var allContestList: java.util.ArrayList<ContestModelLists>
    var objectMatches: UpcomingMatchesModel?=null
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var mBinding: FragmentMoreContestBinding? = null
    lateinit var adapter: ContestListAdapter

    companion object{
        val CONTEST_LIST: String?="contestlist"

        fun newInstance(bundle : Bundle) : MoreContestFragment{
            val fragment = MoreContestFragment()
            fragment.arguments=bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        objectMatches = arguments!!.get(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT) as UpcomingMatchesModel
        allContestList = arguments!!.get(CONTEST_LIST) as  ArrayList<ContestModelLists>

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnContestLoadedListener) {
            mListener = context
        }else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }

        if (context is OnContestEvents) {
            mListenerContestEvents = context
        }else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_more_contest, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(activity)

        mBinding!!.recyclerMyContest.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        var  colorCode =  activity!!.resources.getColor(R.color.white)
        adapter = ContestListAdapter(activity!!, allContestList, objectMatches!!, mListenerContestEvents as OnContestEvents, colorCode)
        mBinding!!.recyclerMyContest.adapter = adapter


        adapter.onItemClick= { objects ->
            val intent = Intent(context, LeadersBoardActivity::class.java)
            intent.putExtra(LeadersBoardActivity.SERIALIZABLE_MATCH_KEY, objectMatches)
            intent.putExtra(LeadersBoardActivity.SERIALIZABLE_CONTEST_KEY, objects)
            activity!!.startActivityForResult(intent, LeadersBoardActivity.CREATETEAM_REQUESTCODE)
        }

    }


}
