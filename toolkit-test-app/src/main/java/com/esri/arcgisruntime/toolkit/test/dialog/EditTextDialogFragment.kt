/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.toolkit.test.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.EditText
import com.esri.arcgisruntime.toolkit.test.R
import kotlinx.android.synthetic.main.dialog_edit_text.view.dialogEditTextView

private const val ARG_TITLE = "TITLE"
private const val ARG_POSITIVE_BUTTON_TEXT = "POSITIVE_BUTTON_TEXT"
private const val ARG_NEGATIVE_BUTTON_TEXT = "NEGATIVE_BUTTON_TEXT"
private const val ARG_EXTRA_DATA = "EXTRA_DATA"

class EditTextDialogFragment : DialogFragment() {

    private var title: String? = null
    private var positiveButtonText: String? = null
    private var negativeButtonText: String? = null
    private var extraData: Bundle? = null
    private lateinit var editTextView: EditText
    private var onButtonClickedListener: OnButtonClickedListener? = null

    companion object {
        fun newInstance(
            title: String,
            positiveButtonText: String,
            negativeButtonText: String,
            extraData: Bundle? = null
        ) =
            EditTextDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_POSITIVE_BUTTON_TEXT, positiveButtonText)
                    putString(ARG_NEGATIVE_BUTTON_TEXT, negativeButtonText)
                    if (extraData != null) {
                        putBundle(ARG_EXTRA_DATA, extraData)
                    }
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(arguments) {
            title = this?.getString(ARG_TITLE)
            positiveButtonText = this?.getString(ARG_POSITIVE_BUTTON_TEXT)
            negativeButtonText = this?.getString(ARG_NEGATIVE_BUTTON_TEXT)
        }
        (context as? OnButtonClickedListener)?.let {
            onButtonClickedListener = it
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        context?.let {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_text, null)
            editTextView = view.dialogEditTextView
            return AlertDialog.Builder(it)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(positiveButtonText) { _, _ ->
                    onButtonClickedListener?.onPositiveClicked(editTextView.text.toString(), extraData)
                }
                .setNegativeButton(negativeButtonText) { _, _ ->
                    onButtonClickedListener?.onCancelClicked()
                }
                .setCancelable(false)
                .create()
        }
        return super.onCreateDialog(savedInstanceState)
    }

    interface OnButtonClickedListener {
        fun onPositiveClicked(editTextValue: String, extraData: Bundle?)
        fun onCancelClicked()
    }

}
