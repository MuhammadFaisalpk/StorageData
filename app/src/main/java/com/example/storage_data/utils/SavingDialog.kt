package com.example.storage_data.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import com.example.storage_data.R

class SavingDialog {
    companion object {
        fun progressDialog(context: Context): Dialog {
            val dialog = Dialog(context)
            val inflate = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
            dialog.setContentView(inflate)
            dialog.setCancelable(false)

            return dialog
        }
    }
}