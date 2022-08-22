package fancode.cricks.requestmodels

import androidx.annotation.Keep
import fancode.cricks.models.UserInfo
import fancode.cricks.utils.HardwareInfo

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