package ninja.cricks.utils

import android.os.CountDownTimer
import android.util.Log
import com.edify.atrist.listener.OnMatchTimerStarted
import ninja.cricks.BuildConfig
import java.util.*


class BindingUtils {

    companion object {
        val UNLIMITED_SPOT_MARGIN: Int = 6
        val MINIMUM_DEPOSIT_AMOUNT: Int = 25
        private var isCountedObjectCreated: Boolean = false
        var timer: CountDownTimer? = null
        val REUEST_STATUS_CODE_FRAUD: Int = 420
        val MATCH_STATUS_UPCOMING: Int = 1
        val MATCH_STATUS_LIVE: Int = 3

        val BANK_DOCUMENTS_STATUS_REJECTED: Int = 3
        val BANK_DOCUMENTS_STATUS_VERIFIED: Int = 2
        val BANK_DOCUMENTS_STATUS_APPROVAL_PENDING: Int = 1
        val BANNERS_KEY_ADD: String? = "ADD"
        val BANNERS_KEY_REFFER: String? = "reffer"
        val BANNERS_KEY_SUPPORT: String? = "support"
        val BANNERS_KEY_BROWSERS: String? = "browser"
        val EMAIL: String? = "support@ninja11.in"
        val PHONE_NUMBER: String? = "=+91"
        val GOOGLE_TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user"
        val PAYMENT_GOOGLEPAY_UPI = "jangrasachin091999-2@okicici"
        val BASE_URL_MAIN = "https://ninja11.in/"
        val BASE_URL_API = "https://app.ninja11.in/"
        val WEBVIEW_FANTASY_POINTS = BASE_URL_MAIN + "fantasy-points-system/index.html"
        val WEBVIEW_FANTASY_HOW_TO_PLAY = BASE_URL_MAIN + "how-to-play?request=mobile"
        val WEBVIEW_TNC = BASE_URL_MAIN + "terms-and-conditions?request=mobile"
        val WEBVIEW_PRIVACY = BASE_URL_MAIN + "privacy-policy?request=mobile"
        val WEBVIEW_ABOUT_US = BASE_URL_MAIN + "about-us?request=mobile"
        val WEBVIEW_LEGALITY = BASE_URL_MAIN + "legality?request=mobile"
        const val NOTIFICATION_ID_BIG_IMAGE = 101
        val BILTY_APK_LINK: String = BASE_URL_API + "apk"
        val WEB_TITLE_PRIVACY_POLICY: String = "Privacy Policy"
        val WEB_TITLE_TERMS_CONDITION: String = "Terms & Conditions"
        val WEB_TITLE_HOW_TO_PLAY: String = "How To Play"
        val WEB_TITLE_ABOUT_US: String = "About Us"
        val WEB_TITLE_FANTASY_POINTS: String = "Fantasy Point System"
        val WEB_TITLE_LEGALITY: String = "LEGALITY"

        var currentTimeStamp: Long = 0

        fun logD(tag: String, message: String) {
            if (BuildConfig.MLOG) {
                Log.e("MX:$tag", message)
            }
        }

        fun countDownStart(
            starttimeStamp: Long,
            listeners: OnMatchTimerStarted
        ) {
            if (starttimeStamp > currentTimeStamp && !isCountedObjectCreated) {
                isCountedObjectCreated = true
                //BindingUtils.logD("TimerLogs","Count Down timer Called")
                timer = object : CountDownTimer(starttimeStamp, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val itemLong = starttimeStamp
                        val date = Date(itemLong * 1000L)
                        //val date2 = Date(currentTimeStamp*1000L)
                        val date2 = Date()
                        if (date2.before(date)) {
                            val l8 = date.time - date2.time
                            val l2 = l8 / 86400000L
                            java.lang.Long.signum(l2)
                            val l3 = l8 - 86400000L * l2
                            val l4 = l3 / 3600000L
                            val l5 = l3 - 3600000L * l4
                            try {
                                val l6 = l5 / 60000L
                                val l7 = (l5 - 60000L * l6) / 1000L
                                //val customTextView2: CustomTextView = customTextView
                                val stringBuilder = StringBuilder()
                                if (l2 != 0L) {
                                    stringBuilder.append(l2)
                                    stringBuilder.append("d ")
                                }

                                if (l4 != 0L) {
                                    stringBuilder.append(l4)
                                    stringBuilder.append("h ")
                                }
                                if (l6 != 0L) {
                                    stringBuilder.append(l6)
                                    stringBuilder.append("m ")
                                }
                                stringBuilder.append(l7)
                                stringBuilder.append("s left")
                                // upcomingMatchesAdapter.notifyItemChanged(viewType)
                                listeners.onTicks(stringBuilder.toString())
                            } catch (exception: Exception) {
                                // MyUtils.logd("timestamp",exception.message)
                                exception.printStackTrace()
                            }
                        } else {
                            listeners.onTimeFinished()
                        }

                    }

                    override fun onFinish() {
                        listeners.onTimeFinished()
                        isCountedObjectCreated = false
                    }
                }
                timer!!.start()
            } else {
                listeners.onTimeFinished()
                isCountedObjectCreated = false
            }

        }

        fun stopTimer() {
            if (timer != null) {
                isCountedObjectCreated = false
                timer!!.cancel()
                timer = null
            }
        }


        fun countDownStartForAdaptors(
            starttimeStamp: Long,
            listeners: OnMatchTimerStarted
        ) {
            if (starttimeStamp > currentTimeStamp) {
                //BindingUtils.logD("TimerLogs","Count Down timer Called Adaptors")
                var timerAdapters = object : CountDownTimer(starttimeStamp, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val itemLong = starttimeStamp
                        val date = Date(itemLong * 1000L)
                        //val date2 = Date(currentTimeStamp*1000L)
                        val date2 = Date()
                        if (date2.before(date)) {
                            val l8 = date.time - date2.time
                            val l2 = l8 / 86400000L
                            java.lang.Long.signum(l2)
                            val l3 = l8 - 86400000L * l2
                            val l4 = l3 / 3600000L
                            val l5 = l3 - 3600000L * l4
                            try {
                                val l6 = l5 / 60000L
                                val l7 = (l5 - 60000L * l6) / 1000L
                                //val customTextView2: CustomTextView = customTextView
                                val stringBuilder = StringBuilder()
                                if (l2 != 0L) {
                                    stringBuilder.append(l2)
                                    stringBuilder.append("d ")
                                }

                                if (l4 != 0L) {
                                    stringBuilder.append(l4)
                                    stringBuilder.append("h ")
                                }
                                if (l6 != 0L) {
                                    stringBuilder.append(l6)
                                    stringBuilder.append("m ")
                                }
                                stringBuilder.append(l7)
                                stringBuilder.append("s left")
                                listeners.onTicks(stringBuilder.toString())
                            } catch (exception: Exception) {
                                // MyUtils.logd("TimerLogs", exception.message)
                                exception.printStackTrace()
                            }
                        } else {
                            listeners.onTimeFinished()
                        }

                    }

                    override fun onFinish() {
                        listeners.onTimeFinished()
                    }
                }
                timerAdapters.start()
            } else {
                listeners.onTimeFinished()
            }

        }
    }
}