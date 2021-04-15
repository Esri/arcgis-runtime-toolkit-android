/*
 * Copyright 2021 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.arcgisruntime.toolkit.test.popup.views.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.esri.arcgisruntime.mapping.popup.Popup
import com.esri.arcgisruntime.toolkit.popup.util.observeEvent
import com.esri.arcgisruntime.toolkit.popup.viewmodel.PopupViewModel
import com.esri.arcgisruntime.toolkit.test.R
import com.esri.arcgisruntime.toolkit.test.databinding.FragmentPopupBinding
import kotlinx.android.synthetic.main.fragment_popup.*

/**
 * Responsible for displaying a Popup.
 */
class PopupFragment : Fragment() {

    private val popupViewModel: PopupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentPopupBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_popup,
            container,
            false
        )

        binding.popupViewModel = popupViewModel
        binding.lifecycleOwner = this

        popupViewModel.showSavingProgressEvent.observeEvent(viewLifecycleOwner) { isShowProgressBar ->
            if (isShowProgressBar) {
                requireActivity().window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                progressBarLayout.visibility = View.VISIBLE
            } else {
                progressBarLayout.visibility = View.GONE
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        }

        popupViewModel.showSavePopupErrorEvent.observeEvent(viewLifecycleOwner) { errorMessage ->
            showAlertDialog(errorMessage)
        }

        popupViewModel.showDeletePopupErrorEvent.observeEvent(viewLifecycleOwner) { errorMessage ->
            showAlertDialog(errorMessage)
        }

        popupViewModel.confirmCancelPopupEditingEvent.observeEvent(viewLifecycleOwner) {
            showConfirmCancelEditingDialog()
        }

        popupViewModel.confirmDeletePopupEvent.observeEvent(viewLifecycleOwner) {
            showConfirmDeletePopupDialog()
        }

        return binding.root
    }

    /**
     * Shows dialog to confirm deleting the popup.
     */
    private fun showConfirmDeletePopupDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Delete ${(popupViewModel.popup.value as Popup).title}?")
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton(getString(R.string.ok)) { dialog, id ->
                popupViewModel.deletePopup()
            }
            // negative button text and action
            .setNegativeButton(getString(R.string.cancel)) { dialog, id -> dialog.cancel()
            }
        val alert = dialogBuilder.create()
        // show alert dialog
        alert.show()
    }

    /**
     * Shows dialog to confirm cancelling edit mode on popup view.
     */
    private fun showConfirmCancelEditingDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Discard changes?")
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton(getString(R.string.ok)) { dialog, id ->
                popupViewModel.cancelEditing()
            }
            // negative button text and action
            .setNegativeButton(getString(R.string.cancel)) { dialog, id -> dialog.cancel()
            }
        val alert = dialogBuilder.create()
        // show alert dialog
        alert.show()
    }

    /**
     * Shows error message to the user in a dialog.
     */
    private fun showAlertDialog(message: String) {

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage(message)
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton(getString(R.string.ok)) { dialog, id ->
                dialog.cancel()
            }
        val alert = dialogBuilder.create()
        // show alert dialog
        alert.show()
    }

}
