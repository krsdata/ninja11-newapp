package ninja.cricks

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import ninja.cricks.databinding.ActivityUpdateApplicationBinding
import ninja.cricks.ui.BaseActivity
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.DownloadController

class UpdateApplicationActivity : BaseActivity() {

    private var mBinding: ActivityUpdateApplicationBinding? = null
    private lateinit var downloadController: DownloadController
    lateinit var customProgress: CustomeProgressDialog

    companion object {
        val TAG: String = UpdateApplicationActivity::class.java.simpleName
        const val REQUEST_CODE_APK_UPDATE = "apkupdateurl"
        const val REQUEST_RELEASE_NOTE = "release_note"
        const val PERMISSION_REQUEST_STORAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_update_application)
        customeProgressDialog = CustomeProgressDialog(this)

        val apkUrl = intent.getStringExtra(REQUEST_CODE_APK_UPDATE)
        val releaseNote = intent.getStringExtra(REQUEST_RELEASE_NOTE)

        Log.e(TAG, "Release Notes: $releaseNote")
        if (!TextUtils.isEmpty(releaseNote)) {
            mBinding?.releaseNote?.text = releaseNote
        }

        if (!apkUrl.isNullOrEmpty()) {
            downloadController = DownloadController(this, apkUrl, customeProgressDialog)
        } else {
            Log.e(TAG, "APK URL is null or empty")
        }

        mBinding?.toolbar?.apply {
            title = getString(R.string.label_update)
            setTitleTextColor(resources.getColor(R.color.white, theme))
            setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            setSupportActionBar(this)
            setNavigationOnClickListener { finish() }
        }

        mBinding?.addCash?.setOnClickListener {
            Log.e(TAG, "Download button clicked")
            checkStoragePermission()
        }

        mBinding?.closeButton?.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {}

    override fun onUploadedImageUrl(url: String) {}

    /**
     * Checks and requests necessary permissions before downloading.
     */
    private fun checkStoragePermission() {
        if (arePermissionsGranted()) {
            Log.e(TAG, "Permissions already granted. Proceeding with download.")
            downloadController.enqueueDownload()
        } else {
            Log.e(TAG, "Requesting permissions")
            requestPermissions()
        }
    }

    /**
     * Requests storage permissions dynamically.
     */
    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // Android 9 and below
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11 and above
            if (!android.provider.Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_STORAGE)
        }
    }

    /**
     * Checks if necessary permissions are granted.
     */
    private fun arePermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            return Settings.canDrawOverlays(this@UpdateApplicationActivity)
//        } else {
//            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//        }
    }

    /**
     * Handles the permission request result.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.e(TAG, "All permissions granted. Proceeding with download.")
                downloadController.enqueueDownload()
            } else {
                Log.e(TAG, "Permission denied. Cannot proceed with download.")
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}


/*
package ninja.cricks

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import ninja.cricks.databinding.ActivityUpdateApplicationBinding
import ninja.cricks.ui.BaseActivity
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.DownloadController


class UpdateApplicationActivity : BaseActivity() {

    private var mBinding: ActivityUpdateApplicationBinding? = null
    lateinit var downloadController: DownloadController

    companion object {
        val TAG: String = UpdateApplicationActivity::class.java.simpleName
        val REQUEST_CODE_APK_UPDATE: String = "apkupdateurl"
        val REQUEST_RELEASE_NOTE: String = "release_note"
        const val PERMISSION_REQUEST_STORAGE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_update_application
        )
        customeProgressDialog = CustomeProgressDialog(this)

        val apkUrl = intent.getStringExtra(REQUEST_CODE_APK_UPDATE)
        val releaseNote = intent.getStringExtra(REQUEST_RELEASE_NOTE)
        Log.e(TAG, "releaseNotes ======> $releaseNote")
        if (!TextUtils.isEmpty(releaseNote)) {
            mBinding!!.releaseNote.text = releaseNote
        }
        downloadController = DownloadController(this, apkUrl!!, customeProgressDialog)

        mBinding!!.toolbar.title = this@UpdateApplicationActivity.getString(R.string.label_update)
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        mBinding!!.addCash.setOnClickListener {
            Log.e(TAG, "Button clicked")
            checkStoragePermission()
        }

        mBinding!!.closeButton.setOnClickListener{
            onBackPressed()
        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (areAllPermissionsGranted(grantResults)) {
            downloadController.enqueueDownload()
        }
    }

    private fun areAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun checkStoragePermission() {
        if (arePermissionsGranted()) {
            downloadController.enqueueDownload()
        } else {
            Log.e(TAG, "request permission")
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        // Define an array of permissions to request
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_STORAGE)
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_STORAGE)
        }

    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
//        val intent = Intent(Intent.ACTION_MAIN)
//        intent.addCategory(Intent.CATEGORY_HOME)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        startActivity(intent)
    }

    private fun arePermissionsGranted(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.REQUEST_INSTALL_PACKAGES,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}*/
