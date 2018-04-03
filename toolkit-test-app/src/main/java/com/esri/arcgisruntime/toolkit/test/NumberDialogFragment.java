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
package com.esri.arcgisruntime.toolkit.test;

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

/**
 * Displays a dialog asking the user to specify a number. Uses android:inputType="number" which results in an unsigned
 * integer number.
 */
public final class NumberDialogFragment extends DialogFragment {
  private static final String KEY_TITLE = "KEY_TITLE";

  private static final String KEY_VALUE = "KEY_VALUE";

  /**
   * The host activity must implement this interface to receive the callback.
   */
  public interface Listener {
    /**
     * Called when user specifies a number.
     *
     * @param number the specified number
     */
    void onNumberSpecified(int number);
  }

  private Listener mListener;

  private EditText mInputField;

  /**
   * Creates a new instance of NumberDialogFragment.
   *
   * @param title the title of the dialog
   * @param value the current value of the number being specified, for display as a hint
   * @return the NumberDialogFragment
   */
  public static NumberDialogFragment newInstance(String title, int value) {
    // Create the fragment
    NumberDialogFragment fragment = new NumberDialogFragment();

    // Set arguments on the fragment
    Bundle args = new Bundle();
    args.putString(KEY_TITLE, title);
    args.putInt(KEY_VALUE, value);
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
      throw new ClassCastException(context.toString() + " must implement NumberDialogFragment.Listener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    // Get the arguments
    String title = getArguments().getString(KEY_TITLE);
    String valueString = Integer.toString(getArguments().getInt(KEY_VALUE));

    // Inflate the custom view we use for this dialog and initialize the input field
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View sizeDialog = inflater.inflate(R.layout.number_dialog, null);
    mInputField = sizeDialog.findViewById(R.id.number_input_field);
    mInputField.setHint(valueString);

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
              mListener.onNumberSpecified(Integer.parseInt(mInputField.getText().toString()));
            } catch (NumberFormatException e) {
              Log.e(NumberDialogFragment.this.getTag(), "Failed to parse input as an integer");
            }
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }

}
