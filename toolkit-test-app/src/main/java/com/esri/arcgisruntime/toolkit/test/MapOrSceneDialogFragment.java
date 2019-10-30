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
package com.esri.arcgisruntime.toolkit.test;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

/**
 * Displays a dialog asking the user to select whether to use a map or a scene.
 */
public final class MapOrSceneDialogFragment extends DialogFragment {

  /**
   * The host activity must implement this interface to receive the callback.
   */
  public interface Listener {
    /**
     * Called when user selects map or scene.
     *
     * @param useMap true to use a map, false to use a scene
     */
    void onMapOrSceneSpecified(boolean useMap);
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
      // The activity doesn't implement the interface: display message, log the exception and dismiss the dialog
      String msg = "ERROR: the calling activity must implement MapOrSceneDialogFragment.Listener";
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
      Log.e("MapOrSceneDialog", msg);
      e.printStackTrace();
      dismiss();
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Use Map or Scene?")
        .setItems(R.array.map_or_scene, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Make callback with the selected item
            switch (which) {
              case 0:
                mListener.onMapOrSceneSpecified(true);
                break;
              case 1:
                mListener.onMapOrSceneSpecified(false);
                break;
              default:
                // shouldn't happen - do nothing
            }
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }
}
