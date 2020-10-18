package ninja.cricks.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import ninja.cricks.SplashScreenActivity
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RequestModel
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.home.models.UsersPostDBResponse
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseFragment : Fragment() {

    var customeProgressDialog: CustomeProgressDialog?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(activity)
    }

    fun logoutApp(message: String,boolean: Boolean) {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        genericAlertDialog(message,boolean)
    }


    fun genericAlertDialog(message: String, boolean: Boolean) {
        val builder = AlertDialog.Builder(activity!!)
        //set title for alert dialog
        // builder.setTitle("Warning")
        //set message for alert dialog

        builder.setMessage(message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        if(boolean) {
            builder.setNegativeButton("Cancel", null)
        }
        builder.setPositiveButton("OK"){
                dialogInterface, which ->

            customeProgressDialog!!.show()
            var request = RequestModel()
            request.user_id = MyPreferences.getUserID(activity!!)!!
            request.token = MyPreferences.getToken(activity!!)!!
            WebServiceClient(activity!!).client.create(IApiMethod::class.java).logout(request)
                .enqueue(object : Callback<UsersPostDBResponse?> {
                    override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                    }

                    override fun onResponse(
                        call: Call<UsersPostDBResponse?>?,
                        response: Response<UsersPostDBResponse?>?
                    ) {

                        customeProgressDialog!!.dismiss()
                        MyPreferences.clear(activity!!)
                        val intent = Intent(
                            activity!!,
                            SplashScreenActivity::class.java
                        )
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        activity!!.finish()
                    }

                })
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }
}