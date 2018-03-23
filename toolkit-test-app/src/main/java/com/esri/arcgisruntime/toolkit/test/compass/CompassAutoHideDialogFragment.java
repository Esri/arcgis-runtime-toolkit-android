package com.esri.arcgisruntime.toolkit.test.compass;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import com.esri.arcgisruntime.toolkit.test.R;

/**
 * Displays a dialog asking the user to select an auto-hide option.
 */
public final class CompassAutoHideDialogFragment extends DialogFragment {

  /**
   * The host activity must implement this interface to receive the callback.
   */
  public interface Listener {
    /**
     * Called when user selects an auto-hide option.
     *
     * @param autoHide true to auto hide the Compass, false to have it always show
     */
    void onCompassAutoHideSpecified(boolean autoHide);
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
      throw new ClassCastException(context.toString() + " must implement CompassAutoHideDialogFragment.Listener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Auto hide feature:")
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // User cancelled the dialog - do nothing
          }
        })
        .setItems(R.array.compass_auto_hide, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Make callback with the selected item
            switch (which) {
              case 0:
                mListener.onCompassAutoHideSpecified(true);
                break;
              case 1:
                mListener.onCompassAutoHideSpecified(false);
                break;
            }
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }
}
