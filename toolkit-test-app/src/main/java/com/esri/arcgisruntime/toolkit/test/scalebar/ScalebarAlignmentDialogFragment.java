/*
 * Copyright 2017 Esri
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
package com.esri.arcgisruntime.toolkit.test.scalebar;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import com.esri.arcgisruntime.toolkit.java.scalebar.Scalebar;
import com.esri.arcgisruntime.toolkit.test.R;

/**
 * Displays a dialog asking the user to select a scalebar alignment option.
 */
public final class ScalebarAlignmentDialogFragment extends DialogFragment {

  /**
   * The host activity must implement this interface to receive the callback.
   */
  public interface Listener {
    /**
     * Called when user selects a scalebar alignment option.
     *
     * @param alignment the selected alignment
     */
    void onScalebarAlignmentSpecified(Scalebar.Alignment alignment);
  }

  private Listener mListener;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the Listener so we can send events to the host
      mListener = (Listener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw an exception
      throw new ClassCastException(context.toString() + " must implement ScalebarAlignmentDialogFragment.Listener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Scalebar alignment:")
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // User cancelled the dialog - do nothing
          }
        })
        .setItems(R.array.scalebar_alignments, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Make callback with the selected item
            switch (which) {
              case 0:
                mListener.onScalebarAlignmentSpecified(Scalebar.Alignment.LEFT);
                break;
              case 1:
                mListener.onScalebarAlignmentSpecified(Scalebar.Alignment.CENTER);
                break;
              case 2:
                mListener.onScalebarAlignmentSpecified(Scalebar.Alignment.RIGHT);
                break;
            }
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }
}
