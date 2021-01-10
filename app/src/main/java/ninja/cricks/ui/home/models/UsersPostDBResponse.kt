package ninja.cricks.ui.home.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ninja.cricks.models.*
import ninja.cricks.ui.contest.models.ContestModelLists
import ninja.cricks.ui.createteam.models.PlayersInfoModel
import ninja.cricks.ui.leadersboard.models.LeadersBoardModels
import ninja.cricks.ui.leadersboard.models.PrizeBreakUpModels

class UsersPostDBResponse {

    @SerializedName("status")
    @Expose
    var status: Boolean = false

    @SerializedName("order_id")
    @Expose
    var orderId: String = ""

    @SerializedName("maintainance")
    @Expose
    var appMaintainance: Boolean = false

    @SerializedName("session_expired")
    @Expose
    var sessionExpired: Boolean = false

    @SerializedName("url")
    @Expose
    var updatedApkUrl: String = ""

    @SerializedName("splashScreen")
    @Expose
    var splash: String = ""

    @SerializedName("force_update")
    @Expose
    var forceupdate: Boolean = false

    @SerializedName("release_note")
    @Expose
    var releaseNote: String = ""

    @SerializedName("action")
    @Expose
    var actionForTeam: Int = 0

    @SerializedName("team_list")
    @Expose
    var selectedTeamModel: ArrayList<SelectedTeamModels>? = null

    @SerializedName("notification_list")
    @Expose
    var notificationList: ArrayList<NotifyModels>? = null

    @SerializedName("system_time")
    @Expose
    var systemTime: Long = 0

    @SerializedName("total_points")
    @Expose
    var totalPoints: String = ""

    @SerializedName("message")
    @Expose
    var message: String = ""

    @SerializedName("code")
    @Expose
    var code: Int = 0

    @SerializedName("walletInfo")
    @Expose
    var walletObjects: WalletInfo? = null

    @SerializedName("transaction_history")
    @Expose
    var transactionHistory: WalletInfo? = null

    @SerializedName("referal_user")
    @Expose
    var referalUserList: ArrayList<RefferalUsersModel>? = null

    @SerializedName("response")
    @Expose
    var responseObject: Response? = null

    @SerializedName("scores")
    @Expose
    var scoresModel: ScoresBoardModels? = null

    @SerializedName("leaderBoard")
    @Expose
    var leaderBoardList: ArrayList<LeadersBoardModels>? = null

    inner class Response {

        @SerializedName("total_team_joined")
        @Expose
        var totalTeamJoined: Int? = null

        @SerializedName("total_match_played")
        @Expose
        var totalMatchPlayed: Int? = null

        @SerializedName("total_contest_joined")
        @Expose
        var totalContestJoined: Int? = null

        @SerializedName("total_unique_contest")
        @Expose
        var totalUniqueContest: Int? = null

        @SerializedName("total_match_win")
        @Expose
        var totalMatchWin: Int? = null

        @SerializedName("total_winning_amount")
        @Expose
        var totalWinningAmount: Int? = null

        @SerializedName("matchdata")
        @Expose
        var matchdatalist: List<MatchesModels>? = null

        @SerializedName("matchcontests")
        @Expose
        var matchContestlist: List<ContestsParentModels>? = null

        @SerializedName("prizeBreakup")
        @Expose
        var prizeBreakUpModelsList: List<PrizeBreakUpModels>? = null

        @SerializedName("players")
        @Expose
        var playersList: PlayerModels? = null

        @SerializedName("myteam")
        @Expose
        var myTeamList: List<MyTeamModels>? = null

        @SerializedName("my_joined_contest")
        @Expose
        var myJoinedContest: List<ContestModelLists>? = null

        @SerializedName("myjoinedTeams")
        @Expose
        var myjoinedTeams: ArrayList<MyTeamModels>? = null

        @SerializedName("myjoinedContest")
        @Expose
        var joinedContestDetails: ArrayList<ContestModelLists>? = null

        @SerializedName("player_points")
        @Expose
        var playerPointsList: ArrayList<PlayersInfoModel>? = null

        @SerializedName("pan_number")
        @Expose
        var panNumber: String = ""

        @SerializedName("pan_name")
        @Expose
        var panName: String = ""

        @SerializedName("pan_url")
        @Expose
        var panUrl: String = ""

        @SerializedName("bank_name")
        @Expose
        var bankName: String = ""

        @SerializedName("account_name")
        @Expose
        var accountName: String = ""

        @SerializedName("account_number")
        @Expose
        var accountNumber: String = ""

        @SerializedName("ifsc_code")
        @Expose
        var IFSCCode: String = ""

        @SerializedName("account_type")
        @Expose
        var accountType: String = ""

        @SerializedName("bank_url")
        @Expose
        var bankUrl: String = ""

        @SerializedName("paytm_number")
        @Expose
        var paytmNumber: String = ""

        @SerializedName("upi_id")
        @Expose
        var UPIId: String = ""
    }

    inner class ScoresBoardModels {

        @SerializedName("status")
        @Expose
        var matchStatus: Int = 0

        @SerializedName("status_note")
        @Expose
        var statusNote: String = ""

        @SerializedName("teama")
        @Expose
        var teama: TeamAInfo? = null

        @SerializedName("teamb")
        @Expose
        var teamb: TeamBInfo? = null
    }

    @SerializedName("rozar_key")
    @Expose
    var razorPay: String = ""

    @SerializedName("paytm_show")
    @Expose
    var paytm_show: Boolean = false

    @SerializedName("rozarpay_show")
    @Expose
    var rozarpay_show: Boolean = false

    @SerializedName("gpay_show")
    @Expose
    var gpay_show: Boolean = false

    @SerializedName("bank_withdrawal")
    @Expose
    var bank_withdrawal: Boolean = false

    @SerializedName("paytm_withdrawal")
    @Expose
    var paytm_withdrawal: Boolean = false

    @SerializedName("upi_withdrawal")
    @Expose
    var upi_withdrawal: Boolean = false

    @SerializedName("offer_image")
    @Expose
    var offerImage: String = ""
}