package fancode.cricks.utils

import android.os.Build
import fancode.cricks.BuildConfig
import java.io.Serializable

data class HardwareInfo(
    var signature: Int = 0,
    private var os: String? = BuildConfig.VERSION_NAME,
    var brand: String? = null,
    var model: String? = Build.MODEL,
    var os_sdk_int: Int? = Build.VERSION.SDK_INT,
    var os_version: String? = Build.VERSION.RELEASE,
    var package_name: String? = "",
    var device_id: String? = null,
    var manufacturer: String? = Build.MANUFACTURER,
    var appVersion: String? = Build.VERSION.RELEASE,
    var versionCode: Int? = BuildConfig.VERSION_CODE,

    var timeZone: String? = null,
    var country: String? = null,
    var noOfSIM: Int = 0,
    var adId: String? = null,

    ) : Serializable {

    fun setOs(os: String) {
        val version = String.format("(%s)", BuildConfig.VERSION_NAME)
        this.os = os + version
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
