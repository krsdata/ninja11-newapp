package fancode.cricks.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import fancode.cricks.R

class CustomProgressDialog2 (context: Context?) : Dialog(context!!) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(R.layout.dialog_progress2)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}