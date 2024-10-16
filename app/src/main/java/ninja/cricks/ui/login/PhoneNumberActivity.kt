package ninja.cricks.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import ninja.cricks.R
import ninja.cricks.databinding.ActivityPhoneNumberBinding
import ninja.cricks.utils.MyUtils

class PhoneNumberActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhoneNumberBinding
    private var canVerify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_number)
        init()
    }

    private fun init() {
        binding.close.setOnClickListener {
            this.onBackPressed()
        }
        binding.verify.setOnClickListener {
            val mobile = binding.edPhoneNo.text.toString()
            for (i in mobile) {
                if (i == '0' || i == '1' || i == '2' || i == '3' || i == '4' || i == '5' || i == '6' || i == '7' || i == '8' || i == '9') {
                } else {
                    MyUtils.showToast(this, "Please enter a valid mobile number")
                    return@setOnClickListener
                }
            }
            if (mobile.length != 10) {
                MyUtils.showToast(this, "Please enter 10 digit mobile number")
            } else {
                val intent = Intent(this@PhoneNumberActivity, PhoneLoginActivity::class.java)
                intent.putExtra("mobileNo", mobile)
                startActivity(intent)
            }
        }
    }
}