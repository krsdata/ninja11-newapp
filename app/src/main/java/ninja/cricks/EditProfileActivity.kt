package ninja.cricks

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import hashim.gallerylib.model.GalleryModel
import hashim.gallerylib.observer.OnResultCallback
import hashim.gallerylib.util.GalleryConstants
import hashim.gallerylib.view.galleryActivity.GalleryLib
import ninja.cricks.VerifyDocumentsActivity.Companion
import ninja.cricks.databinding.ActivityEditProfileBinding
import ninja.cricks.models.ResponseModel
import ninja.cricks.models.UserInfo
import ninja.cricks.models.UsersPostDBResponse
import ninja.cricks.network.IApiMethod
import ninja.cricks.network.WebServiceClient
import ninja.cricks.ui.BaseActivity
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.MyPreferences
import ninja.cricks.utils.MyUtils
import ninja.cricks.utils.setLocalImage
import ninja.cricks.utils.setServerImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

class EditProfileActivity : AppCompatActivity() {

    private var mBinding: ActivityEditProfileBinding? = null
    private var photoUrl: String = ""
    private lateinit var userInfo: UserInfo
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private lateinit var mContext: Context
    private var mImageFile: File? = null
    private var isPasscode: Boolean? = false
    private var galleryModels = ArrayList<GalleryModel>()

    var PERMISSIONS: ArrayList<String> = ArrayList()

    companion object {
        private var TAG: String = EditProfileActivity::class.java.simpleName
        private const val GALLERY_IMAGE_REQ_CODE = 102
        private const val CAMERA_IMAGE_REQ_CODE = 103
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        mContext = this

        userInfo = (application as NinjaApplication).userInformations
        customeProgressDialog = CustomeProgressDialog(mContext)

        mBinding!!.toolbar.title = "Update Profile"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        Glide.with(this)
            .load(userInfo.profileImage)
            .placeholder(R.drawable.ic_profile)
            .into(mBinding!!.profileImage)

        updateUserOtherInfo()

        mBinding!!.profileImage.setOnClickListener {
            if (!TextUtils.isEmpty(photoUrl)) {
                val intent = Intent(this@EditProfileActivity, FullScreenImageViewActivity::class.java)
                intent.putExtra(FullScreenImageViewActivity.KEY_IMAGE_URL, photoUrl)
                startActivity(intent)
            } else {
                if(createPermission()){
                    openGallery()
                } else {
                    requestPermissions(PERMISSIONS.toTypedArray(), GalleryConstants.REQUEST_Permission_Gallery)
                }
            }
        }

        mBinding!!.imageEdit.setOnClickListener {
            if(createPermission()){
                openGallery()
            } else {
                requestPermissions(PERMISSIONS.toTypedArray(), GalleryConstants.REQUEST_Permission_Gallery)
            }
        }

        mBinding!!.dateOfBirth.setOnClickListener {
            val c = Calendar.getInstance()
            val mYear = c[Calendar.YEAR]
            val mMonth = c[Calendar.MONTH]
            val mDay = c[Calendar.DAY_OF_MONTH]

            val datePickerDialog = DatePickerDialog(
                this,
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val a = monthOfYear + 1
                    val formatter = DecimalFormat("00")
                    val month = formatter.format(a.toLong())

                    val formatter2 = DecimalFormat("00")
                    val date = formatter2.format(dayOfMonth.toLong())

                    mBinding!!.dateOfBirth.setText(String.format(Locale.ENGLISH, "%s-%s-%d", date, month, year))
                }, mYear, mMonth, mDay
            )
            datePickerDialog.show()
        }

        mBinding!!.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }
        initProfile()
        getProfile()
    }

    private fun updateUserOtherInfo() {
        if (!TextUtils.isEmpty(userInfo.teamName)) {
            mBinding!!.editTeamName.setText(userInfo.teamName)
            mBinding!!.editTeamName.setSelection(userInfo.teamName.length)
        }

        if (!TextUtils.isEmpty(userInfo.dateOfBirth)) {
            mBinding!!.dateOfBirth.setText(userInfo.dateOfBirth)
        }

        if (!TextUtils.isEmpty(userInfo.city)) {
            mBinding!!.editCity.setText(userInfo.city)
        }
    }

//    private fun selectImage() {
//        val options: Array<CharSequence> = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
//
//        val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
//        builder.setTitle("Add Photo")
//        builder.setItems(options) { dialog, items ->
//            if (options[items] == "Take Photo") {
//                getImageCamera()
//            } else if (options[items] == "Choose from Gallery") {
//                getImageGallery()
//            } else if (options[items] == "Cancel") {
//                dialog!!.dismiss()
//            }
//        }
//        builder.show()
//    }

//    private fun getImageCamera() {
//        ImagePicker.with(this)
//            .cameraOnly()
//            .crop()
//            .compress(2048)
//            .saveDir(File(cacheDir, "Ninja11"))
//            .start(CAMERA_IMAGE_REQ_CODE)
//    }
//
//    private fun getImageGallery() {
//        ImagePicker.with(this)
//            .galleryOnly()
//            .crop()
//            .compress(2048)
//            .saveDir(File(cacheDir, "Ninja11"))
//            .galleryMimeTypes(
//                mimeTypes = arrayOf(
//                    "image/png",
//                    "image/jpg",
//                    "image/jpeg"
//                )
//            )
//            .maxResultSize(1080, 1920)
//            .start(GALLERY_IMAGE_REQ_CODE)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            Log.e(TAG, "Path:${ImagePicker.getFilePath(data)}")
//            val file = ImagePicker.getFile(data)!! // File object will not be null for RESULT_OK
//            when (requestCode) {
//                GALLERY_IMAGE_REQ_CODE -> {
//                    mImageFile = file
//                    mBinding!!.profileImage.setLocalImage(file, true)
//                    uploadImageToServer(file)
//                }
//
//                CAMERA_IMAGE_REQ_CODE -> {
//                    mImageFile = file
//                    mBinding!!.profileImage.setLocalImage(file, true)
//                    uploadImageToServer(file)
//                }
//            }
//        } else if (resultCode == ImagePicker.RESULT_ERROR) {
//            MyUtils.showToast(this, ImagePicker.getError(data))
//        } else {
//            MyUtils.showToast(this, "Task Cancelled")
//        }
//    }

    private fun initProfile() {
        photoUrl = userInfo.profileImage
        mBinding!!.editTeamName.setText(userInfo.teamName)
        mBinding!!.updateProfileName.setText(userInfo.fullName)
        mBinding!!.updateEmail.setText(userInfo.userEmail)
        mBinding!!.updateEditMobile.setText(userInfo.mobileNumber)

        if (userInfo.gender.equals("male")) {
            mBinding!!.genderMale.isChecked = true
            mBinding!!.genderFemale.isChecked = false
        } else {
            mBinding!!.genderMale.isChecked = false
            mBinding!!.genderFemale.isChecked = true
        }

        if (userInfo.profileImage.isNotEmpty())
            Glide.with(this).load(userInfo.profileImage).placeholder(R.drawable.player_blue)
                .into(mBinding!!.profileImage)
    }

    private fun updateProfile() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        val editTeamName = mBinding!!.editTeamName.text.toString()
        val editName = mBinding!!.updateProfileName.text.toString()
        val mobileNumber = mBinding!!.updateEditMobile.text.toString()
        val emailAddress = mBinding!!.updateEmail.text.toString()
        val cityName = mBinding!!.editCity.text.toString()
        val passcode = mBinding!!.editPasscode.text.toString()
        var gender = "male"
        if (!mBinding!!.genderMale.isChecked) {
            gender = "female"
        }
        val dateOfBirth = mBinding!!.dateOfBirth.text.toString()

        if (TextUtils.isEmpty(editName)) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter your real name")
            return
        } else if (TextUtils.isEmpty(mobileNumber)) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter valid mobile number")
            return
        } else if (mobileNumber.length < 10) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter valid mobile number")
            return
        } else if (TextUtils.isEmpty(emailAddress) || !MyUtils.isEmailValid(emailAddress)) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter valid email address")
            return
        } else if (TextUtils.isEmpty(cityName)) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter city Name")
            return
        } else if (TextUtils.isEmpty(dateOfBirth)) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter your Date of Birth")
            return
        } else if (TextUtils.isEmpty(passcode) && passcode.length != 6) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter new passcode and length will be 6 characters")
            return
        } else {


            mBinding!!.progressBar.visibility = View.VISIBLE

            val jsonRequest = JsonObject()
            jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
            jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
            jsonRequest.addProperty("image_url", photoUrl)
            jsonRequest.addProperty("team_name", mBinding!!.editTeamName.text.toString())
            jsonRequest.addProperty("name", editName)
            jsonRequest.addProperty("email", emailAddress)
            jsonRequest.addProperty("mobile_number", mobileNumber)
            jsonRequest.addProperty("city", cityName)
            jsonRequest.addProperty("gender", gender)
            jsonRequest.addProperty("dateOfBirth", dateOfBirth)
            jsonRequest.addProperty("pass_code", passcode)


            Log.e(TAG, "jsonRequest =========> $jsonRequest")

            WebServiceClient(this).client.create(IApiMethod::class.java).updateProfile(jsonRequest)
                .enqueue(object : Callback<UsersPostDBResponse?> {
                    override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                        mBinding!!.progressBar.visibility = View.GONE
                    }

                    override fun onResponse(
                        call: Call<UsersPostDBResponse?>?,
                        response: Response<UsersPostDBResponse?>?
                    ) {
                        mBinding!!.progressBar.visibility = View.GONE
                        val res = response!!.body()
                        if (res != null) {
                            if (res.status) {
                                userInfo.profileImage = photoUrl
                                userInfo.teamName = editTeamName
                                userInfo.fullName = editName
                                userInfo.city = cityName
                                userInfo.gender = gender
                                userInfo.dateOfBirth = dateOfBirth

                                (application as NinjaApplication).saveUserInformations(userInfo)
                                MyUtils.showMessage(mContext, "Profile updated successfully")
                            } else {
                                if (res.code == 1001) {
                                    MyUtils.showMessage(this@EditProfileActivity, res.message)
                                    MyUtils.logoutApp(this@EditProfileActivity)
                                } else {
                                    MyUtils.showMessage(this@EditProfileActivity, res.message)
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun getProfile() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)

        WebServiceClient(this).client.create(IApiMethod::class.java).getProfile(jsonRequest)
            .enqueue(object : Callback<ResponseModel?> {
                override fun onFailure(call: Call<ResponseModel?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<ResponseModel?>?,
                    response: Response<ResponseModel?>?
                ) {
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val infoModels = res.infomodel
                            if (infoModels != null) {
                                (application as NinjaApplication).saveUserInformations(infoModels)
                                userInfo = (application as NinjaApplication).userInformations
                                if (res.passcode != null) {
                                    isPasscode = res.passcode
                                }
                                initProfile()
                                updateUserOtherInfo()
                            } else {
                                MyUtils.showToast(
                                    this@EditProfileActivity,
                                    "Something went wrong, please contact admin"
                                )
                            }
                        } else {
                            if (res.statusCode == 1001) {
                                MyUtils.showMessage(this@EditProfileActivity, res.message)
                                MyUtils.logoutApp(this@EditProfileActivity)
                            } else {
                                MyUtils.showMessage(this@EditProfileActivity, res.message)
                            }
                        }
                    }
                }
            })
    }

    private fun uploadImageToServer(file: File) {

        var multipartImage: MultipartBody.Part? = null
        val requestPanImage: RequestBody = file.asRequestBody("multipart/jpg".toMediaTypeOrNull())
        multipartImage = MultipartBody.Part.createFormData("image_bytes", file.name, requestPanImage)

        val userId: RequestBody = createPartFromString(MyPreferences.getUserID(mContext)!!)
        val documentType: RequestBody = createPartFromString(BaseActivity.DOCUMENTS_TYPE_PROFILES)
        val systemToken: RequestBody = createPartFromString(MyPreferences.getSystemToken(mContext)!!)

        val map: HashMap<String, RequestBody> = HashMap<String, RequestBody>()
        map["user_id"] = userId
        map["documents_type"] = documentType
        map["system_token"] = systemToken

        customeProgressDialog.show()
        WebServiceClient(mContext).client.create(IApiMethod::class.java)
            .saveDocumentImage(map, multipartImage)
            .enqueue(object : Callback<ResponseModel?> {
                override fun onFailure(call: Call<ResponseModel?>, t: Throwable) {
                    customeProgressDialog.dismiss()
                    Log.e(TAG, "error from server after image upload ==========> ${t.localizedMessage!!}")
                    MyUtils.showToast(this@EditProfileActivity, t.localizedMessage!!)
                }

                override fun onResponse(call: Call<ResponseModel?>, response: Response<ResponseModel?>) {
                    if (!isFinishing) {
                        customeProgressDialog.dismiss()
                        val res = response.body()

                        Log.e(TAG, "response from server after image upload ==========> ${res.toString()}")
                        if (res != null) {
                            if (res.status) {
                                photoUrl = res.image_url
                                userInfo.profileImage = res.image_url

                                mBinding!!.profileImage.setServerImage(res.image_url, true)

                                MyUtils.showMessage(mContext, res.message)
                            } else {
                                MyUtils.showMessage(mContext, res.message)
                            }
                        }
                    }
                }
            })
    }

    private fun createPartFromString(param: String): RequestBody {
        return param.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun openGallery() {
        GalleryLib(this).showGallery(
            isDialog = false,
            isOpenEdit = true,
            selectionType = GalleryConstants.GalleryTypeImages,
            locale = "en",
            maxSelectionCount = 1,
            gridColumnsCount = 4,
            selected = galleryModels,
            onResultCallback = object : OnResultCallback {
                override fun onDismiss() {

                }

                override fun onResult(list: ArrayList<GalleryModel>) {
                    galleryModels = list
//                    uploadImageToServer(File(galleryModels[0].url))
                }
            },
            galleryResultLauncher = galleryResultLauncher,
        )
    }

    private val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //back from gallery activity
            val dataList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.extras?.getParcelableArrayList(
                    GalleryConstants.selected,
                    GalleryModel::class.java
                ) as java.util.ArrayList<GalleryModel>
            } else {
                result.data?.extras?.get(GalleryConstants.selected) as java.util.ArrayList<*>
            }
            if (dataList.isNotEmpty()) {
                galleryModels = dataList as ArrayList<GalleryModel>
                Log.e(EditProfileActivity.TAG, "galleryModels from activity result =======> ${galleryModels[0].toString()}")

                val file: File = File(galleryModels[0].sdcardPath)

                uploadImageToServer(file)
            }
        }
    }

    private fun createPermission(): Boolean {

        PERMISSIONS.add(Manifest.permission.CAMERA)
        PERMISSIONS.add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PERMISSIONS.add(Manifest.permission.READ_MEDIA_IMAGES)
            PERMISSIONS.add(Manifest.permission.READ_MEDIA_VIDEO)
            PERMISSIONS.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            PERMISSIONS.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= 23 && !hasPermissions(this@EditProfileActivity, PERMISSIONS.toTypedArray())) {
            requestPermissions(PERMISSIONS.toTypedArray(), GalleryConstants.REQUEST_Permission_Gallery)
            return false
        }
        return true
    }

    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (context != null) {
            for (p in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        p
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            GalleryConstants.REQUEST_Permission_Gallery ->
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    MaterialAlertDialogBuilder(this@EditProfileActivity)
                        .setMessage(getString(hashim.gallerylib.R.string.you_should_allow_all_permissions_to_fetch_gallery_images))
                        .setPositiveButton(getString(hashim.gallerylib.R.string.settings)) { dialog, which ->
                            // Respond to positive button press
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                this@EditProfileActivity.packageName, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .setNegativeButton(getString(hashim.gallerylib.R.string.cancel)) { dialog, which ->
                            // Respond to positive button press
                        }
                        .show()
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            else -> {
            }
        }
    }
    
}