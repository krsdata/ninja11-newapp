package mega.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import mega.cricks.ui.contest.models.ContestModelLists
import java.io.Serializable


class ContestsParentModels:Serializable,Cloneable {

    var contestTitle : String=""
    var contestSubTitle : String=""

    @SerializedName("contests")
    @Expose
    var allContestsRunning: ArrayList<ContestModelLists> ?=null


}
