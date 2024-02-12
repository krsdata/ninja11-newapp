package ninja.cricks.models

class PhonePePaymentRequest(
    private val merchantId: String, private val merchantTransactionId: String, private val amount: Double, private val merchantUserId: String,
    private val redirectUrl: String, private val redirectMode: String, private val callbackUrl: String, private val mobileNumber: String,
    private val paymentInstrument: PhonePePaymentInstrument
)
