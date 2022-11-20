package ninja.cricks.requestmodels

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/requestmodels/RequestEvent.kt
import ninja.cricks.models.UserInfo
import ninja.cricks.utils.HardwareInfo


=======
import androidx.annotation.Keep
import ninja.cricks.models.UserInfo
import ninja.cricks.utils.HardwareInfo

@Keep
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/requestmodels/RequestEvent.kt
class RequestEvent {

    var user_info: UserInfo? = null
    var event_name: String = ""
    var match_id: Int = 0
    var contest_id: Int = 0
    var storage_permission: Int = 0
    var device_id: String = ""
    var deviceDetails: HardwareInfo? = null
}