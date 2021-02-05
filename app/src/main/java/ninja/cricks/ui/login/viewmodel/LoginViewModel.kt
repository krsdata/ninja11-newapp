package ninja.cricks.ui.login.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import ninja.cricks.models.ResponseModel
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.RetrofitClient
import ninja.cricks.network.WebServiceClient
import ninja.cricks.utils.HardwareInfo
import ninja.cricks.utils.MyUtils
import ninja.cricks.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(application: Application) : AndroidViewModel(application),
    Callback<ResponseModel> {
    var app:Application?=application

    var btnSelected: ObservableBoolean? = null
    var email: ObservableField<String>? = null
    var password: ObservableField<String>? = null
    var progressDialog: SingleLiveEvent<Boolean>? = null
    var userLogin: MutableLiveData<ResponseModel>? = null
    var hrdinfo: HardwareInfo?=null
    init {
        btnSelected = ObservableBoolean(false)
        progressDialog = SingleLiveEvent<Boolean>()
        email = ObservableField("")
        password = ObservableField("")
        userLogin = MutableLiveData<ResponseModel>()
    }

    fun onEmailChanged(s: CharSequence, start: Int, befor: Int, count: Int) {
        btnSelected?.set(MyUtils.isMobileValid(s.toString()) && password?.get()!!.length >= 8)
    }

    fun onPasswordChanged(s: CharSequence, start: Int, befor: Int, count: Int) {
        btnSelected?.set(MyUtils.isMobileValid(email?.get()!!) && s.toString().length >= 8)
    }

    fun login() {
        progressDialog?.value = true

        /*var request = RequestModel()
        request.username = email?.get()!!
        request.password = password?.get()!!
        request.deviceDetails = hrdinfo*/

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("username", email!!.get())
        jsonRequest.addProperty("password", password!!.get())

        val gson = Gson()
        val jsonString: String = gson.toJson(hrdinfo)
        val deviceDetails: JsonObject = JsonParser().parse(jsonString).asJsonObject
        jsonRequest.add("deviceDetails", deviceDetails)

        RetrofitClient(getApplication()).client.create(IApiMethod::class.java).customerLogin(jsonRequest)
                .enqueue(this)

    }

    override fun onResponse(call: Call<ResponseModel>?, response: Response<ResponseModel>?) {
        progressDialog?.value = false
        userLogin?.value = response?.body()

    }

    override fun onFailure(call: Call<ResponseModel>?, t: Throwable?) {
        progressDialog?.value = false
        Toast.makeText(app
            , "Warning , ${t?.message}", Toast.LENGTH_LONG).show()


    }

}

