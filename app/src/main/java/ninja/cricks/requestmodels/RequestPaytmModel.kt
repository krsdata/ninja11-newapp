package ninja.cricks.requestmodels

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/requestmodels/RequestPaytmModel.kt
=======
import androidx.annotation.Keep

@Keep
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/requestmodels/RequestPaytmModel.kt
data class RequestPaytmModel(
    internal var ORDER_ID: String,
    internal var CUST_ID: String,
    internal var TXN_AMOUNT: String,
    internal var EMAIL: String,
    internal var MOBILE_NO: String,
    internal var CALLBACK_URL: String,
    internal var MID: String,
    internal var INDUSTRY_TYPE_ID: String,
    internal var CHANNEL_ID: String,
    internal var WEBSITE: String
)