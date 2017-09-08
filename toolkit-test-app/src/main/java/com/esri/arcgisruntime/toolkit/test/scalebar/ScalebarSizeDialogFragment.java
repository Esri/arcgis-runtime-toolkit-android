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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.esri.arcgisruntime.toolkit.test.R;

/**
 * Displays a dialog asking the user to specify a size.
 *
 * @since 100.1.0
 */
public final class ScalebarSizeDialogFragment extends DialogFragment {
  private static final String KEY_TITLE = "KEY_TITLE";

  private static final String KEY_VALUE = "KEY_VALUE";

  /**
   * The host activity must implement this interface to receive the callback.
   *
   * @since 100.1.0
   */
  public interface Listener {
    /**
     * Called when user specifies a size.
     *
     * @param size the specified size
     * @since 100.1.0
     */
    void onScalebarSizeSpecified(float size);
  }

  private Listener mListener;

  private EditText mSizeField;

  /**
   * Creates a new instance of ScalebarSizeDialogFragment.
   *
   * @param title the title of the dialog
   * @param value the current size value, for display as a hint
   * @return the ScalebarSizeDialogFragment
   * @since 100.1.0
   */
  public static ScalebarSizeDialogFragment newInstance(String title, float value) {
    // Create the fragment
    ScalebarSizeDialogFragment fragment = new ScalebarSizeDialogFragment();

    // Set arguments on the fragment
    Bundle args = new Bundle();
    args.putString(KEY_TITLE, title);
    args.putFloat(KEY_VALUE, value);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the Listener so we can send events to the host
      mListener = (Listener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw an exception
      throw new ClassCastException(context.toString() + " must implement ScalebarSizeDialogFragment.Listener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Get the arguments
    String title = getArguments().getString(KEY_TITLE);
    String valueString = Float.toString(getArguments().getFloat(KEY_VALUE));
    if (valueString.endsWith(".0")) {
      valueString = valueString.substring(0, valueString.length() - 2);
    }

    // Inflate the custom view we use for this dialog and initialize the size field
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View sizeDialog = inflater.inflate(R.layout.scalebar_size_dialog, null);
    mSizeField = sizeDialog.findViewById(R.id.scalebar_size);
    mSizeField.setHint(valueString);

    // Setup the dialog builder
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(title)
        .setView(sizeDialog)
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // User cancelled the dialog - do nothing
          }
        })
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // Make callback with the specified size
            try {
              mListener.onScalebarSizeSpecified(Float.parseFloat(mSizeField.getText().toString()));
            } catch (NumberFormatException e) {
              Log.e(ScalebarSizeDialogFragment.this.getTag(), "Failed to parse input as a float");
            }
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }

}
