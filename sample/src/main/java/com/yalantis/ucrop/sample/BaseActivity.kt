package com.yalantis.ucrop.sample

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */
open class BaseActivity : AppCompatActivity() {
    private var mAlertDialog: AlertDialog? = null

    /**
     * Hide alert dialog if any.
     */
    override fun onStop() {
        super.onStop()
        if (mAlertDialog != null && mAlertDialog!!.isShowing) {
            mAlertDialog!!.dismiss()
        }
    }

    /**
     * Requests given permission.
     * If the permission has been denied previously, a Dialog will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    protected fun requestPermission(permission: String, rationale: String?, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            showAlertDialog(getString(R.string.permission_title_rationale), rationale,
                { dialog, which ->
                    ActivityCompat.requestPermissions(
                        this@BaseActivity,
                        arrayOf(permission),
                        requestCode
                    )
                }, getString(R.string.label_ok), null, getString(R.string.label_cancel)
            )
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    /**
     * This method shows dialog with given title & message.
     * Also there is an option to pass onClickListener for positive & negative button.
     *
     * @param title                         - dialog title
     * @param message                       - dialog message
     * @param onPositiveButtonClickListener - listener for positive button
     * @param positiveText                  - positive button text
     * @param onNegativeButtonClickListener - listener for negative button
     * @param negativeText                  - negative button text
     */
    protected fun showAlertDialog(
        title: String?, message: String?,
        onPositiveButtonClickListener: DialogInterface.OnClickListener?,
        positiveText: String,
        onNegativeButtonClickListener: DialogInterface.OnClickListener?,
        negativeText: String
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(positiveText, onPositiveButtonClickListener)
        builder.setNegativeButton(negativeText, onNegativeButtonClickListener)
        mAlertDialog = builder.show()
    }

    companion object {
        const val REQUEST_STORAGE_READ_ACCESS_PERMISSION = 101
        const val REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 102
    }
}