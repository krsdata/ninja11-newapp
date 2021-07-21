package justkhelo.cricks.models

data class DataModel(
    val userList: ArrayList<LeaderBoardModel>,
    val match_name: String = "",
    val rank: ArrayList<RankModel>
)