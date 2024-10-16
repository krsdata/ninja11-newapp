package ninja.cricks

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import hashim.gallerylib.util.GalleryConstants
import ninja.cricks.databinding.ActivityUpdateApplicationBinding
import ninja.cricks.ui.BaseActivity
import ninja.cricks.utils.CustomeProgressDialog
import ninja.cricks.utils.DownloadController


class UpdateApplicationActivity : BaseActivity() {

    private var mBinding: ActivityUpdateApplicationBinding? = null
    lateinit var downloadController: DownloadController
    var PERMISSIONS: ArrayList<String> = ArrayList()

    companion object {
        val TAG: String = UpdateApplicationActivity::class.java.simpleName
        val REQUEST_CODE_APK_UPDATE: String = "apkupdateurl"
        val REQUEST_RELEASE_NOTE: String = "release_note"
        val REQUEST_TITLE: String = "title"
        const val PERMISSION_REQUEST_STORAGE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_update_application)
        customeProgressDialog = CustomeProgressDialog(this)

        val apkUrl = intent.getStringExtra(REQUEST_CODE_APK_UPDATE)
        val releaseNote = intent.getStringExtra(REQUEST_RELEASE_NOTE)
        val forceUpdate = intent!!.getBooleanExtra(REQUEST_TITLE, false)
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

        mBinding!!.closeImage.setOnClickListener {
            Log.e(TAG, "close image clicked")
            finish()
        }

        mBinding!!.addCash.setOnClickListener {
            if (createPermission()) {
                downloadController.enqueueDownload()
            } else {
                requestPermissions(PERMISSIONS.toTypedArray(), GalleryConstants.REQUEST_Permission_Gallery)
            }
        }

        if (forceUpdate) {
            mBinding!!.updateTitle.text = "Mandatory update"
            mBinding!!.closeImage.visibility = View.GONE
        } else {
            mBinding!!.updateTitle.text = "New update available"
            mBinding!!.closeImage.visibility = View.VISIBLE
        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onUploadedImageUrl(url: String) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
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

        if (!hasPermissions(this@UpdateApplicationActivity, PERMISSIONS.toTypedArray())) {
            requestPermissions(PERMISSIONS.toTypedArray(), GalleryConstants.REQUEST_Permission_Gallery)
            return false
        }
        return true
    }

    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (p in permissions) {
            if (ActivityCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            GalleryConstants.REQUEST_Permission_Gallery ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadController.enqueueDownload()
                } else {
                    MaterialAlertDialogBuilder(this@UpdateApplicationActivity)
                        .setMessage("you should allow all permissions to update the application")
                        .setPositiveButton(getString(hashim.gallerylib.R.string.settings)) { dialog, which ->
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", this@UpdateApplicationActivity.packageName, null)
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .setNegativeButton(getString(hashim.gallerylib.R.string.cancel)) { dialog, which ->
                            // Respond to negative positive button press
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts("package", this@UpdateApplicationActivity.packageName, null)
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }

            else -> {
            }
        }
    }
}