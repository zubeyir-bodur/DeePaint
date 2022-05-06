package com.example.deepaint.base

import android.Manifest
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.deepaint.R
import com.google.android.material.snackbar.Snackbar


open class BaseActivity : AppCompatActivity() {
    private var mProgressDialog: ProgressDialog? = null
    fun requestPermissionEdit(permission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(
                this,
                permission
        ) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            permission)) {
                showExplanation("Permission Needed for Storage", "We are nice ppl, we wont use it for bad purposes...", Manifest.permission.WRITE_EXTERNAL_STORAGE, READ_WRITE_STORAGE);
            } else {
                requestPermission(permission, READ_WRITE_STORAGE)
            }
        } else {
            Toast.makeText(this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
        return true
    }

    private fun requestPermission(permissionName: String, permissionRequestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permissionName), permissionRequestCode)
    }

    private fun showExplanation(title: String,
                                     message: String,
                                     permission: String,
                                     permissionRequestCode: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, id -> requestPermission(permission, permissionRequestCode) })
        builder.create().show()
    }

    open fun isPermissionGranted(isGranted: Boolean, permission: String?) {


    }
    fun makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            @NonNull permissions: Array<String?>,
            @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_WRITE_STORAGE -> isPermissionGranted(
                    grantResults[0] == PackageManager.PERMISSION_GRANTED, permissions[0]
            )
        }
    }

    protected fun showLoading(@NonNull message: String?) {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setMessage(message)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.show()
    }

    protected fun hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    protected fun showSnackbar(@NonNull message: String?) {
        val view: View? = findViewById(R.id.content)
        if (view != null) {
            Snackbar.make(view, message!!, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val READ_WRITE_STORAGE = 52
    }
}