package fancode.cricks.network

import fancode.cricks.models.UserInfo
import fancode.cricks.utils.HardwareInfo


class RequestEvent {


    var user_info: UserInfo?=null
    var event_name: String=""
    var match_id: Int=0
    var contest_id: Int=0
    var storage_permission: Int=0
    var device_id: String = ""
    var deviceDetails: HardwareInfo?=null

}
