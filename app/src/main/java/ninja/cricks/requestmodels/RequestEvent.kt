package ninja.cricks.requestmodels

import androidx.annotation.Keep
import ninja.cricks.models.UserInfo
import ninja.cricks.utils.HardwareInfo

@Keep
class RequestEvent {

    var user_info: UserInfo? = null
    var event_name: String = ""
    var match_id: Int = 0
    var contest_id: Int = 0
    var storage_permission: Int = 0
    var device_id: String = ""
    var deviceDetails: HardwareInfo? = null
}