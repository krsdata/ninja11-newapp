package ninja.cricks.ui.contest.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ninja.cricks.models.MyTeamModels
import java.io.Serializable


class ContestModelLists :Serializable,Cloneable {

    @SerializedName("contestId")
    @Expose
    var id: Int = 0

    @SerializedName("isCancelled")
    @Expose
    var isContestCancelled: Boolean = false

    @SerializedName("match_id")
    @Expose
    var matchId: String = ""

    @SerializedName("totalWinningPrize")
    @Expose
    var totalWinningPrize: Int = 0

    @SerializedName("entryFees")
    @Expose
    var entryFees: Int = 0

    @SerializedName("match_status")
    @Expose
    var matchStatus: String = ""

    @SerializedName("totalSpots")
    @Expose
    var totalSpots: Int = 0

    @SerializedName("filledSpots")
    @Expose
    var filledSpots: Int = 0

    @SerializedName("firstPrice")
    @Expose
    var firstPrice: Int = 0

    @SerializedName("winnerPercentage")
    @Expose
    var winnerPercentage: Int = 0

    @SerializedName("winnerCount")
    @Expose
    var winnerCounts: Int = 0

    @SerializedName("maxAllowedTeam")
    @Expose
    var maxAllowedTeam: Int = 0

    @SerializedName("usable_bonus")
    @Expose
    var usableBonus: Int = 0

    @SerializedName("bonus_contest")
    @Expose
    var isBonusContest: Boolean = false

    @SerializedName("cancellation")
    @Expose
    var cancellation: Boolean = false

    @SerializedName("joinedTeams")
    @Expose
    var joinedTeams: ArrayList<MyTeamModels>? = null



    public override fun clone(): ContestModelLists {
        return super.clone() as ContestModelLists
    }



}
