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
package com.esri.arcgisruntime.toolkit.scalebartestapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Displays a dialog asking the user to select a color option.
 *
 * @since 100.1.0
 */
public final class SpecifyColorDialogFragment extends DialogFragment {

  /**
   * The host activity must implement this interface to receive the callback.
   *
   * @since 100.1.0
   */
  public interface Listener {
    /**
     * Called when user selects a scalebar color option.
     *
     * @param color the selected color
     * @since 100.1.0
     */
    void onScalebarColorSpecified(int color);
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
      throw new ClassCastException(context.toString() + " must implement SpecifyColorDialogFragment.Listener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Select color:")
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // User cancelled the dialog - do nothing
          }
        })
        .setItems(R.array.scalebar_colors, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Make callback with the selected item
            switch (which) {
              case 0:
                mListener.onScalebarColorSpecified(Color.BLACK);
                break;
              case 1:
                mListener.onScalebarColorSpecified(Color.BLUE);
                break;
              case 2:
                mListener.onScalebarColorSpecified(Color.CYAN);
                break;
              case 3:
                mListener.onScalebarColorSpecified(Color.DKGRAY);
                break;
              case 4:
                mListener.onScalebarColorSpecified(Color.GRAY);
                break;
              case 5:
                mListener.onScalebarColorSpecified(Color.GREEN);
                break;
              case 6:
                mListener.onScalebarColorSpecified(Color.LTGRAY);
                break;
              case 7:
                mListener.onScalebarColorSpecified(Color.MAGENTA);
                break;
              case 8:
                mListener.onScalebarColorSpecified(Color.RED);
                break;
              case 9:
                mListener.onScalebarColorSpecified(Color.WHITE);
                break;
              case 10:
                mListener.onScalebarColorSpecified(Color.YELLOW);
                break;
            }
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }
}
