package plug.cricket.ui.createteam

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edify.atrist.listener.OnTeamCreateListener
import ninja.cricks.ContestActivity
import ninja.cricks.CreateTeamActivity
import ninja.cricks.CreateTeamActivity.Companion.MAX_BATSMAN
import ninja.cricks.R
import ninja.cricks.databinding.FragmentCreateTeamListBinding
import ninja.cricks.models.UpcomingMatchesModel
import ninja.cricks.ui.createteam.adaptors.PlayersContestAdapter
import ninja.cricks.ui.createteam.models.PlayersInfoModel
import ninja.cricks.utils.CricketPlayersFilters
import ninja.cricks.utils.MyUtils


class Batsman : Fragment() {
    var batsmenListFilter: ArrayList<PlayersInfoModel>? = null
    var matchObject: UpcomingMatchesModel? = null
    var count = 0
    private lateinit var mListener: OnTeamCreateListener
    private var mBinding: FragmentCreateTeamListBinding? = null
    lateinit var adapter: PlayersContestAdapter

    companion object {
        fun newInstance(bundle: Bundle): Batsman {
            val fragment = Batsman()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        batsmenListFilter =
            arguments!!.get(CreateTeamActivity.SERIALIZABLE_KEY_PLAYERS) as ArrayList<PlayersInfoModel>
        matchObject =
            arguments!!.get(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT) as UpcomingMatchesModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_create_team_list, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.labelPlayersCounts.text =
            String.format("Select %d - %d Batsmen", MAX_BATSMAN[0], MAX_BATSMAN[1])
        batsmenListFilter = CricketPlayersFilters.getPlayersbyOddEvenPositions(
            batsmenListFilter!!,
            matchObject!!,
            CreateTeamActivity.CREATE_TEAM_BATSMAN
        )
        resetSorting()
        mBinding!!.recyclerCreatePlayersList.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(
            mBinding!!.recyclerCreatePlayersList.context,
            RecyclerView.VERTICAL
        )
        mBinding!!.recyclerCreatePlayersList.addItemDecoration(dividerItemDecoration)

        adapter = PlayersContestAdapter(
            activity!!,
            batsmenListFilter!!,
            matchObject!!
        )
        mBinding!!.recyclerCreatePlayersList.adapter = adapter

        adapter.onItemClick = { objects ->
            CreateTeamActivity.isEditMode = false
            if (objects.isSelected) {
                count--
                objects.isSelected = false
                mListener.onBatsManDeSelected(objects)
            } else {
                if (!CreateTeamActivity.isAllPlayersSelected!!) {
                    if (count < CreateTeamActivity.MAX_BATSMAN[1]) {
                        if (isMaxPlayersValid(objects)) {
                            if (isMinimumPlayerSelected()) {
                                count++
                                objects.isSelected = true
                                mListener.onBatsManSelected(objects)
                            }
                        } else {
                            MyUtils.showToast(
                                activity!! as AppCompatActivity,
                                "MAX Player Reached limit  " + objects.teamShortName
                            )
                        }
                    } else {
                        MyUtils.showToast(
                            activity!! as AppCompatActivity,
                            "MAX ALLOWED is " + CreateTeamActivity.MAX_BATSMAN[1]
                        )
                    }
                } else {
                    MyUtils.showToast(activity!! as AppCompatActivity, "ALL 11 Players Selected")
                }
            }
            adapter.notifyDataSetChanged()
        }
    }


    fun setFilterIfActive() {
        activateSelectionSorting()
        activatePointsSorting()
        activateCreditSorting()
    }

    private fun resetSorting() {
        mBinding!!.sortBySelectedBy.setOnClickListener(View.OnClickListener {
            (activity as CreateTeamActivity).sortBySelections()
            activateSelectionSorting()
        })
        mBinding!!.sortBySelectedArrow.visibility = View.GONE

        mBinding!!.sortByPoints.setOnClickListener(View.OnClickListener {
            (activity as CreateTeamActivity).sortByPoints()

            activatePointsSorting()

        })
        mBinding!!.sortByPointsArrow.visibility = View.GONE


        mBinding!!.sortByCredits.setOnClickListener(View.OnClickListener {
            (activity as CreateTeamActivity).sortByCredits()
            activateCreditSorting()


        })
        mBinding!!.sortByCreditsArrow.visibility = View.GONE


    }

    private fun activateCreditSorting() {
        if (CreateTeamActivity.isSortByCreditsActive!!) {
            mBinding!!.sortByPointsArrow.visibility = View.GONE
            mBinding!!.sortByCreditsArrow.visibility = View.VISIBLE
            mBinding!!.sortBySelectedArrow.visibility = View.GONE

            if (CreateTeamActivity.isSortByCreditsActiveDecending!!) {
                mBinding!!.sortByCreditsArrow.setImageResource(R.drawable.ic_baseline_arrow_upward_24)
            } else {
                mBinding!!.sortByCreditsArrow.setImageResource(R.drawable.ic_baseline_arrow_downward_24)
            }
            var swapArray = CricketPlayersFilters.getPlayersbyMaxCredits(
                batsmenListFilter!!,
                CreateTeamActivity.isSortByCreditsActiveDecending!!
            )

            batsmenListFilter!!.clear()
            batsmenListFilter!!.addAll(swapArray)

            adapter.notifyDataSetChanged()
        }
    }

    private fun activatePointsSorting() {
        if (CreateTeamActivity.isSortByPointsActive!!) {
            mBinding!!.sortByPointsArrow.visibility = View.VISIBLE
            mBinding!!.sortByCreditsArrow.visibility = View.GONE
            mBinding!!.sortBySelectedArrow.visibility = View.GONE

            if (CreateTeamActivity.isSortByPointsActiveDecending!!) {
                mBinding!!.sortByPointsArrow.setImageResource(R.drawable.ic_baseline_arrow_upward_24)
            } else {
                mBinding!!.sortByPointsArrow.setImageResource(R.drawable.ic_baseline_arrow_downward_24)
            }
            var swapArray = CricketPlayersFilters.getPlayersbyMaxPoints(
                batsmenListFilter!!,
                CreateTeamActivity.isSortByPointsActiveDecending!!
            )

            batsmenListFilter!!.clear()
            batsmenListFilter!!.addAll(swapArray)

            adapter.notifyDataSetChanged()
        }
    }

    private fun activateSelectionSorting() {
        if (CreateTeamActivity.isSortBySelectionActive!!) {
            mBinding!!.sortByPointsArrow.visibility = View.GONE
            mBinding!!.sortByCreditsArrow.visibility = View.GONE
            mBinding!!.sortBySelectedArrow.visibility = View.VISIBLE
            if (CreateTeamActivity.isSortBySelectionActiveDecending!!) {
                mBinding!!.sortBySelectedArrow.setImageResource(R.drawable.ic_baseline_arrow_upward_24)
            } else {
                mBinding!!.sortBySelectedArrow.setImageResource(R.drawable.ic_baseline_arrow_downward_24)
            }
            var swapArray = CricketPlayersFilters.getPlayersbyMaxSelection(
                batsmenListFilter!!,
                CreateTeamActivity.isSortBySelectionActiveDecending!!
            )

            batsmenListFilter!!.clear()
            batsmenListFilter!!.addAll(swapArray)

            adapter.notifyDataSetChanged()
        }
    }

    private fun isMinimumPlayerSelected(): Boolean {
        if ((activity!! as CreateTeamActivity).isSpotAvailable(CreateTeamActivity.WANT_BAT)) {
            if (CreateTeamActivity.COUNT_WICKET_KEEPER < CreateTeamActivity.MAX_WICKET_KEEPER[0]) {
                MyUtils.showToast(
                    activity!! as AppCompatActivity,
                    "Minimum " + CreateTeamActivity.MAX_WICKET_KEEPER[0] + " " + "Wicket Keeper Required"
                )
                return false
            } else if (CreateTeamActivity.COUNT_BATS_MAN < CreateTeamActivity.MAX_BATSMAN[0]) {
                //MyUtils.showToast(activity!!.getWindow().getDecorView().getRootView(),"Minimum "+ CreateTeamActivity.MAX_BATSMAN[0]+" "+"BatsMan Required")
                return true
            } else if (CreateTeamActivity.COUNT_ALL_ROUNDER < CreateTeamActivity.MAX_ALL_ROUNDER[0]) {
                MyUtils.showToast(
                    activity!! as AppCompatActivity,
                    "Minimum " + CreateTeamActivity.MAX_ALL_ROUNDER[0] + " " + "All Rounder Required"
                )
                return false
            } else if (CreateTeamActivity.COUNT_BOWLER < CreateTeamActivity.MAX_BOWLER[0]) {
                MyUtils.showToast(
                    activity!! as AppCompatActivity,
                    "Minimum " + CreateTeamActivity.MAX_BOWLER[0] + " " + "BOWLER Required"
                )
                return false

            }
            return true
        }
        return true
    }

    private fun isMaxPlayersValid(objects: PlayersInfoModel): Boolean {
        if (objects.teamId == CreateTeamActivity.teamAId && CreateTeamActivity.TEAMA < CreateTeamActivity.MAX_PLAYERS_FROM_TEAM) {
            return true
        } else if (objects.teamId == CreateTeamActivity.teamBId && CreateTeamActivity.TEAMB < CreateTeamActivity.MAX_PLAYERS_FROM_TEAM) {
            return true
        }
        return false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnTeamCreateListener) {
            mListener = context
        } else {
            throw RuntimeException(
                "$context must implement OnTeamCreateListener"
            )
        }

    }

}
