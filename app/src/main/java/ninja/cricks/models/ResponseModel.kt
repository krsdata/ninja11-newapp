package ninja.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ResponseModel(

    @SerializedName("status")
    val status: Boolean = false,

    @SerializedName("code")
    val statusCode: Int = 0,

    @SerializedName("is_otp_required")
    val isOTPRequired: Boolean = false,

    @SerializedName("message")
    val message: String = "",

    @SerializedName("is_account_verified")
    val isAccountVerified: Int = 0,

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/models/ResponseModel.kt
=======
    @SerializedName("image_url")
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/models/ResponseModel.kt
    val image_url: String = "",

    @SerializedName("token")
    @Expose
    val token: String = "1",

    @SerializedName("g_pay")
    @Expose
    val gpayid: String = "",

    @SerializedName("pmid")
    @Expose
    val paytmMid: String = "",

    @SerializedName("call_url")
    @Expose
    val callbackurrl: String = "",

    @SerializedName("CHECKSUMHASH")
    val checksum: String = "",

    @SerializedName("data")
    var infomodel: UserInfo? = null,

    @SerializedName("response")
    @Expose
    var responseModels: Response? = null,

    @SerializedName("rozar_key")
    @Expose
    var razorPay: String = "",

    @SerializedName("base_url")
    @Expose
    var baseUrl: String = "",

    @SerializedName("system_token")
    @Expose
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/models/ResponseModel.kt
    var systemToken: String = ""
=======
    var systemToken: String = "",

    @SerializedName("pass_code")
    @Expose
    var passcode: Boolean
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/models/ResponseModel.kt


) : Serializable