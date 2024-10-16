package ninja.cricks.utils

import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import ninja.cricks.models.PhonePePaymentInstrument
import ninja.cricks.models.PhonePePaymentRequest
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object PhonePeUtil {

    // checksum calculation =====> String checksum = sha256(base64Body + apiEndPoint + salt) + ### + saltIndex;

    fun sha256New(saltkey: String, base64: String): String? {
        // generate sha256 for base64 + /pg/v1/pay + saltkey
        val data = "$base64/pg/v1/pay$saltkey"
        val bytes = data.toByteArray(StandardCharsets.UTF_8)
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            val hash = StringBuilder()
            for (hashByte in hashBytes) {
                val hex = Integer.toHexString(0xff and hashByte.toInt())
                if (hex.length == 1) hash.append('0')
                hash.append(hex)
            }
            val result = "$hash###1"
            Log.e("", "BASE64 ===========> $base64\nSHA256New =======> $result")
            result
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }

    fun createJson(
        merchantId: String,
        merchantTransactionId: String,
        amount: Double,
        merchantUserId: String,
        redirectUrl: String,
        redirectMode: String,
        callbackUrl: String,
        mobileNumber: String
    ): String {
        // Create json from given arguments, note that paymentInstrument has type as enum
        val gson = Gson()
        val paymentInstrument = PhonePePaymentInstrument()
        paymentInstrument.setType("PAY_PAGE")
        val paymentRequest = PhonePePaymentRequest(
            merchantId,
            merchantTransactionId,
            amount,
            merchantUserId,
            redirectUrl,
            redirectMode,
            callbackUrl,
            mobileNumber,
            paymentInstrument
        )
        return gson.toJson(paymentRequest)
    }

    fun convertJsonToBase64(json: Any?): String {
        // Convert JSON to Base64
        val jsonString = Gson().toJson(json)

        val base64Bytes: ByteArray = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.util.Base64.getEncoder().encode(jsonString.toByteArray())
        } else {
            Base64.encode(jsonString.toByteArray(), Base64.NO_WRAP)
        }
        val base64String = String(base64Bytes)
        println("BASE64STRING: $base64String")
        return base64String
    }
}