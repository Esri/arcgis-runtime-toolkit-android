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
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.toolkit.test.R;

/**
 * Displays a dialog asking the user to select a basemap.
 */
public class ScalebarBasemapDialogFragment extends DialogFragment {
  private static final String URL_OS_OPEN_CARTO =
      "http://tiles.arcgis.com/tiles/qHLhLQrcvEnxjtPr/arcgis/rest/services/OS_Open_Carto_2/MapServer";

  private static final String URL_ARCTIC_OCEAN_BASE =
      "http://services.arcgisonline.com/arcgis/rest/services/Polar/Arctic_Ocean_Base/MapServer";

  private static final String URL_ROBINSON_COORD_SYSTEM =
      "http://tiles.arcgis.com/tiles/BG6nSlhZSAWtExvp/arcgis/rest/services/coordsys_robinson/MapServer";

  /**
   * The host activity must implement this interface to receive the callback.
   */
  public interface Listener {
    /**
     * Called when user selects a basemap.
     *
     * @param basemap the selected basemap
     * @since 100.1.0
     */
    void onBasemapSpecified(Basemap basemap);
  }

  private ScalebarBasemapDialogFragment.Listener mListener;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the Listener so we can send events to the host
      mListener = (ScalebarBasemapDialogFragment.Listener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw an exception
      throw new ClassCastException(context.toString() + " must implement ScalebarBasemapDialogFragment.Listener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Select basemap (Sp Ref):")
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int id) {
            // User cancelled the dialog - do nothing
          }
        })
        .setItems(R.array.scalebar_basemaps, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            // Create the selected basemap
            Basemap basemap = null;
            switch (which) {
              case 0:
                basemap = Basemap.createStreetsVector();
                break;
              case 1:
                basemap = Basemap.createImageryWithLabels();
                break;
              case 2:
                basemap = new Basemap(new ArcGISTiledLayer(URL_OS_OPEN_CARTO));
                break;
              case 3:
                basemap = new Basemap(new ArcGISTiledLayer(URL_ARCTIC_OCEAN_BASE));
                break;
              case 4:
                basemap = new Basemap(new ArcGISTiledLayer(URL_ROBINSON_COORD_SYSTEM));
                break;
            }
            // Make callback with the basemap
            mListener.onBasemapSpecified(basemap);
          }
        });
    // Create the AlertDialog object and return it
    return builder.create();
  }
}
